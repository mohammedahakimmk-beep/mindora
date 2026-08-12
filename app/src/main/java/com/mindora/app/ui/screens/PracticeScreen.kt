package com.mindora.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindora.app.MindoraApp
import com.mindora.app.data.models.EnergyState
import com.mindora.app.data.models.Question
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.components.QuestionCard
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import kotlinx.coroutines.launch

@Composable
fun PracticeScreen(
    topicId: String,
    energy: EnergyState,
    energyCountdown: String,
    onBack: () -> Unit,
    onComplete: (Int) -> Unit,
    onEnergyDepleted: () -> Unit
) {
    val app = MindoraApp.instance
    val scope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(topicId) {
        val topic = app.mathCurriculum.getTopic(topicId)
        val lessonQuestions = app.mathCurriculum.getLessonsForTopic(topicId)
            .flatMap { it.stages.flatMap { s -> s.questions } }
        val generated = (0..2).mapNotNull {
            app.aiEngine.generatePracticeQuestion(topicId, when (topic?.difficulty) {
                "hard" -> 3; "medium" -> 2; else -> 1
            })
        }
        questions = (lessonQuestions + generated).shuffled().take(5)
    }

    if (finished) {
        Scaffold(topBar = { MindoraTopBar("Practice Complete", onBack) }, containerColor = NightSky) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Score: $correctCount / ${questions.size}", style = MaterialTheme.typography.headlineLarge, color = StarGold)
                Spacer(Modifier.height(16.dp))
                ForgeButton("Done") { onComplete(correctCount) }
            }
        }
        return
    }

    val question = questions.getOrNull(currentIndex) ?: return

    Scaffold(
        topBar = { MindoraTopBar("Practice", onBack, energy, energyCountdown) },
        containerColor = NightSky
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Question ${currentIndex + 1} of ${questions.size}", color = WarmSand.copy(0.7f))
            Spacer(Modifier.height(16.dp))
            QuestionCard(
                question = question,
                selectedAnswer = answers[question.id] ?: "",
                onAnswer = { answers = answers + (question.id to it) },
                onSubmit = {
                    scope.launch {
                        val answer = answers[question.id] ?: ""
                        val correct = app.aiEngine.checkAnswer(question, answer)
                        isCorrect = correct
                        feedback = app.aiEngine.explainAnswer(question, answer, topicId)
                        if (correct) correctCount++
                    }
                },
                onHint = {
                    scope.launch {
                        feedback = app.aiEngine.generateHint(question, topicId)
                    }
                },
                feedback = feedback,
                isCorrect = isCorrect
            )
            Spacer(Modifier.height(16.dp))
            ForgeButton("Next Question") {
                scope.launch {
                    val result = app.energyRepository.consumeEnergy(1)
                    if (result.isFailure) {
                        onEnergyDepleted()
                        return@launch
                    }
                    feedback = null
                    isCorrect = null
                    if (currentIndex < questions.size - 1) {
                        currentIndex++
                    } else {
                        finished = true
                        app.progressRepository.addXp(correctCount * 10)
                    }
                }
            }
        }
    }
}

@Composable
fun AssessmentScreen(
    topicId: String,
    energy: EnergyState,
    energyCountdown: String,
    onBack: () -> Unit,
    onComplete: (Int) -> Unit
) {
    val app = MindoraApp.instance
    val scope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var correctCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    LaunchedEffect(topicId) {
        questions = app.mathCurriculum.getLessonsForTopic(topicId)
            .flatMap { it.stages.filter { s -> s.type == com.mindora.app.data.models.StageType.QUIZ }.flatMap { it.questions } }
            .ifEmpty {
                app.mathCurriculum.getLessonsForTopic(topicId)
                    .flatMap { it.stages.flatMap { s -> s.questions } }
            }
            .take(4)
    }

    if (finished) {
        val pct = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0
        Scaffold(topBar = { MindoraTopBar("Assessment Results", onBack) }, containerColor = NightSky) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Assessment Score", style = MaterialTheme.typography.headlineLarge, color = TealLight)
                Text("$pct%", style = MaterialTheme.typography.displayLarge, color = StarGold)
                Spacer(Modifier.height(16.dp))
                ForgeButton("Continue") { onComplete(pct) }
            }
        }
        return
    }

    val question = questions.getOrNull(currentIndex) ?: return

    Scaffold(
        topBar = { MindoraTopBar("Assessment", onBack, energy, energyCountdown) },
        containerColor = NightSky
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Question ${currentIndex + 1} of ${questions.size}", color = WarmSand.copy(0.7f))
            Spacer(Modifier.height(16.dp))
            QuestionCard(
                question = question,
                selectedAnswer = answers[question.id] ?: "",
                onAnswer = { answers = answers + (question.id to it); submitted = false },
                onSubmit = { submitted = true },
                onHint = {},
                feedback = null,
                isCorrect = null
            )
            Spacer(Modifier.height(16.dp))
            ForgeButton(
                text = if (currentIndex < questions.size - 1) "Next" else "Submit Assessment",
                enabled = submitted
            ) {
                scope.launch {
                    val answer = answers[question.id] ?: ""
                    if (app.aiEngine.checkAnswer(question, answer)) correctCount++
                    if (currentIndex < questions.size - 1) {
                        currentIndex++
                        submitted = false
                    } else {
                        finished = true
                        app.progressRepository.addXp(correctCount * 20)
                        if (correctCount == questions.size) app.progressRepository.unlockAchievement("perfect_score")
                    }
                }
            }
        }
    }
}
