package com.mindora.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mindora.app.MindoraApp
import com.mindora.app.R
import com.mindora.app.data.models.EnergyState
import com.mindora.app.data.models.Subject
import com.mindora.app.data.models.UserProfile
import com.mindora.app.ui.components.ConstellationMap
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.components.XpBar
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profile: UserProfile?,
    energy: EnergyState,
    energyCountdown: String,
    isAdmin: Boolean,
    onNavigateToSubjects: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToAiTutor: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToLearnPath: (String) -> Unit
) {
    val subjects = runCatching { MindoraApp.instance.mathCurriculum.getAllSubjects() }.getOrDefault(emptyList())
    val mathTopics = runCatching { MindoraApp.instance.mathCurriculum.getAllTopics() }.getOrDefault(emptyList())

    Scaffold(
        topBar = {
            MindoraTopBar(
                title = "Mindora",
                energy = energy,
                energyCountdown = energyCountdown,
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = WarmSand)
                    }
                }
            )
        },
        containerColor = NightSky
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.home_greeting, profile?.displayName ?: "Apprentice"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = WarmSand
                )
                Spacer(Modifier.height(8.dp))
                profile?.let { XpBar(it.xp, it.level) }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.streak_label, profile?.streak ?: 0),
                    color = Ember,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            item {
                Text("Your Constellation", style = MaterialTheme.typography.titleLarge, color = TealLight)
                ConstellationMap(mathTopics, emptySet(), mathTopics.firstOrNull()?.id)
            }
            item {
                ForgeButton(stringResource(R.string.continue_learning)) {
                    onNavigateToLearnPath("math")
                }
            }
            item {
                Text("Subjects", style = MaterialTheme.typography.titleLarge, color = TealLight)
            }
            items(subjects) { subject ->
                SubjectCard(subject, onClick = {
                    if (subject.available) onNavigateToSubjects() else {}
                })
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickNavCard("Star Guide", "🤖", Modifier.weight(1f), onNavigateToAiTutor)
                    QuickNavCard("Achievements", "🏆", Modifier.weight(1f), onNavigateToAchievements)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickNavCard("Profile", "👤", Modifier.weight(1f), onNavigateToProfile)
                    if (isAdmin) {
                        QuickNavCard("Admin", "⚙️", Modifier.weight(1f), onNavigateToAdmin)
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCard(subject: Subject, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = subject.available, onClick = onClick)
            .alpha(if (subject.available) 1f else 0.5f),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(subject.icon, style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.padding(start = 16.dp).weight(1f)) {
                Text(subject.name, color = WarmSand, style = MaterialTheme.typography.titleMedium)
                Text(subject.description, color = WarmSand.copy(0.6f), style = MaterialTheme.typography.bodySmall)
            }
            if (!subject.available) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = WarmSand.copy(0.4f))
            } else {
                Text("${subject.topicCount} topics", color = StarGold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun QuickNavCard(title: String, emoji: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Text(title, color = WarmSand, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectHubScreen(
    energy: EnergyState,
    energyCountdown: String,
    onBack: () -> Unit,
    onSelectSubject: (String) -> Unit
) {
    val subjects = MindoraApp.instance.mathCurriculum.getAllSubjects()
    Scaffold(
        topBar = { MindoraTopBar("Subject Hub", onBack, energy, energyCountdown) },
        containerColor = NightSky
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(subjects) { subject ->
                SubjectCard(subject) {
                    if (subject.available) onSelectSubject(subject.id)
                }
            }
        }
    }
}
