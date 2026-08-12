package com.mindora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindora.app.BuildConfig
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.SurfaceDark
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand

@Composable
fun AiAboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { MindoraTopBar("How AI Works in Mindora", onBack) },
        containerColor = NightSky
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Star Guide — on-device AI tutor", style = MaterialTheme.typography.headlineSmall, color = TealLight)
            Text(
                "Mindora uses an on-device teaching engine so personalization can work without sending every question to the cloud.",
                color = WarmSand
            )

            AiInfoCard(
                "1. Personalized teaching",
                "Your grade, goals, experience, and placement score shape a learning path. The AI adjusts which topics unlock next and how hard practice feels."
            )
            AiInfoCard(
                "2. Hints while you practice",
                "During lessons and quizzes, Star Guide generates step-by-step hints from the lesson topic and question type — not just “wrong/right”."
            )
            AiInfoCard(
                "3. Explanations & feedback",
                "After you answer, the AI explains why an answer is correct or incorrect using the curriculum knowledge base for that topic."
            )
            AiInfoCard(
                "4. Practice generation",
                "The AI can remix curriculum templates into new practice items (numbers/options varied) so drills don’t feel identical every time."
            )
            AiInfoCard(
                "5. Difficulty adjustment",
                "If you streak correct answers, difficulty trends up; if you struggle, it eases — keeping the forge challenging but fair."
            )
            AiInfoCard(
                "6. Chat Q&A (offline-first)",
                "Ask Star Guide questions about math. It retrieves related curriculum facts (RAG-style) on device. Installing the model pack unlocks richer responses."
            )
            AiInfoCard(
                "7. Model pack on your phone",
                "In Star Guide → Install Enhanced Model. This installs a local tutor pack on the device. Core tutoring still works with the built-in knowledge engine if the pack isn’t installed."
            )

            Text("Privacy note", color = StarGold, style = MaterialTheme.typography.titleMedium)
            Text(
                "Teaching logic is designed to run on-device. Account sync (progress, energy, profile) still uses Firebase so you can keep progress across devices.",
                color = WarmSand.copy(0.85f)
            )
            Spacer(Modifier.height(8.dp))
            Text("App version ${BuildConfig.VERSION_NAME}", color = Ember, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AiInfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = TealLight, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(body, color = WarmSand, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
