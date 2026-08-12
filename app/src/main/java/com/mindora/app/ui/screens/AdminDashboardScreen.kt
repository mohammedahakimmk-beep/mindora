package com.mindora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindora.app.MindoraApp
import com.mindora.app.ui.components.MindoraTopBar
import com.google.firebase.database.FirebaseDatabase
import com.mindora.app.BuildConfig
import kotlinx.coroutines.tasks.await
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand

@Composable
fun AdminDashboardScreen(onBack: () -> Unit, isAdmin: Boolean) {
    if (!isAdmin) {
        Scaffold(topBar = { MindoraTopBar("Access Denied", onBack) }, containerColor = NightSky) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Admin access required.", color = Ember, style = MaterialTheme.typography.headlineMedium)
            }
        }
        return
    }

    var stats by remember { mutableStateOf(AdminStats()) }

    LaunchedEffect(Unit) {
        stats = loadAdminStats()
    }

    Scaffold(
        topBar = { MindoraTopBar("Admin Dashboard", onBack) },
        containerColor = NightSky
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Mindora Admin", style = MaterialTheme.typography.headlineMedium, color = TealLight)
                Text("Firebase: mindora-b2550", color = WarmSand.copy(0.6f), style = MaterialTheme.typography.bodySmall)
            }
            item { AdminStatCard("Total Users", stats.totalUsers.toString()) }
            item { AdminStatCard("Active Today", stats.activeToday.toString()) }
            item { AdminStatCard("Math Topics", stats.mathTopics.toString()) }
            item { AdminStatCard("Total Lessons", stats.totalLessons.toString()) }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Admin Actions", color = WarmSand, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("• Seed admin role at /admins/{uid}", color = WarmSand.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                        Text("• RTDB rules enforce admin email", color = WarmSand.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                        Text("• Deploy rules: firebase deploy --only database", color = WarmSand.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Curriculum Overview", color = StarGold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        MindoraApp.instance.mathCurriculum.getAllTopics().forEach { topic ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(topic.title, color = WarmSand, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text(topic.difficulty, color = TealLight, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            Text(label, color = WarmSand.copy(0.7f), modifier = Modifier.weight(1f))
            Text(value, color = StarGold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

data class AdminStats(
    val totalUsers: Int = 0,
    val activeToday: Int = 0,
    val mathTopics: Int = 7,
    val totalLessons: Int = 7
)

private suspend fun loadAdminStats(): AdminStats {
    val app = MindoraApp.instance
    return try {
        val snapshot = FirebaseDatabase.getInstance(BuildConfig.RTDB_URL)
            .reference.child("users").get().await()
        AdminStats(
            totalUsers = snapshot.children.count(),
            activeToday = snapshot.children.count(),
            mathTopics = app.mathCurriculum.getAllTopics().size,
            totalLessons = app.mathCurriculum.load().lessons.size
        )
    } catch (_: Exception) {
        AdminStats(
            mathTopics = app.mathCurriculum.getAllTopics().size,
            totalLessons = app.mathCurriculum.load().lessons.size
        )
    }
}
