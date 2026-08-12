package com.mindora.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindora.app.BuildConfig
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import com.mindora.app.update.UpdateUiState
import com.mindora.app.update.UpdateViewModel

@Composable
fun ForceUpdateGate(
    updateViewModel: UpdateViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val state by updateViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        updateViewModel.checkAndAutoUpdate()
    }

    Box(Modifier.fillMaxSize()) {
        val blockApp = when (state) {
            is UpdateUiState.UpdateAvailable,
            is UpdateUiState.Downloading,
            is UpdateUiState.ReadyToInstall,
            is UpdateUiState.Installing -> true
            is UpdateUiState.Error -> (state as UpdateUiState.Error).release != null
            else -> false
        }

        if (!blockApp) {
            content()
        } else {
            // Keep a dark backdrop while forcing update.
            Box(Modifier.fillMaxSize().background(NightSky))
        }

        when (val s = state) {
            is UpdateUiState.UpdateAvailable,
            is UpdateUiState.Downloading,
            is UpdateUiState.ReadyToInstall,
            is UpdateUiState.Installing,
            is UpdateUiState.Error -> {
                if (s is UpdateUiState.Error && s.release == null) {
                    // Soft failure: don't block the app.
                } else {
                    ForceUpdateDialog(
                        state = s,
                        onRetry = updateViewModel::retry
                    )
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun ForceUpdateDialog(
    state: UpdateUiState,
    onRetry: () -> Unit
) {
    BackHandler(enabled = true) { /* block back while updating */ }

    Dialog(
        onDismissRequest = { /* non-dismissible */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Updating Mindora",
                style = MaterialTheme.typography.headlineSmall,
                color = TealLight,
                fontWeight = FontWeight.Bold
            )

            val versionLabel = when (state) {
                is UpdateUiState.UpdateAvailable -> state.release.versionName
                is UpdateUiState.Downloading -> state.release.versionName
                is UpdateUiState.ReadyToInstall -> state.release.versionName
                is UpdateUiState.Installing -> state.release.versionName
                is UpdateUiState.Error -> state.release?.versionName
                else -> null
            }

            Text(
                "A newer version is available. Installing automatically — no confirmation needed.",
                color = WarmSand,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Current ${BuildConfig.VERSION_NAME} → $versionLabel",
                color = StarGold,
                style = MaterialTheme.typography.labelLarge
            )

            when (state) {
                is UpdateUiState.UpdateAvailable -> {
                    Text("Preparing download…", color = WarmSand.copy(0.8f))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Ember)
                }
                is UpdateUiState.Downloading -> {
                    Text("Downloading ${state.progress}%", color = WarmSand)
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Ember,
                        trackColor = NightSky
                    )
                }
                is UpdateUiState.ReadyToInstall, is UpdateUiState.Installing -> {
                    Text("Installing update…", color = WarmSand)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Ember)
                }
                is UpdateUiState.Error -> {
                    Text(state.message, color = Ember, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    ForgeButton("Retry update", onClick = onRetry)
                }
                else -> Unit
            }
        }
    }
}
