package com.mindora.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mindora.app.BuildConfig
import com.mindora.app.data.local.DataStoreManager
import com.mindora.app.data.models.EnergyState
import com.mindora.app.energy.EnergyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class EnergyRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.RTDB_URL),
    private val dataStore: DataStoreManager,
    private val energyManager: EnergyManager
) {
    val energyFlow: Flow<EnergyState> = dataStore.energyStateFlow

    suspend fun getEnergyState(): EnergyState {
        val local = dataStore.energyStateFlow.first()
        val reset = energyManager.checkAndResetIfNeeded(local)
        if (reset != local) {
            saveEnergyState(reset)
        }
        return reset
    }

    suspend fun syncFromRemote(): EnergyState {
        val uid = auth.currentUser?.uid ?: return getEnergyState()
        return try {
            val snapshot = database.reference.child("energy").child(uid).get().await()
            val remote = snapshot.getValue(EnergyState::class.java) ?: EnergyState()
            val reset = energyManager.checkAndResetIfNeeded(remote)
            dataStore.saveEnergyState(reset)
            reset
        } catch (_: Exception) {
            getEnergyState()
        }
    }

    suspend fun consumeEnergy(amount: Int = 1): Result<EnergyState> {
        val current = getEnergyState()
        if (current.current < amount) {
            return Result.failure(IllegalStateException("Not enough energy"))
        }
        val updated = current.copy(
            current = current.current - amount,
            lastConsumedAt = System.currentTimeMillis()
        )
        saveEnergyState(updated)
        return Result.success(updated)
    }

    suspend fun saveEnergyState(state: EnergyState) {
        dataStore.saveEnergyState(state)
        val uid = auth.currentUser?.uid ?: return
        database.reference.child("energy").child(uid).setValue(state).await()
    }

    fun getResetCountdownMs(): Long = energyManager.millisUntilNextReset()
}
