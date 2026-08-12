package com.mindora.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.mindora.app.data.curriculum.MathCurriculum
import com.mindora.app.data.local.DataStoreManager
import com.mindora.app.data.repository.EnergyRepository
import com.mindora.app.data.repository.FirebaseAuthRepository
import com.mindora.app.data.repository.ProgressRepository
import com.mindora.app.data.repository.UserRepository
import com.mindora.app.domain.ai.OnDeviceAiEngine
import com.mindora.app.energy.EnergyManager
import com.mindora.app.notifications.DailyReminderWorker
import com.mindora.app.update.AppUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MindoraApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var gson: Gson
        private set
    lateinit var dataStore: DataStoreManager
        private set
    lateinit var authRepository: FirebaseAuthRepository
        private set
    lateinit var userRepository: UserRepository
        private set
    lateinit var progressRepository: ProgressRepository
        private set
    lateinit var energyRepository: EnergyRepository
        private set
    lateinit var mathCurriculum: MathCurriculum
        private set
    lateinit var aiEngine: OnDeviceAiEngine
        private set
    lateinit var energyManager: EnergyManager
        private set
    lateinit var updateManager: AppUpdateManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseApp init failed", e)
        }

        val database = try {
            val db = FirebaseDatabase.getInstance(normalizedRtdbUrl())
            try {
                db.setPersistenceEnabled(true)
            } catch (_: Exception) {
                // Already enabled or database was used by a provider.
            }
            db
        } catch (e: Exception) {
            Log.e(TAG, "RTDB init failed, falling back to default instance", e)
            FirebaseDatabase.getInstance()
        }

        gson = Gson()
        dataStore = DataStoreManager(this, gson)
        energyManager = EnergyManager()
        authRepository = FirebaseAuthRepository(database = database)
        userRepository = UserRepository(database = database, dataStore = dataStore, gson = gson)
        progressRepository = ProgressRepository(database = database, dataStore = dataStore)
        energyRepository = EnergyRepository(database = database, dataStore = dataStore, energyManager = energyManager)
        mathCurriculum = MathCurriculum(this, gson)
        aiEngine = OnDeviceAiEngine(this, dataStore, gson, mathCurriculum)
        updateManager = AppUpdateManager(this, database)

        createNotificationChannels()

        try {
            energyManager.scheduleResetAlarm(this)
        } catch (e: Exception) {
            Log.w(TAG, "Energy alarm schedule failed", e)
        }

        try {
            DailyReminderWorker.schedule(this)
        } catch (e: Exception) {
            Log.w(TAG, "Reminder schedule failed", e)
        }

        appScope.launch {
            runCatching { energyRepository.getEnergyState() }
        }
    }

    fun resetEnergyIfNeeded() {
        appScope.launch {
            runCatching {
                val state = energyRepository.getEnergyState()
                val reset = energyManager.checkAndResetIfNeeded(state)
                if (reset != state) {
                    energyRepository.saveEnergyState(reset)
                }
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val learningChannel = NotificationChannel(
                "mindora_learning",
                "Learning Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily learning reminders" }
            val generalChannel = NotificationChannel(
                "mindora_general",
                "General",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(learningChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    companion object {
        private const val TAG = "MindoraApp"
        lateinit var instance: MindoraApp
            private set

        fun normalizedRtdbUrl(): String {
            val raw = BuildConfig.RTDB_URL.trim()
            return if (raw.endsWith("/")) raw else "$raw/"
        }
    }
}
