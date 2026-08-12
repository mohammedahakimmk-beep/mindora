package com.mindora.app.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindora.app.MindoraApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel : ViewModel() {
    private val manager = MindoraApp.instance.updateManager
    private val _state = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    fun checkAndAutoUpdate() {
        viewModelScope.launch {
            _state.value = UpdateUiState.Checking
            try {
                val release = manager.checkForUpdate()
                if (release == null) {
                    _state.value = UpdateUiState.UpToDate
                    return@launch
                }
                _state.value = UpdateUiState.UpdateAvailable(release)
                // No "Do you want to update?" — start download immediately.
                startDownload(release)
            } catch (e: Exception) {
                _state.value = UpdateUiState.Error(e.message ?: "Update check failed")
            }
        }
    }

    fun retry() {
        val current = _state.value
        val release = when (current) {
            is UpdateUiState.Error -> current.release
            is UpdateUiState.UpdateAvailable -> current.release
            else -> null
        }
        if (release != null) startDownload(release) else checkAndAutoUpdate()
    }

    private fun startDownload(release: AppReleaseInfo) {
        viewModelScope.launch {
            try {
                if (!manager.canRequestPackageInstalls()) {
                    manager.openUnknownSourcesSettings()
                    _state.value = UpdateUiState.Error(
                        "Allow Mindora to install updates, then tap Retry.",
                        release
                    )
                    return@launch
                }
                _state.value = UpdateUiState.Downloading(0, release)
                val file = manager.downloadApk(release) { pct ->
                    _state.value = UpdateUiState.Downloading(pct, release)
                }
                _state.value = UpdateUiState.ReadyToInstall(file.absolutePath, release)
                _state.value = UpdateUiState.Installing(release)
                manager.installApk(file)
            } catch (e: Exception) {
                _state.value = UpdateUiState.Error(e.message ?: "Download failed", release)
            }
        }
    }
}
