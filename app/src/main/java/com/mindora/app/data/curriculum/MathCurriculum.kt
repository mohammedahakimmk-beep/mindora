package com.mindora.app.data.curriculum

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.mindora.app.data.models.Lesson
import com.mindora.app.data.models.Question
import com.mindora.app.data.models.QuestionType
import com.mindora.app.data.models.Subject
import com.mindora.app.data.models.Topic

data class MathCatalog(
    val subject: SubjectInfo = SubjectInfo(),
    val topics: List<Topic> = emptyList(),
    val lessons: List<Lesson> = emptyList()
)

data class SubjectInfo(
    val id: String = "math",
    val name: String = "Mathematics",
    val description: String = "Forge your numerical constellation across every grade.",
    val icon: String = "🔢"
)

class MathCurriculum(private val context: Context, private val gson: Gson) {

    @Volatile
    private var catalog: MathCatalog? = null

    fun load(): MathCatalog {
        catalog?.let { return it }
        synchronized(this) {
            catalog?.let { return it }
            val built = runCatching { GradeCurriculumFactory.buildCatalog() }.getOrElse { error ->
                Log.e(TAG, "Grade curriculum factory failed", error)
                fallbackCatalog()
            }
            catalog = built
            Log.i(TAG, "Loaded ${built.topics.size} topics / ${built.lessons.size} lessons")
            return built
        }
    }

    fun loadForGrade(grade: String?): MathCatalog {
        val band = GradeCurriculumFactory.normalizeGrade(grade)
        return runCatching { GradeCurriculumFactory.buildCatalogForGrade(band) }
            .getOrElse { load() }
    }

    fun getSubject(grade: String? = null): Subject {
        val cat = if (grade.isNullOrBlank()) load() else loadForGrade(grade)
        return Subject(
            id = cat.subject.id,
            name = cat.subject.name,
            description = cat.subject.description,
            icon = cat.subject.icon,
            available = true,
            topicCount = cat.topics.size
        )
    }

    fun getAllTopics(grade: String? = null): List<Topic> {
        val cat = if (grade.isNullOrBlank()) load() else loadForGrade(grade)
        return cat.topics.sortedBy { it.order }
    }

    fun getTopic(topicId: String): Topic? =
        load().topics.find { it.id == topicId }
            ?: GradeCurriculumFactory.getSupportedGrades()
                .asSequence()
                .map { loadForGrade(it) }
                .flatMap { it.topics.asSequence() }
                .find { it.id == topicId }

    fun getLessonsForTopic(topicId: String): List<Lesson> {
        val fromAll = load().lessons.filter { it.topicId == topicId }
        if (fromAll.isNotEmpty()) return fromAll
        return GradeCurriculumFactory.getSupportedGrades()
            .flatMap { loadForGrade(it).lessons }
            .filter { it.topicId == topicId }
    }

    fun getLesson(lessonId: String): Lesson? {
        load().lessons.find { it.id == lessonId }?.let { return it }
        return GradeCurriculumFactory.getSupportedGrades()
            .asSequence()
            .flatMap { loadForGrade(it).lessons.asSequence() }
            .find { it.id == lessonId }
    }

    fun getPlacementQuestions(grade: String? = null): List<Question> {
        val cat = loadForGrade(grade)
        val fromCatalog = cat.topics.take(5).flatMap { topic ->
            cat.lessons.filter { it.topicId == topic.id }
                .flatMap { lesson -> lesson.stages.flatMap { it.questions } }
                .take(2)
        }
        return fromCatalog.filter { it.prompt.isNotBlank() }.ifEmpty { fallbackPlacementQuestions() }
    }

    fun getPlacementQuestions(): List<Question> = getPlacementQuestions(null)

    fun getAllSubjects(grade: String? = null): List<Subject> = listOf(
        getSubject(grade),
        Subject("languages", "Languages", "Master new tongues across the stars", "🌍", false),
        Subject("chess", "Chess", "Strategic thinking among the constellations", "♟️", false),
        Subject("music", "Music", "Harmonize your mind with melody", "🎵", false)
    )

    fun buildLearningPath(difficulty: String, placementScore: Int, grade: String? = null): List<String> {
        val topics = getAllTopics(grade)
        if (topics.isEmpty()) return emptyList()
        return when {
            placementScore >= 80 -> topics.drop((topics.size * 0.25).toInt()).map { it.id }
            placementScore >= 50 -> topics.map { it.id }
            else -> topics.take((topics.size * 0.5).toInt().coerceAtLeast(3)).map { it.id }
        }
    }

    fun buildLearningPath(difficulty: String, placementScore: Int): List<String> =
        buildLearningPath(difficulty, placementScore, null)

    fun lessonCount(grade: String? = null): Int =
        if (grade.isNullOrBlank()) load().lessons.size
        else GradeCurriculumFactory.lessonCountForGrade(grade)

    private fun fallbackCatalog(): MathCatalog = MathCatalog(
        subject = SubjectInfo(),
        topics = listOf(
            Topic(
                id = "k2_place_value",
                subjectId = "math",
                title = "Place Value",
                description = "[Grade K-2] Ones and tens",
                difficulty = "easy",
                order = 1,
                xpReward = 40,
                energyCost = 1,
                constellationX = 0.2f,
                constellationY = 0.5f,
                lessonIds = listOf("k2_place_value_l01")
            )
        ),
        lessons = emptyList()
    )

    private fun fallbackPlacementQuestions(): List<Question> = listOf(
        Question(
            id = "place_1",
            type = QuestionType.NUMERIC,
            prompt = "What is 7 + 5?",
            correctAnswer = "12",
            explanation = "7 plus 5 equals 12.",
            hint = "Count up 5 from 7."
        ),
        Question(
            id = "place_2",
            type = QuestionType.MULTIPLE_CHOICE,
            prompt = "Which number is greater?",
            options = listOf("19", "91", "9", "11"),
            correctAnswer = "91",
            explanation = "91 is the largest value."
        ),
        Question(
            id = "place_3",
            type = QuestionType.TRUE_FALSE,
            prompt = "15 is an odd number.",
            options = listOf("true", "false"),
            correctAnswer = "true",
            explanation = "15 is not divisible by 2."
        )
    )

    companion object {
        private const val TAG = "MathCurriculum"
    }
}
