package com.mindora.app.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mindora.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(
    private val context: Context,
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.RTDB_URL)
) {
    private val gson = Gson()

    suspend fun checkForUpdate(): AppReleaseInfo? = withContext(Dispatchers.IO) {
        val remote = fetchFromFirebase() ?: fetchFromGitHub()
        if (remote != null && remote.versionCode > BuildConfig.VERSION_CODE) remote else null
    }

    private suspend fun fetchFromFirebase(): AppReleaseInfo? {
        return try {
            val snap = database.reference.child("config").child("update").get().await()
            if (!snap.exists()) return null
            val code = snap.child("versionCode").getValue(Int::class.java) ?: return null
            val name = snap.child("versionName").getValue(String::class.java) ?: code.toString()
            val url = snap.child("apkUrl").getValue(String::class.java).orEmpty()
            val notes = snap.child("releaseNotes").getValue(String::class.java).orEmpty()
            val force = snap.child("forceUpdate").getValue(Boolean::class.java) ?: true
            if (url.isBlank()) return null
            AppReleaseInfo(code, name, url, notes, force)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFromGitHub(): AppReleaseInfo? {
        return try {
            val api = URL(GITHUB_LATEST_RELEASE_API)
            val conn = (api.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 12_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Mindora-Android")
            }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().readText()
            val release = gson.fromJson(body, GitHubRelease::class.java) ?: return null
            val asset = release.assets.firstOrNull {
                it.name.endsWith(".apk", ignoreCase = true)
            } ?: return null
            val code = parseVersionCode(release.tagName, release.name)
            AppReleaseInfo(
                versionCode = code,
                versionName = release.tagName.removePrefix("v"),
                apkUrl = asset.browserDownloadUrl,
                releaseNotes = release.body.orEmpty(),
                forceUpdate = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVersionCode(tag: String?, name: String?): Int {
        val source = listOfNotNull(tag, name).joinToString(" ")
        // Prefer explicit versionCode-123 pattern, else derive from semver like 1.0.1 -> 101
        Regex("""versionCode[=:\s-]*(\d+)""", RegexOption.IGNORE_CASE).find(source)?.groupValues?.get(1)
            ?.toIntOrNull()?.let { return it }
        val parts = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?""").find(source)?.groupValues
        if (parts != null) {
            val major = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val minor = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val patch = parts.getOrNull(3)?.toIntOrNull() ?: 0
            return major * 10000 + minor * 100 + patch
        }
        return BuildConfig.VERSION_CODE
    }

    suspend fun downloadApk(
        release: AppReleaseInfo,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val outFile = File(dir, "Mindora-${release.versionName}.apk")
        if (outFile.exists()) outFile.delete()

        val conn = (URL(release.apkUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mindora-Android")
            setRequestProperty("Accept", "*/*")
        }
        if (conn.responseCode !in 200..299) {
            throw IllegalStateException("Download failed (${conn.responseCode})")
        }
        val total = conn.contentLengthLong.takeIf { it > 0 } ?: -1L
        conn.inputStream.use { input ->
            FileOutputStream(outFile).use { output ->
                val buffer = ByteArray(8 * 1024)
                var readTotal = 0L
                var lastPct = -1
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    readTotal += read
                    if (total > 0) {
                        val pct = ((readTotal * 100) / total).toInt().coerceIn(0, 100)
                        if (pct != lastPct) {
                            lastPct = pct
                            onProgress(pct)
                        }
                    }
                }
                output.flush()
            }
        }
        onProgress(100)
        outFile
    }

    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    fun openUnknownSourcesSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun installApk(apkFile: File) {
        // Prefer PackageInstaller session (fewer confirmation prompts on many devices),
        // fall back to ACTION_VIEW if needed.
        try {
            installWithSession(apkFile)
        } catch (_: Exception) {
            installWithIntent(apkFile)
        }
    }

    private fun installWithSession(apkFile: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(context.packageName)
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)
        apkFile.inputStream().use { input ->
            session.openWrite("Mindora.apk", 0, apkFile.length()).use { output ->
                input.copyTo(output)
                session.fsync(output)
            }
        }
        val callback = Intent(context, UpdateInstallReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pending = PendingIntent.getBroadcast(context, sessionId, callback, flags)
        session.commit(pending.intentSender)
        session.close()
    }

    private fun installWithIntent(apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    companion object {
        val GITHUB_LATEST_RELEASE_API: String
            get() = "https://api.github.com/repos/${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO}/releases/latest"
    }
}

private data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String = "",
    val name: String? = null,
    val body: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

private data class GitHubAsset(
    val name: String = "",
    @SerializedName("browser_download_url") val browserDownloadUrl: String = ""
)
