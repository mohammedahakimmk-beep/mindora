package com.mindora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindora.app.MindoraApp
import com.mindora.app.data.models.ChatMessage
import com.mindora.app.data.models.OnboardingData
import com.mindora.app.data.models.PlacementResult
import com.mindora.app.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isSignUp: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val app = MindoraApp.instance
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(v: String) = _uiState.update { it.copy(email = v) }
    fun updatePassword(v: String) = _uiState.update { it.copy(password = v) }
    fun updateDisplayName(v: String) = _uiState.update { it.copy(displayName = v) }
    fun toggleSignUp() = _uiState.update { it.copy(isSignUp = !it.isSignUp, error = null) }

    fun signInWithEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (state.isSignUp) {
                app.authRepository.signUpWithEmail(state.email, state.password, state.displayName)
            } else {
                app.authRepository.signInWithEmail(state.email, state.password)
            }
            result.onSuccess { onSuccess() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            app.authRepository.signInWithGoogle(idToken)
                .onSuccess { onSuccess() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

data class OnboardingUiState(
    val step: Int = 0,
    val data: OnboardingData = OnboardingData(),
    val isSaving: Boolean = false
)

class OnboardingViewModel : ViewModel() {
    private val app = MindoraApp.instance
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val totalSteps = 5

    fun updateAge(age: Int) = _uiState.update { it.copy(data = it.data.copy(age = age)) }
    fun updateGrade(grade: String) = _uiState.update { it.copy(data = it.data.copy(grade = grade)) }
    fun toggleGoal(goal: String) {
        _uiState.update { state ->
            val goals = state.data.goals.toMutableList()
            if (goals.contains(goal)) goals.remove(goal) else goals.add(goal)
            state.copy(data = state.data.copy(goals = goals))
        }
    }
    fun updateExperience(level: String) = _uiState.update { it.copy(data = it.data.copy(experienceLevel = level)) }
    fun updateDifficulty(diff: String) = _uiState.update { it.copy(data = it.data.copy(preferredDifficulty = diff)) }
    fun updateDailyGoal(minutes: Int) = _uiState.update { it.copy(data = it.data.copy(dailyGoalMinutes = minutes)) }
    fun nextStep() = _uiState.update { it.copy(step = (it.step + 1).coerceAtMost(totalSteps - 1)) }
    fun prevStep() = _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }

    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val uid = app.authRepository.currentUser?.uid ?: return@launch
            app.userRepository.completeOnboarding(uid, _uiState.value.data)
            _uiState.update { it.copy(isSaving = false) }
            onDone()
        }
    }
}

data class PlacementUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val showResult: Boolean = false,
    val score: Int = 0,
    val isSubmitting: Boolean = false
)

class PlacementViewModel : ViewModel() {
    private val app = MindoraApp.instance
    private val _uiState = MutableStateFlow(PlacementUiState())
    val uiState: StateFlow<PlacementUiState> = _uiState.asStateFlow()

    init {
        val questions = app.mathCurriculum.getPlacementQuestions()
        _uiState.update { it.copy(questions = questions) }
    }

    fun answer(questionId: String, answer: String) {
        _uiState.update { it.copy(answers = it.answers + (questionId to answer)) }
    }

    fun next() {
        _uiState.update { state ->
            if (state.currentIndex < state.questions.size - 1) {
                state.copy(currentIndex = state.currentIndex + 1)
            } else state
        }
    }

    fun prev() {
        _uiState.update { state ->
            if (state.currentIndex > 0) state.copy(currentIndex = state.currentIndex - 1) else state
        }
    }

    fun submit(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val state = _uiState.value
            var correct = 0
            state.questions.forEach { q ->
                val answer = state.answers[q.id] ?: ""
                if (app.aiEngine.checkAnswer(q, answer)) correct++
            }
            val score = if (state.questions.isNotEmpty()) (correct * 100) / state.questions.size else 0
            val topicIds = app.mathCurriculum.buildLearningPath("medium", score)
            val result = PlacementResult(
                subjectId = "math",
                score = score,
                totalQuestions = state.questions.size,
                recommendedDifficulty = when {
                    score >= 80 -> "hard"
                    score >= 50 -> "medium"
                    else -> "easy"
                },
                recommendedTopicIds = topicIds
            )
            app.progressRepository.savePlacementResult(result)
            app.progressRepository.saveLearningPath(
                com.mindora.app.data.models.LearningPath(
                    subjectId = "math",
                    topicIds = topicIds,
                    difficulty = result.recommendedDifficulty
                )
            )
            app.progressRepository.unlockAchievement("placement_pro")
            _uiState.update { it.copy(showResult = true, score = score, isSubmitting = false) }
        }
    }
}

data class AiTutorUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isThinking: Boolean = false,
    val modelStatus: com.mindora.app.data.models.AiModelStatus = com.mindora.app.data.models.AiModelStatus(),
    val topicId: String? = null
)

class AiTutorViewModel : ViewModel() {
    private val app = MindoraApp.instance
    private val _uiState = MutableStateFlow(AiTutorUiState())
    val uiState: StateFlow<AiTutorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            app.aiEngine.modelStatusFlow.collect { status ->
                _uiState.update { it.copy(modelStatus = status) }
            }
        }
        _uiState.update {
            it.copy(messages = listOf(
                ChatMessage(
                    id = "welcome",
                    role = "assistant",
                    content = "Greetings, forge apprentice! I'm your Star Guide. Ask me anything about math, request hints, or install the on-device model for enhanced tutoring."
                )
            ))
        }
    }

    fun setTopic(topicId: String?) = _uiState.update { it.copy(topicId = topicId) }
    fun updateInput(v: String) = _uiState.update { it.copy(input = v) }

    fun sendMessage() {
        val input = _uiState.value.input.trim()
        if (input.isBlank()) return
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), role = "user", content = input)
        _uiState.update { it.copy(messages = it.messages + userMsg, input = "", isThinking = true) }
        viewModelScope.launch {
            val response = app.aiEngine.chat(input, _uiState.value.topicId)
            _uiState.update { it.copy(messages = it.messages + response, isThinking = false) }
        }
    }

    fun installModel() {
        viewModelScope.launch { app.aiEngine.installModelPack() }
    }

    fun uninstallModel() {
        viewModelScope.launch { app.aiEngine.uninstallModel() }
    }
}

data class LessonUiState(
    val lesson: com.mindora.app.data.models.Lesson? = null,
    val currentStageIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val feedback: String? = null,
    val isCorrect: Boolean? = null,
    val completed: Boolean = false
)

class LessonViewModel : ViewModel() {
    private val app = MindoraApp.instance
    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    fun loadLesson(lessonId: String) {
        val lesson = app.mathCurriculum.getLesson(lessonId)
        _uiState.update { LessonUiState(lesson = lesson) }
    }

    fun nextStage() {
        _uiState.update { state ->
            val max = (state.lesson?.stages?.size ?: 1) - 1
            if (state.currentStageIndex < max) state.copy(currentStageIndex = state.currentStageIndex + 1, feedback = null, isCorrect = null)
            else state.copy(completed = true)
        }
    }

    fun prevStage() {
        _uiState.update { state ->
            if (state.currentStageIndex > 0) state.copy(currentStageIndex = state.currentStageIndex - 1, feedback = null, isCorrect = null)
            else state
        }
    }

    fun answer(questionId: String, answer: String) {
        _uiState.update { it.copy(answers = it.answers + (questionId to answer)) }
    }

    fun checkAnswer(question: Question) {
        viewModelScope.launch {
            val answer = _uiState.value.answers[question.id] ?: ""
            val correct = app.aiEngine.checkAnswer(question, answer)
            val explanation = app.aiEngine.explainAnswer(question, answer, _uiState.value.lesson?.topicId ?: "")
            _uiState.update { it.copy(isCorrect = correct, feedback = explanation) }
        }
    }

    fun getHint(question: Question) {
        viewModelScope.launch {
            val hint = app.aiEngine.generateHint(question, _uiState.value.lesson?.topicId ?: "")
            _uiState.update { it.copy(feedback = hint) }
        }
    }
}
