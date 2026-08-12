package com.mindora.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mindora.app.data.models.AiModelStatus
import com.mindora.app.data.models.EnergyState
import com.mindora.app.data.models.LearningPath
import com.mindora.app.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mindora_prefs")

class DataStoreManager(private val context: Context, private val gson: Gson) {

    private object Keys {
        val USER_PROFILE = stringPreferencesKey("user_profile")
        val ENERGY_STATE = stringPreferencesKey("energy_state")
        val LEARNING_PATH_PREFIX = "learning_path_"
        val AI_MODEL_STATUS = stringPreferencesKey("ai_model_status")
        val PROGRESS_PREFIX = "progress_"
        val ACHIEVEMENTS = stringPreferencesKey("achievements")
    }

    val userProfileFlow: Flow<UserProfile?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_PROFILE]?.let { gson.fromJson(it, UserProfile::class.java) }
    }

    val energyStateFlow: Flow<EnergyState> = context.dataStore.data.map { prefs ->
        prefs[Keys.ENERGY_STATE]?.let { gson.fromJson(it, EnergyState::class.java) }
            ?: EnergyState()
    }

    val aiModelStatusFlow: Flow<AiModelStatus> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_MODEL_STATUS]?.let { gson.fromJson(it, AiModelStatus::class.java) }
            ?: AiModelStatus()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { it[Keys.USER_PROFILE] = gson.toJson(profile) }
    }

    suspend fun saveEnergyState(state: EnergyState) {
        context.dataStore.edit { it[Keys.ENERGY_STATE] = gson.toJson(state) }
    }

    suspend fun saveAiModelStatus(status: AiModelStatus) {
        context.dataStore.edit { it[Keys.AI_MODEL_STATUS] = gson.toJson(status) }
    }

    suspend fun saveLearningPath(path: LearningPath) {
        val key = stringPreferencesKey("${Keys.LEARNING_PATH_PREFIX}${path.subjectId}")
        context.dataStore.edit { it[key] = gson.toJson(path) }
    }

    fun learningPathFlow(subjectId: String): Flow<LearningPath?> = context.dataStore.data.map { prefs ->
        val key = stringPreferencesKey("${Keys.LEARNING_PATH_PREFIX}$subjectId")
        prefs[key]?.let { gson.fromJson(it, LearningPath::class.java) }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
