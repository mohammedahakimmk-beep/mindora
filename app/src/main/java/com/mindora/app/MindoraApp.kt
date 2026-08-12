package com.mindora.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.mindora.app.data.curriculum.MathCurriculum
import com.mindora.app.data.local.DataStoreManager
import com.mindora.app.data.models.EnergyState
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

        FirebaseApp.initializeApp(this)
        try {
            FirebaseDatabase.getInstance(BuildConfig.RTDB_URL).setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Persistence may already be enabled on process restart.
        }

        gson = Gson()
        dataStore = DataStoreManager(this, gson)
        energyManager = EnergyManager()
        authRepository = FirebaseAuthRepository()
        userRepository = UserRepository(dataStore = dataStore, gson = gson)
        progressRepository = ProgressRepository(dataStore = dataStore)
        energyRepository = EnergyRepository(dataStore = dataStore, energyManager = energyManager)
        mathCurriculum = MathCurriculum(this, gson)
        aiEngine = OnDeviceAiEngine(this, dataStore, gson, mathCurriculum)
        updateManager = AppUpdateManager(this)

        createNotificationChannels()
        energyManager.scheduleResetAlarm(this)
        DailyReminderWorker.schedule(this)

        appScope.launch {
            energyRepository.getEnergyState()
        }
    }

    fun resetEnergyIfNeeded() {
        appScope.launch {
            val state = energyRepository.getEnergyState()
            val reset = energyManager.checkAndResetIfNeeded(state)
            if (reset != state) {
                energyRepository.saveEnergyState(reset)
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
        lateinit var instance: MindoraApp
            private set
    }
}
