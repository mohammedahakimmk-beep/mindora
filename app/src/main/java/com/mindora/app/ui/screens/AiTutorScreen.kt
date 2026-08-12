package com.mindora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindora.app.data.models.ChatMessage
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.ForgeOutlinedButton
import com.mindora.app.ui.components.ForgeTextField
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import com.mindora.app.ui.viewmodel.AiTutorViewModel

@Composable
fun AiTutorScreen(onBack: () -> Unit, topicId: String? = null, viewModel: AiTutorViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(topicId) { viewModel.setTopic(topicId) }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = { MindoraTopBar("Star Guide", onBack) },
        containerColor = NightSky
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            ModelStatusCard(state.modelStatus, viewModel::installModel, viewModel::uninstallModel)
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages) { msg -> ChatBubble(msg) }
                if (state.isThinking) {
                    item {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.height(16.dp), color = TealLight, strokeWidth = 2.dp)
                            Text(" Thinking…", color = WarmSand.copy(0.6f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ForgeTextField(
                    state.input,
                    viewModel::updateInput,
                    "Ask the Star Guide…",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = viewModel::sendMessage) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Ember)
                }
            }
        }
    }
}

@Composable
fun ModelStatusCard(
    status: com.mindora.app.data.models.AiModelStatus,
    onInstall: () -> Unit,
    onUninstall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (status.installed) "Star Guide Model v${status.version}" else "Offline Tutor Active",
                color = WarmSand,
                style = MaterialTheme.typography.titleSmall
            )
            if (status.downloading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { status.downloadProgress }, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(8.dp))
            if (status.installed) {
                ForgeOutlinedButton("Uninstall Model", onClick = onUninstall)
            } else if (!status.downloading) {
                ForgeButton("Install Enhanced Model", onClick = onInstall)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Ember.copy(0.3f) else SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                message.content,
                modifier = Modifier.padding(12.dp),
                color = WarmSand,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
