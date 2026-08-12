package com.mindora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.ForgeOutlinedButton
import com.mindora.app.ui.components.LoadingScreen
import com.mindora.app.ui.components.MindoraTopBar
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.NightSky
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import com.mindora.app.ui.viewmodel.OnboardingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit, viewModel: OnboardingViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val goals = listOf("Build math skills", "Prepare for exams", "Have fun learning", "Catch up in school", "Challenge myself")
    val grades = listOf("K-2", "3-5", "6-8", "9-12", "College", "Adult")
    val experiences = listOf("beginner", "intermediate", "advanced")
    val difficulties = listOf("easy", "medium", "hard")

    if (state.isSaving) { LoadingScreen("Saving your path…"); return }

    Scaffold(
        topBar = { MindoraTopBar("Chart Your Course") },
        containerColor = NightSky
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Step ${state.step + 1} of ${viewModel.totalSteps}", color = Ember, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(16.dp))

            when (state.step) {
                0 -> {
                    Text("How old are you?", style = MaterialTheme.typography.titleLarge, color = WarmSand)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = state.data.age.toFloat().coerceAtLeast(5f),
                        onValueChange = { viewModel.updateAge(it.toInt()) },
                        valueRange = 5f..18f,
                        steps = 13
                    )
                    Text("${state.data.age} years old", color = TealLight)
                }
                1 -> {
                    Text("What grade are you in?", style = MaterialTheme.typography.titleLarge, color = WarmSand)
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        grades.forEach { grade ->
                            FilterChip(
                                selected = state.data.grade == grade,
                                onClick = { viewModel.updateGrade(grade) },
                                label = { Text(grade) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealLight)
                            )
                        }
                    }
                }
                2 -> {
                    Text("What are your goals?", style = MaterialTheme.typography.titleLarge, color = WarmSand)
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        goals.forEach { goal ->
                            FilterChip(
                                selected = state.data.goals.contains(goal),
                                onClick = { viewModel.toggleGoal(goal) },
                                label = { Text(goal) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealLight)
                            )
                        }
                    }
                }
                3 -> {
                    Text("Your experience level?", style = MaterialTheme.typography.titleLarge, color = WarmSand)
                    Spacer(Modifier.height(12.dp))
                    experiences.forEach { exp ->
                        ForgeOutlinedButton(
                            exp.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) { viewModel.updateExperience(exp) }
                    }
                }
                4 -> {
                    Text("Preferred difficulty?", style = MaterialTheme.typography.titleLarge, color = WarmSand)
                    Spacer(Modifier.height(12.dp))
                    difficulties.forEach { diff ->
                        ForgeOutlinedButton(
                            diff.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) { viewModel.updateDifficulty(diff) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Daily goal (minutes): ${state.data.dailyGoalMinutes}", color = WarmSand)
                    Slider(
                        value = state.data.dailyGoalMinutes.toFloat(),
                        onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 10
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
                if (state.step > 0) {
                    ForgeOutlinedButton("Back", modifier = Modifier.weight(1f).padding(end = 8.dp), onClick = viewModel::prevStep)
                }
                if (state.step < viewModel.totalSteps - 1) {
                    ForgeButton("Next", modifier = Modifier.weight(1f), onClick = viewModel::nextStep)
                } else {
                    ForgeButton("Begin Placement", modifier = Modifier.weight(1f)) { viewModel.complete(onComplete) }
                }
            }
        }
    }
}
