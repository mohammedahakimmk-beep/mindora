package com.mindora.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.LoadingScreen
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.components.QuestionCard
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import com.mindora.app.ui.viewmodel.PlacementViewModel

@Composable
fun PlacementScreen(onComplete: () -> Unit, viewModel: PlacementViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isSubmitting) { LoadingScreen("Calculating your path…"); return }

    if (state.showResult) {
        Scaffold(topBar = { MindoraTopBar("Placement Complete") }, containerColor = NightSky) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Your Forge Score", style = MaterialTheme.typography.headlineLarge, color = StarGold)
                Spacer(Modifier.height(8.dp))
                Text("${state.score}%", style = MaterialTheme.typography.displayLarge, color = TealLight)
                Spacer(Modifier.height(16.dp))
                Text(
                    when {
                        state.score >= 80 -> "Excellent! Your constellation path starts at advanced topics."
                        state.score >= 50 -> "Good foundation! We'll build on your existing skills."
                        else -> "Let's start from the basics and forge strong foundations."
                    },
                    color = WarmSand,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(32.dp))
                ForgeButton("View My Path", onClick = onComplete)
            }
        }
        return
    }

    val question = state.questions.getOrNull(state.currentIndex) ?: return
    val progress = if (state.questions.isNotEmpty()) (state.currentIndex + 1f) / state.questions.size else 0f

    Scaffold(
        topBar = { MindoraTopBar("Constellation Placement") },
        containerColor = NightSky
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Question ${state.currentIndex + 1} of ${state.questions.size}", color = WarmSand.copy(0.7f))
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                color = TealLight
            )
            Spacer(Modifier.height(24.dp))
            QuestionCard(
                question = question,
                selectedAnswer = state.answers[question.id] ?: "",
                onAnswer = { viewModel.answer(question.id, it) },
                onSubmit = {
                    if (state.currentIndex < state.questions.size - 1) viewModel.next()
                    else viewModel.submit(onComplete)
                },
                onHint = {},
                feedback = null,
                isCorrect = null
            )
        }
    }
}
