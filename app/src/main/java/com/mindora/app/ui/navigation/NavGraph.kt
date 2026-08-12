package com.mindora.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindora.app.ui.screens.AdminDashboardScreen
import com.mindora.app.ui.screens.AiAboutScreen
import com.mindora.app.ui.screens.AiTutorScreen
import com.mindora.app.ui.screens.AssessmentScreen
import com.mindora.app.ui.screens.AuthScreen
import com.mindora.app.ui.screens.HomeScreen
import com.mindora.app.ui.screens.LearnPathScreen
import com.mindora.app.ui.screens.LessonScreen
import com.mindora.app.ui.screens.NotificationsPrefsScreen
import com.mindora.app.ui.screens.OnboardingScreen
import com.mindora.app.ui.screens.PlacementScreen
import com.mindora.app.ui.screens.PracticeScreen
import com.mindora.app.ui.screens.AchievementsScreen
import com.mindora.app.ui.screens.ProfileScreen
import com.mindora.app.ui.screens.SettingsScreen
import com.mindora.app.ui.screens.SplashScreen
import com.mindora.app.ui.screens.SubjectHubScreen
import com.mindora.app.ui.viewmodel.AppViewModel

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val ONBOARDING = "onboarding"
    const val PLACEMENT = "placement"
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val LEARN_PATH = "learn_path/{subjectId}"
    const val LESSON = "lesson/{topicId}/{lessonId}"
    const val PRACTICE = "practice/{topicId}"
    const val ASSESSMENT = "assessment/{topicId}"
    const val AI_TUTOR = "ai_tutor?topicId={topicId}"
    const val PROFILE = "profile"
    const val ACHIEVEMENTS = "achievements"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val AI_ABOUT = "ai_about"
    const val ADMIN = "admin"

    fun learnPath(subjectId: String) = "learn_path/$subjectId"
    fun lesson(topicId: String, lessonId: String) = "lesson/$topicId/$lessonId"
    fun practice(topicId: String) = "practice/$topicId"
    fun assessment(topicId: String) = "assessment/$topicId"
    fun aiTutor(topicId: String? = null) = "ai_tutor?topicId=${topicId ?: ""}"
}

@Composable
fun MindoraNavGraph(appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val appState by appViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { isLoggedIn ->
                    val dest = when {
                        !isLoggedIn -> Routes.AUTH
                        appState.profile?.onboardingComplete != true -> Routes.ONBOARDING
                        appState.profile?.placementComplete != true -> Routes.PLACEMENT
                        else -> Routes.HOME
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                isLoggedIn = appState.user != null
            )
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    val dest = when {
                        appState.profile?.onboardingComplete != true -> Routes.ONBOARDING
                        appState.profile?.placementComplete != true -> Routes.PLACEMENT
                        else -> Routes.HOME
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.PLACEMENT) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PLACEMENT) {
            PlacementScreen(
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PLACEMENT) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                profile = appState.profile,
                energy = appState.energy,
                energyCountdown = appState.energyCountdown,
                isAdmin = appState.isAdmin,
                onNavigateToSubjects = { navController.navigate(Routes.SUBJECTS) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToAchievements = { navController.navigate(Routes.ACHIEVEMENTS) },
                onNavigateToAiTutor = { navController.navigate(Routes.aiTutor()) },
                onNavigateToAdmin = { navController.navigate(Routes.ADMIN) },
                onNavigateToLearnPath = { navController.navigate(Routes.learnPath(it)) }
            )
        }

        composable(Routes.SUBJECTS) {
            SubjectHubScreen(
                energy = appState.energy,
                energyCountdown = appState.energyCountdown,
                onBack = { navController.popBackStack() },
                onSelectSubject = { navController.navigate(Routes.learnPath(it)) }
            )
        }

        composable(
            Routes.LEARN_PATH,
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
        ) { entry ->
            val subjectId = entry.arguments?.getString("subjectId") ?: "math"
            LearnPathScreen(
                subjectId = subjectId,
                energy = appState.energy,
                energyCountdown = appState.energyCountdown,
                learnerGrade = appState.profile?.grade,
                onBack = { navController.popBackStack() },
                onStartLesson = { topicId, lessonId ->
                    appViewModel.consumeEnergy(2) { success ->
                        if (success) navController.navigate(Routes.lesson(topicId, lessonId))
                    }
                },
                onStartPractice = { navController.navigate(Routes.practice(it)) },
                onStartAssessment = { navController.navigate(Routes.assessment(it)) }
            )
        }

        composable(
            Routes.LESSON,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) { entry ->
            val lessonId = entry.arguments?.getString("lessonId") ?: ""
            LessonScreen(
                lessonId = lessonId,
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }

        composable(
            Routes.PRACTICE,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { entry ->
            val topicId = entry.arguments?.getString("topicId") ?: ""
            PracticeScreen(
                topicId = topicId,
                energy = appState.energy,
                energyCountdown = appState.energyCountdown,
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                onEnergyDepleted = { navController.popBackStack() }
            )
        }

        composable(
            Routes.ASSESSMENT,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { entry ->
            val topicId = entry.arguments?.getString("topicId") ?: ""
            AssessmentScreen(
                topicId = topicId,
                energy = appState.energy,
                energyCountdown = appState.energyCountdown,
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }

        composable(
            Routes.AI_TUTOR,
            arguments = listOf(navArgument("topicId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { entry ->
            val topicId = entry.arguments?.getString("topicId")?.ifBlank { null }
            AiTutorScreen(
                onBack = { navController.popBackStack() },
                topicId = topicId
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                profile = appState.profile,
                onBack = { navController.popBackStack() },
                onSignOut = {
                    appViewModel.signOut()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onAiTutor = { navController.navigate(Routes.aiTutor()) },
                onAiAbout = { navController.navigate(Routes.AI_ABOUT) }
            )
        }

        composable(Routes.AI_ABOUT) {
            AiAboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsPrefsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN) {
            AdminDashboardScreen(
                onBack = { navController.popBackStack() },
                isAdmin = appState.isAdmin
            )
        }
    }
}
