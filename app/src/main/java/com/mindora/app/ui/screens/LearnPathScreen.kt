package com.mindora.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindora.app.MindoraApp
import com.mindora.app.data.models.EnergyState
import com.mindora.app.data.models.LearningPath
import com.mindora.app.data.models.Topic
import com.mindora.app.ui.components.ConstellationMap
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand

@Composable
fun LearnPathScreen(
    subjectId: String,
    energy: EnergyState,
    energyCountdown: String,
    learnerGrade: String? = null,
    onBack: () -> Unit,
    onStartLesson: (String, String) -> Unit,
    onStartPractice: (String) -> Unit,
    onStartAssessment: (String) -> Unit
) {
    val app = MindoraApp.instance
    var path by remember { mutableStateOf<LearningPath?>(null) }

    LaunchedEffect(subjectId, learnerGrade) {
        val gradeTopics = app.mathCurriculum.getAllTopics(learnerGrade).map { it.id }
        path = app.progressRepository.getLearningPath(subjectId)
            ?: LearningPath(
                subjectId = subjectId,
                topicIds = gradeTopics.ifEmpty { app.mathCurriculum.getAllTopics().map { it.id } }
            )
        // If saved path has no topics for this grade, rebuild.
        val current = path
        if (current != null && current.topicIds.none { id -> gradeTopics.contains(id) } && gradeTopics.isNotEmpty()) {
            val rebuilt = current.copy(topicIds = gradeTopics, currentTopicIndex = 0)
            path = rebuilt
            app.progressRepository.saveLearningPath(rebuilt)
        }
    }

    val learningPath = path
    val topics = learningPath?.topicIds?.mapNotNull { app.mathCurriculum.getTopic(it) }
        ?.ifEmpty { app.mathCurriculum.getAllTopics(learnerGrade) }
        ?: app.mathCurriculum.getAllTopics(learnerGrade)
    val completed = learningPath?.completedTopicIds?.toSet() ?: emptySet()
    val currentTopicId = learningPath?.topicIds?.getOrNull(learningPath.currentTopicIndex)
        ?: topics.firstOrNull()?.id
    val lessonCount = topics.sumOf { it.lessonIds.size }

    Scaffold(
        topBar = { MindoraTopBar("Forge Path", onBack, energy, energyCountdown) },
        containerColor = NightSky
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Grade band: ${learnerGrade ?: "All"} · $lessonCount lessons across ${topics.size} topics",
                    color = WarmSand.copy(0.7f),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
                Text("Your Constellation Map", style = MaterialTheme.typography.titleLarge, color = TealLight)
                ConstellationMap(topics, completed, currentTopicId)
            }
            items(topics) { topic ->
                TopicPathCard(
                    topic = topic,
                    isCompleted = completed.contains(topic.id),
                    isCurrent = topic.id == currentTopicId,
                    onLesson = {
                        val lessonId = topic.lessonIds.firstOrNull() ?: return@TopicPathCard
                        onStartLesson(topic.id, lessonId)
                    },
                    onPractice = { onStartPractice(topic.id) },
                    onAssessment = { onStartAssessment(topic.id) }
                )
            }
        }
    }
}

@Composable
fun TopicPathCard(
    topic: Topic,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onLesson: () -> Unit,
    onPractice: () -> Unit,
    onAssessment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) SurfaceDark.copy(alpha = 1f) else SurfaceDark.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isCompleted) TealLight else if (isCurrent) Ember else WarmSand.copy(0.4f)
                )
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(topic.title, color = WarmSand, style = MaterialTheme.typography.titleMedium)
                    Text(topic.description, color = WarmSand.copy(0.6f), style = MaterialTheme.typography.bodySmall)
                }
                Text("${topic.xpReward} XP", color = StarGold, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PathAction("Lesson", onLesson, Modifier.weight(1f))
                PathAction("Practice", onPractice, Modifier.weight(1f))
                PathAction("Test", onAssessment, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PathAction(label: String, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = TealLight.copy(0.15f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            color = TealLight,
            style = MaterialTheme.typography.labelMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
