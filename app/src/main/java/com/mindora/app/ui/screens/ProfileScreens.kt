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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mindora.app.MindoraApp
import com.mindora.app.R
import com.mindora.app.data.models.Achievement
import com.mindora.app.data.models.UserProfile
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.ForgeOutlinedButton
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.components.XpBar
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand

@Composable
fun ProfileScreen(profile: UserProfile?, onBack: () -> Unit, onSignOut: () -> Unit) {
    Scaffold(
        topBar = { MindoraTopBar(stringResource(R.string.profile_title), onBack) },
        containerColor = NightSky
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile?.displayName?.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = NightSky
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(profile?.displayName ?: "Apprentice", style = MaterialTheme.typography.headlineMedium, color = WarmSand)
            Text(profile?.email ?: "", color = WarmSand.copy(0.6f))
            Spacer(Modifier.height(24.dp))
            profile?.let { XpBar(it.xp, it.level) }
            Spacer(Modifier.height(16.dp))
            ProfileStatRow("Grade", profile?.grade ?: "—")
            ProfileStatRow("Experience", profile?.experienceLevel ?: "—")
            ProfileStatRow("Difficulty", profile?.preferredDifficulty ?: "—")
            ProfileStatRow("Streak", "${profile?.streak ?: 0} days")
            Spacer(Modifier.weight(1f))
            ForgeOutlinedButton(stringResource(R.string.sign_out), onClick = onSignOut)
        }
    }
}

@Composable
fun ProfileStatRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = WarmSand.copy(0.6f))
        Text(value, color = WarmSand)
    }
}

@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    var achievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }

    LaunchedEffect(Unit) {
        achievements = MindoraApp.instance.progressRepository.getAchievements()
    }

    Scaffold(
        topBar = { MindoraTopBar(stringResource(R.string.achievements_title), onBack) },
        containerColor = NightSky
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievement -> AchievementCard(achievement) }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked) SurfaceDark else SurfaceDark.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(achievement.icon, style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.padding(start = 16.dp).weight(1f)) {
                Text(achievement.title, color = if (achievement.unlocked) WarmSand else WarmSand.copy(0.4f))
                Text(achievement.description, color = WarmSand.copy(0.5f), style = MaterialTheme.typography.bodySmall)
            }
            if (achievement.unlocked) {
                Text("+${achievement.xpBonus} XP", color = StarGold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onAiTutor: () -> Unit,
    onAiAbout: () -> Unit = {}
) {
    Scaffold(
        topBar = { MindoraTopBar(stringResource(R.string.settings_title), onBack) },
        containerColor = NightSky
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            SettingsItem("Notifications", onNotifications)
            SettingsItem("Star Guide AI", onAiTutor)
            SettingsItem("How AI Works", onAiAbout)
            SettingsItem("About Mindora", onAiAbout)
            Spacer(Modifier.height(16.dp))
            Text(
                "Version ${com.mindora.app.BuildConfig.VERSION_NAME}",
                color = WarmSand.copy(0.4f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(title, modifier = Modifier.padding(16.dp), color = WarmSand)
    }
}

@Composable
fun NotificationsPrefsScreen(onBack: () -> Unit) {
    var dailyReminder by remember { mutableStateOf(true) }
    var streakAlerts by remember { mutableStateOf(true) }
    var energyAlerts by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MindoraTopBar(stringResource(R.string.notifications_title), onBack) },
        containerColor = NightSky
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            NotificationToggle("Daily learning reminder", dailyReminder) { dailyReminder = it }
            NotificationToggle("Streak alerts", streakAlerts) { streakAlerts = it }
            NotificationToggle("Energy refill alerts", energyAlerts) { energyAlerts = it }
        }
    }
}

@Composable
fun NotificationToggle(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = WarmSand)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = Ember, checkedTrackColor = Ember.copy(0.3f))
        )
    }
}

