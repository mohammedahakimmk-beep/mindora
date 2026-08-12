package com.mindora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.mindora.app.MindoraApp
import com.mindora.app.data.models.EnergyState
import com.mindora.app.data.models.UserProfile
import com.mindora.app.energy.EnergyManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val isLoading: Boolean = true,
    val user: FirebaseUser? = null,
    val profile: UserProfile? = null,
    val energy: EnergyState = EnergyState(),
    val energyCountdown: String = "",
    val isAdmin: Boolean = false,
    val error: String? = null
)

class AppViewModel : ViewModel() {
    private val app = MindoraApp.instance
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        observeAuth()
        startEnergyCountdown()
    }

    private fun observeAuth() {
        viewModelScope.launch {
            app.authRepository.authState.collect { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
                if (user != null) {
                    loadUserData(user.uid)
                }
            }
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            try {
                app.userRepository.syncProfile(uid)
                app.energyRepository.syncFromRemote()
                val profile = app.userRepository.getProfile(uid)
                val energy = app.energyRepository.getEnergyState()
                val isAdmin = app.userRepository.isAdmin(uid)
                _uiState.update {
                    it.copy(profile = profile, energy = energy, isAdmin = isAdmin)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun startEnergyCountdown() {
        viewModelScope.launch {
            while (true) {
                val ms = app.energyRepository.getResetCountdownMs()
                val formatted = app.energyManager.formatCountdown(ms)
                _uiState.update { it.copy(energyCountdown = formatted) }
                delay(1000)
            }
        }
    }

    fun refreshEnergy() {
        viewModelScope.launch {
            val energy = app.energyRepository.getEnergyState()
            _uiState.update { it.copy(energy = energy) }
        }
    }

    fun consumeEnergy(amount: Int = 1, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = app.energyRepository.consumeEnergy(amount)
            result.onSuccess { state ->
                _uiState.update { it.copy(energy = state) }
                onResult(true)
            }.onFailure {
                onResult(false)
            }
        }
    }

    fun signOut() {
        app.authRepository.signOut()
        viewModelScope.launch { app.dataStore.clearAll() }
    }
}
