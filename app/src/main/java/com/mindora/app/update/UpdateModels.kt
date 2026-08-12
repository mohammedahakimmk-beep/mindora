package com.mindora.app.update

data class AppReleaseInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String = "",
    val forceUpdate: Boolean = true
)

sealed class UpdateUiState {
    data object Idle : UpdateUiState()
    data object Checking : UpdateUiState()
    data object UpToDate : UpdateUiState()
    data class UpdateAvailable(val release: AppReleaseInfo) : UpdateUiState()
    data class Downloading(val progress: Int, val release: AppReleaseInfo) : UpdateUiState()
    data class ReadyToInstall(val apkPath: String, val release: AppReleaseInfo) : UpdateUiState()
    data class Installing(val release: AppReleaseInfo) : UpdateUiState()
    data class Error(val message: String, val release: AppReleaseInfo? = null) : UpdateUiState()
}
