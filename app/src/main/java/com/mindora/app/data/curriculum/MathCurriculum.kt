package com.mindora.app.data.curriculum

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.mindora.app.data.models.QuestionType
import com.mindora.app.data.models.StageType
import com.google.gson.annotations.SerializedName
import com.mindora.app.data.models.Lesson
import com.mindora.app.data.models.Subject
import com.mindora.app.data.models.Topic

data class MathCatalog(
    val subject: SubjectInfo,
    val topics: List<Topic>,
    val lessons: List<Lesson>
)

data class SubjectInfo(
    val id: String,
    val name: String,
    val description: String,
    val icon: String
)

class MathCurriculum(private val context: Context, private val gson: Gson) {

    private var catalog: MathCatalog? = null

    fun load(): MathCatalog {
        catalog?.let { return it }
        val gson = GsonBuilder()
            .registerTypeAdapter(StageType::class.java, EnumTypeAdapter(StageType::class.java))
            .registerTypeAdapter(QuestionType::class.java, EnumTypeAdapter(QuestionType::class.java))
            .create()
        val json = context.assets.open("curriculum/math/catalog.json")
            .bufferedReader().use { it.readText() }
        catalog = gson.fromJson(json, MathCatalog::class.java)
        return catalog!!
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

    fun getPlacementQuestions(): List<com.mindora.app.data.models.Question> {
        return load().topics.take(5).flatMap { topic ->
            getLessonsForTopic(topic.id).flatMap { lesson ->
                lesson.stages.flatMap { it.questions }
            }.take(2)
        }
    }

    fun getAllSubjects(): List<Subject> = listOf(
        getSubject(),
        Subject("languages", "Languages", "Master new tongues across the stars", "🌍", false),
        Subject("chess", "Chess", "Strategic thinking among the constellations", "♟️", false),
        Subject("music", "Music", "Harmonize your mind with melody", "🎵", false)
    )

    fun buildLearningPath(difficulty: String, placementScore: Int): List<String> {
        val topics = getAllTopics()
        return when {
            placementScore >= 80 -> topics.filter { it.difficulty != "easy" }.map { it.id }
            placementScore >= 50 -> topics.map { it.id }
            else -> topics.filter { it.difficulty == "easy" }.map { it.id }
        }
    }
}

private class EnumTypeAdapter<T : Enum<T>>(private val clazz: Class<T>) : TypeAdapter<T>() {
    override fun write(out: JsonWriter, value: T?) {
        out.value(value?.name)
    }
    override fun read(reader: JsonReader): T? {
        val name = reader.nextString()
        return clazz.enumConstants?.find { it.name.equals(name, ignoreCase = true) }
    }
}
