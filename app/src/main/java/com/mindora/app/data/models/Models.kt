package com.mindora.app.data.models

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: String = "learner",
    val age: Int = 0,
    val grade: String = "",
    val goals: List<String> = emptyList(),
    val experienceLevel: String = "beginner",
    val preferredDifficulty: String = "medium",
    val onboardingComplete: Boolean = false,
    val placementComplete: Boolean = false,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastActiveDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val selectedSubjects: List<String> = listOf("math")
)

data class EnergyState(
    val current: Int = 25,
    val max: Int = 25,
    val lastResetUtc: Long = 0L,
    val lastConsumedAt: Long = 0L
)

data class Subject(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val available: Boolean,
    val topicCount: Int = 0
)

data class Topic(
    val id: String,
    val subjectId: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val order: Int,
    val xpReward: Int,
    val energyCost: Int,
    val prerequisites: List<String> = emptyList(),
    val constellationX: Float = 0.5f,
    val constellationY: Float = 0.5f,
    val lessonIds: List<String> = emptyList()
)

data class Lesson(
    val id: String,
    val topicId: String,
    val title: String,
    val description: String,
    val stages: List<LessonStage> = emptyList(),
    val videoUrl: String = "",
    val videoTitle: String = ""
)

enum class StageType { CONTENT, VIDEO, EXAMPLE, PRACTICE, QUIZ }

data class LessonStage(
    val id: String,
    val type: StageType,
    val title: String,
    val content: String = "",
    val videoUrl: String = "",
    val examples: List<Example> = emptyList(),
    val questions: List<Question> = emptyList()
)

data class Example(
    val problem: String,
    val solution: String,
    val steps: List<String> = emptyList()
)

enum class QuestionType { MULTIPLE_CHOICE, NUMERIC, FILL_BLANK, TRUE_FALSE }

data class Question(
    val id: String,
    val type: QuestionType,
    val prompt: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String = "",
    val hint: String = "",
    val difficulty: Int = 1
)

data class PlacementResult(
    val subjectId: String,
    val score: Int,
    val totalQuestions: Int,
    val recommendedDifficulty: String,
    val recommendedTopicIds: List<String>,
    val completedAt: Long = System.currentTimeMillis()
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpBonus: Int = 0,
    val unlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

data class LearningPath(
    val subjectId: String,
    val topicIds: List<String>,
    val currentTopicIndex: Int = 0,
    val completedTopicIds: List<String> = emptyList(),
    val difficulty: String = "medium"
)

data class AiModelStatus(
    val installed: Boolean = false,
    val version: String = "",
    val downloadProgress: Float = 0f,
    val downloading: Boolean = false,
    val lastUpdated: Long = 0L
)

data class TopicProgress(
    val topicId: String,
    val completed: Boolean = false,
    val score: Int = 0,
    val attempts: Int = 0,
    val lastAttemptAt: Long = 0L
)

data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class OnboardingData(
    val age: Int = 0,
    val grade: String = "",
    val goals: List<String> = emptyList(),
    val experienceLevel: String = "beginner",
    val preferredDifficulty: String = "medium",
    val dailyGoalMinutes: Int = 15,
    val interests: List<String> = emptyList()
)
