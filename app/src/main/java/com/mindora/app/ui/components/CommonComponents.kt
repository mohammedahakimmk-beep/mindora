package com.mindora.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindora.app.R
import com.mindora.app.data.models.EnergyState
import com.mindora.app.data.models.Question
import com.mindora.app.data.models.Topic
import com.mindora.app.ui.theme.ConstellationLine
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindoraTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    energy: EnergyState? = null,
    energyCountdown: String = "",
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, color = WarmSand) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Go back" }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = WarmSand)
                }
            }
        },
        actions = {
            if (energy != null) {
                EnergyBadge(energy, energyCountdown)
                Spacer(Modifier.width(8.dp))
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NightSky)
    )
}

@Composable
fun EnergyBadge(energy: EnergyState, countdown: String) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .padding(end = 8.dp)
            .semantics { contentDescription = "Energy ${energy.current} of ${energy.max}" }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = StarGold, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("${energy.current}/${energy.max}", color = StarGold, style = MaterialTheme.typography.labelLarge)
        }
        if (countdown.isNotEmpty()) {
            Text(countdown, color = WarmSand.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ForgeButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Ember, contentColor = NightSky)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun ForgeOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) { Text(text, color = WarmSand) }
}

@Composable
fun ForgeTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = WarmSand,
            unfocusedTextColor = WarmSand,
            focusedBorderColor = TealLight,
            unfocusedBorderColor = WarmSand.copy(alpha = 0.4f),
            focusedLabelColor = TealLight,
            unfocusedLabelColor = WarmSand.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun LoadingScreen(message: String = "Loading…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = TealLight)
            Spacer(Modifier.height(16.dp))
            Text(message, color = WarmSand)
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = WarmSand, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            ForgeButton(stringResource(R.string.retry), modifier = Modifier.width(200.dp), onClick = onRetry)
        }
    }
}

@Composable
fun ConstellationMap(
    topics: List<Topic>,
    completedIds: Set<String>,
    currentId: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(300.dp).semantics { contentDescription = "Constellation learning map" }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0 until topics.size - 1) {
                val from = topics[i]
                val to = topics[i + 1]
                drawLine(
                    color = ConstellationLine.copy(alpha = 0.5f),
                    start = Offset(from.constellationX * size.width, from.constellationY * size.height),
                    end = Offset(to.constellationX * size.width, to.constellationY * size.height),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
        }
        topics.forEach { topic ->
            val isCompleted = completedIds.contains(topic.id)
            val isCurrent = topic.id == currentId
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = (topic.constellationX * 280).dp,
                            top = (topic.constellationY * 260).dp
                        )
                        .size(if (isCurrent) 40.dp else 32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> TealLight
                                isCurrent -> Ember
                                else -> SurfaceDark
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = topic.title,
                        tint = if (isCompleted || isCurrent) NightSky else WarmSand.copy(0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    selectedAnswer: String,
    onAnswer: (String) -> Unit,
    onSubmit: () -> Unit,
    onHint: () -> Unit,
    feedback: String?,
    isCorrect: Boolean?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(question.prompt, color = WarmSand, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            when (question.type) {
                com.mindora.app.data.models.QuestionType.MULTIPLE_CHOICE,
                com.mindora.app.data.models.QuestionType.TRUE_FALSE -> {
                    question.options.forEach { option ->
                        val selected = selectedAnswer == option
                        OutlinedButton(
                            onClick = { onAnswer(option) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) TealLight.copy(0.2f) else Color.Transparent
                            )
                        ) { Text(option, color = WarmSand) }
                    }
                }
                com.mindora.app.data.models.QuestionType.NUMERIC,
                com.mindora.app.data.models.QuestionType.FILL_BLANK -> {
                    ForgeTextField(selectedAnswer, onAnswer, "Your answer")
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForgeOutlinedButton(stringResource(R.string.hint), modifier = Modifier.weight(1f), onClick = onHint)
                ForgeButton(stringResource(R.string.submit), modifier = Modifier.weight(1f), enabled = selectedAnswer.isNotBlank(), onClick = onSubmit)
            }
            feedback?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    it,
                    color = when (isCorrect) {
                        true -> com.mindora.app.ui.theme.SuccessGreen
                        false -> com.mindora.app.ui.theme.ErrorRed
                        null -> WarmSand
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun XpBar(xp: Int, level: Int) {
    val progress = (xp % 100) / 100f
    Column {
        Text(stringResource(R.string.level_label, level), color = StarGold, style = MaterialTheme.typography.labelLarge)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = StarGold,
            trackColor = SurfaceDark
        )
        Text("$xp XP", color = WarmSand.copy(0.7f), style = MaterialTheme.typography.labelSmall)
    }
}
