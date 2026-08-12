package com.mindora.app.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mindora.app.R
import com.mindora.app.data.models.StageType
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.ForgeOutlinedButton
import com.mindora.app.ui.components.LoadingScreen
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.components.QuestionCard
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import com.mindora.app.ui.viewmodel.LessonViewModel

@Composable
fun LessonScreen(
    lessonId: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: LessonViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lesson = state.lesson

    androidx.compose.runtime.LaunchedEffect(lessonId) { viewModel.loadLesson(lessonId) }

    if (lesson == null) { LoadingScreen(); return }

    if (state.completed) {
        Scaffold(topBar = { MindoraTopBar("Lesson Complete", onBack) }, containerColor = NightSky) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("🌟 Star Forged!", style = MaterialTheme.typography.headlineLarge, color = TealLight)
                Spacer(Modifier.height(8.dp))
                Text("You've completed ${lesson.title}", color = WarmSand)
                Spacer(Modifier.height(24.dp))
                ForgeButton("Continue", onClick = onComplete)
            }
        }
        return
    }

    val stage = lesson.stages.getOrNull(state.currentStageIndex) ?: return
    val progress = (state.currentStageIndex + 1f) / lesson.stages.size

    Scaffold(
        topBar = { MindoraTopBar(lesson.title, onBack) },
        containerColor = NightSky
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = TealLight)
            Text(
                "Stage ${state.currentStageIndex + 1}/${lesson.stages.size}: ${stage.title}",
                color = WarmSand.copy(0.7f),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                when (stage.type) {
                    StageType.CONTENT -> {
                        Text(stage.content, color = WarmSand, style = MaterialTheme.typography.bodyLarge)
                    }
                    StageType.VIDEO -> {
                        Text(stage.content, color = WarmSand, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        VideoPlayer(stage.videoUrl.ifBlank { lesson.videoUrl })
                    }
                    StageType.EXAMPLE -> {
                        stage.examples.forEach { ex ->
                            Text("Problem: ${ex.problem}", color = WarmSand, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            ex.steps.forEachIndexed { i, step ->
                                Text("${i + 1}. $step", color = WarmSand.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Answer: ${ex.solution}", color = TealLight, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    StageType.PRACTICE, StageType.QUIZ -> {
                        stage.questions.forEach { q ->
                            QuestionCard(
                                question = q,
                                selectedAnswer = state.answers[q.id] ?: "",
                                onAnswer = { viewModel.answer(q.id, it) },
                                onSubmit = { viewModel.checkAnswer(q) },
                                onHint = { viewModel.getHint(q) },
                                feedback = if (state.answers.containsKey(q.id)) state.feedback else null,
                                isCorrect = state.isCorrect
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
                if (state.currentStageIndex > 0) {
                    ForgeOutlinedButton(stringResource(R.string.back), modifier = Modifier.weight(1f).padding(end = 8.dp), onClick = viewModel::prevStage)
                }
                ForgeButton(stringResource(R.string.next), modifier = Modifier.weight(1f), onClick = viewModel::nextStage)
            }
        }
    }
}

@Composable
fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}
