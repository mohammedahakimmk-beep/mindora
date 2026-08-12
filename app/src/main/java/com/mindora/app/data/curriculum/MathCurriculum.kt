package com.mindora.app.data.curriculum

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.mindora.app.data.models.Lesson
import com.mindora.app.data.models.Question
import com.mindora.app.data.models.QuestionType
import com.mindora.app.data.models.StageType
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
    val description: String = "",
    val icon: String = "🔢"
)

class MathCurriculum(private val context: Context, private val gson: Gson) {

    @Volatile
    private var catalog: MathCatalog? = null

    fun load(): MathCatalog {
        catalog?.let { return it }
        synchronized(this) {
            catalog?.let { return it }
            val parsed = runCatching { parseFromAssets() }.getOrElse { error ->
                Log.e(TAG, "Failed to load math catalog", error)
                fallbackCatalog()
            }
            catalog = parsed
            return parsed
        }
    }

    private fun parseFromAssets(): MathCatalog {
        val parser = GsonBuilder()
            .registerTypeAdapter(StageType::class.java, EnumTypeAdapter(StageType::class.java))
            .registerTypeAdapter(QuestionType::class.java, EnumTypeAdapter(QuestionType::class.java))
            .create()
        val json = context.assets.open("curriculum/math/catalog.json")
            .bufferedReader().use { it.readText() }
        return parser.fromJson(json, MathCatalog::class.java) ?: fallbackCatalog()
    }

    fun getSubject(): Subject {
        val cat = load()
        return Subject(
            id = cat.subject.id,
            name = cat.subject.name,
            description = cat.subject.description,
            icon = cat.subject.icon,
            available = true,
            topicCount = cat.topics.size
        )
    }

    fun getAllTopics(): List<Topic> = load().topics.sortedBy { it.order }

    fun getTopic(topicId: String): Topic? = load().topics.find { it.id == topicId }

    fun getLessonsForTopic(topicId: String): List<Lesson> =
        load().lessons.filter { it.topicId == topicId }

    fun getLesson(lessonId: String): Lesson? = load().lessons.find { it.id == lessonId }

    fun getPlacementQuestions(): List<Question> {
        val fromCatalog = runCatching {
            load().topics.take(5).flatMap { topic ->
                getLessonsForTopic(topic.id).flatMap { lesson ->
                    lesson.stages.flatMap { it.questions }
                }.take(2)
            }
        }.getOrDefault(emptyList())

        return fromCatalog.filter { it.id.isNotBlank() && it.prompt.isNotBlank() }
            .ifEmpty { fallbackPlacementQuestions() }
    }

    fun getAllSubjects(): List<Subject> = listOf(
        getSubject(),
        Subject("languages", "Languages", "Master new tongues across the stars", "🌍", false),
        Subject("chess", "Chess", "Strategic thinking among the constellations", "♟️", false),
        Subject("music", "Music", "Harmonize your mind with melody", "🎵", false)
    )

    fun buildLearningPath(difficulty: String, placementScore: Int): List<String> {
        val topics = getAllTopics()
        if (topics.isEmpty()) return listOf("numbers_basics")
        return when {
            placementScore >= 80 -> topics.filter { it.difficulty != "easy" }.map { it.id }
                .ifEmpty { topics.map { it.id } }
            placementScore >= 50 -> topics.map { it.id }
            else -> topics.filter { it.difficulty == "easy" }.map { it.id }
                .ifEmpty { topics.map { it.id } }
        }
    }

    private fun fallbackCatalog(): MathCatalog = MathCatalog(
        subject = SubjectInfo(),
        topics = listOf(
            Topic(
                id = "numbers_basics",
                subjectId = "math",
                title = "Number Constellations",
                description = "Understand whole numbers and place value.",
                difficulty = "easy",
                order = 1,
                xpReward = 50,
                energyCost = 2,
                constellationX = 0.2f,
                constellationY = 0.5f,
                lessonIds = listOf("numbers_lesson_1")
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
            explanation = "91 is the largest value.",
            hint = "Compare place values."
        ),
        Question(
            id = "place_3",
            type = QuestionType.TRUE_FALSE,
            prompt = "15 is an odd number.",
            options = listOf("true", "false"),
            correctAnswer = "true",
            explanation = "15 is not divisible by 2.",
            hint = "Odd numbers end in 1, 3, 5, 7, or 9."
        ),
        Question(
            id = "place_4",
            type = QuestionType.NUMERIC,
            prompt = "What is 9 × 3?",
            correctAnswer = "27",
            explanation = "9 times 3 equals 27.",
            hint = "Think of 9 + 9 + 9."
        )
    )

    companion object {
        private const val TAG = "MathCurriculum"
    }
}

private class EnumTypeAdapter<T : Enum<T>>(private val clazz: Class<T>) : TypeAdapter<T>() {
    override fun write(out: JsonWriter, value: T?) {
        if (value == null) out.nullValue() else out.value(value.name)
    }

    override fun read(reader: JsonReader): T? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return clazz.enumConstants?.firstOrNull()
        }
        val name = reader.nextString()
        return clazz.enumConstants?.find { it.name.equals(name, ignoreCase = true) }
            ?: clazz.enumConstants?.firstOrNull()
    }
}
