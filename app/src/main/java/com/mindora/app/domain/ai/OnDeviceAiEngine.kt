package com.mindora.app.domain.ai

import android.content.Context
import com.google.gson.Gson
import com.mindora.app.data.curriculum.MathCurriculum
import com.mindora.app.data.local.DataStoreManager
import com.mindora.app.data.models.AiModelStatus
import com.mindora.app.data.models.ChatMessage
import com.mindora.app.data.models.Question
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

class OnDeviceAiEngine(
    private val context: Context,
    private val dataStore: DataStoreManager,
    private val gson: Gson,
    private val mathCurriculum: MathCurriculum
) {
    private val knowledgeBase: Map<String, List<String>> by lazy { loadKnowledgeBase() }

    val modelStatusFlow: Flow<AiModelStatus> = dataStore.aiModelStatusFlow

    suspend fun getModelStatus(): AiModelStatus = dataStore.aiModelStatusFlow.first()

    suspend fun installModelPack(): AiModelStatus {
        dataStore.saveAiModelStatus(AiModelStatus(downloading = true, downloadProgress = 0f))
        repeat(10) { step ->
            delay(300)
            dataStore.saveAiModelStatus(
                AiModelStatus(downloading = true, downloadProgress = (step + 1) / 10f)
            )
        }
        val installed = AiModelStatus(
            installed = true,
            version = "1.0.0-mindora-tutor",
            downloadProgress = 1f,
            downloading = false,
            lastUpdated = System.currentTimeMillis()
        )
        dataStore.saveAiModelStatus(installed)
        return installed
    }

    suspend fun uninstallModel() {
        dataStore.saveAiModelStatus(AiModelStatus())
    }

    suspend fun generateHint(question: Question, topicId: String): String {
        val status = getModelStatus()
        if (question.hint.isNotBlank()) return question.hint
        return if (status.installed) {
            enhancedHint(question, topicId)
        } else {
            templateHint(question, topicId)
        }
    }

    suspend fun explainAnswer(question: Question, userAnswer: String, topicId: String): String {
        val status = getModelStatus()
        val correct = checkAnswer(question, userAnswer)
        val base = if (correct) {
            "Excellent forge work! ${question.explanation}"
        } else {
            "Not quite. The correct answer is ${question.correctAnswer}. ${question.explanation}"
        }
        return if (status.installed) {
            "$base\n\n${enhancedExplanation(question, topicId)}"
        } else {
            "$base\n\n${templateExplanation(question, topicId)}"
        }
    }

    suspend fun chat(userMessage: String, topicId: String? = null): ChatMessage {
        val status = getModelStatus()
        val response = if (status.installed) {
            ragResponse(userMessage, topicId)
        } else {
            templateResponse(userMessage, topicId)
        }
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "assistant",
            content = response
        )
    }

    suspend fun generatePracticeQuestion(topicId: String, difficulty: Int): Question? {
        val topic = mathCurriculum.getTopic(topicId) ?: return null
        val lesson = mathCurriculum.getLessonsForTopic(topicId).randomOrNull() ?: return null
        val existing = lesson.stages.flatMap { it.questions }
        if (existing.isNotEmpty()) {
            val base = existing.random()
            return mutateQuestion(base, difficulty)
        }
        return generateFromTemplate(topic.title, difficulty)
    }

    fun adjustDifficulty(currentDifficulty: String, correctStreak: Int, wrongStreak: Int): String {
        return when {
            wrongStreak >= 3 && currentDifficulty != "easy" -> "easy"
            correctStreak >= 5 && currentDifficulty != "hard" -> "hard"
            correctStreak >= 3 && currentDifficulty == "easy" -> "medium"
            else -> currentDifficulty
        }
    }

    fun checkAnswer(question: Question, answer: String): Boolean {
        val normalized = answer.trim().lowercase()
        val correct = question.correctAnswer.trim().lowercase()
        return when (question.type) {
            com.mindora.app.data.models.QuestionType.NUMERIC -> {
                val userNum = normalized.toDoubleOrNull()
                val correctNum = correct.toDoubleOrNull()
                userNum != null && correctNum != null && abs(userNum - correctNum) < 0.001
            }
            else -> normalized == correct
        }
    }

    private fun templateHint(question: Question, topicId: String): String {
        val topic = mathCurriculum.getTopic(topicId)?.title ?: "this topic"
        return when (question.type) {
            com.mindora.app.data.models.QuestionType.NUMERIC ->
                "Think about the operations you've learned in $topic. Break the problem into smaller steps."
            com.mindora.app.data.models.QuestionType.MULTIPLE_CHOICE ->
                "Eliminate answers that don't match what you know about $topic. Check units and signs."
            else -> "Review the key concept: ${knowledgeBase[topicId]?.firstOrNull() ?: "re-read the lesson content."}"
        }
    }

    private fun enhancedHint(question: Question, topicId: String): String {
        return "${templateHint(question, topicId)}\n\n[Star Guide Model] Focus on: ${question.prompt.take(60)}..."
    }

    private fun templateExplanation(question: Question, topicId: String): String {
        val facts = knowledgeBase[topicId] ?: emptyList()
        val fact = facts.randomOrNull() ?: "Practice similar problems to strengthen this skill."
        return "Key concept: $fact"
    }

    private fun enhancedExplanation(question: Question, topicId: String): String {
        return "[Enhanced] ${templateExplanation(question, topicId)} Try a related practice problem next."
    }

    private fun templateResponse(message: String, topicId: String?): String {
        val lower = message.lowercase()
        return when {
            lower.contains("help") || lower.contains("hint") ->
                "I'm your Star Guide! Ask me about a specific math topic, or tap Hint during practice. I work offline using Mindora's curriculum knowledge."
            lower.contains("fraction") ->
                "Fractions represent parts of a whole. To add fractions, find a common denominator. To multiply, multiply numerators and denominators separately."
            lower.contains("algebra") || lower.contains("equation") ->
                "In algebra, we use variables like x to represent unknown values. Solve equations by doing the same operation on both sides."
            lower.contains("geometry") || lower.contains("area") || lower.contains("triangle") ->
                "Geometry studies shapes and space. Key formulas: rectangle area = length × width, triangle area = ½ × base × height."
            lower.contains("multiply") || lower.contains("multiplication") ->
                "Multiplication is repeated addition. Use the times table and break large numbers into smaller parts."
            lower.contains("divide") || lower.contains("division") ->
                "Division splits a number into equal groups. Check your answer by multiplying the quotient by the divisor."
            topicId != null -> {
                val topic = mathCurriculum.getTopic(topicId)
                "About ${topic?.title ?: "math"}: ${knowledgeBase[topicId]?.joinToString(" ") ?: "Let's explore this topic together!"}"
            }
            else ->
                "Great question! In Mindora's forge path, every skill builds on the last. Tell me which topic you're working on and I'll guide you."
        }
    }

    private fun ragResponse(message: String, topicId: String?): String {
        val context = buildString {
            if (topicId != null) {
                val topic = mathCurriculum.getTopic(topicId)
                append("Context: ${topic?.title} - ${topic?.description}\n")
                knowledgeBase[topicId]?.forEach { append("- $it\n") }
            }
        }
        return "[Star Guide v1.0]\n$context\n${templateResponse(message, topicId)}"
    }

    private fun mutateQuestion(base: Question, difficulty: Int): Question {
        if (base.type != com.mindora.app.data.models.QuestionType.NUMERIC) return base
        val num = base.correctAnswer.toIntOrNull() ?: return base
        val offset = Random.nextInt(-5, 6) * difficulty
        val newNum = num + offset
        return base.copy(
            id = UUID.randomUUID().toString(),
            prompt = base.prompt.replace(num.toString(), newNum.toString()),
            correctAnswer = newNum.toString()
        )
    }

    private fun generateFromTemplate(topicTitle: String, difficulty: Int): Question {
        val a = Random.nextInt(1, 10 * difficulty)
        val b = Random.nextInt(1, 10 * difficulty)
        return Question(
            id = UUID.randomUUID().toString(),
            type = com.mindora.app.data.models.QuestionType.NUMERIC,
            prompt = "What is $a + $b?",
            correctAnswer = (a + b).toString(),
            explanation = "$a + $b = ${a + b}. Add the two numbers together.",
            hint = "Start with the larger number and count up.",
            difficulty = difficulty
        )
    }

    private fun loadKnowledgeBase(): Map<String, List<String>> {
        return mathCurriculum.getAllTopics().associate { topic ->
            topic.id to listOf(
                topic.description,
                "Difficulty: ${topic.difficulty}",
                "Practice makes perfect with ${topic.title}."
            )
        }
    }
}
