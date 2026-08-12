package com.mindora.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mindora.app.BuildConfig
import com.mindora.app.data.local.DataStoreManager
import com.mindora.app.data.models.Achievement
import com.mindora.app.data.models.LearningPath
import com.mindora.app.data.models.PlacementResult
import com.mindora.app.data.models.TopicProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ProgressRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.RTDB_URL),
    private val dataStore: DataStoreManager
) {
    private val uid: String? get() = auth.currentUser?.uid

    fun learningPathFlow(subjectId: String): Flow<LearningPath?> =
        dataStore.learningPathFlow(subjectId)

    suspend fun saveLearningPath(path: LearningPath) {
        dataStore.saveLearningPath(path)
        val userId = uid ?: return
        database.reference.child("progress").child(userId).child("paths").child(path.subjectId)
            .setValue(path).await()
    }

    suspend fun getLearningPath(subjectId: String): LearningPath? {
        val local = dataStore.learningPathFlow(subjectId)
        val userId = uid ?: return null
        return try {
            val snapshot = database.reference.child("progress").child(userId)
                .child("paths").child(subjectId).get().await()
            snapshot.getValue(LearningPath::class.java)?.also { dataStore.saveLearningPath(it) }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun savePlacementResult(result: PlacementResult) {
        val userId = uid ?: return
        database.reference.child("progress").child(userId).child("placement").child(result.subjectId)
            .setValue(result).await()
        database.reference.child("users").child(userId).child("placementComplete").setValue(true).await()
    }

    suspend fun updateTopicProgress(topicId: String, progress: TopicProgress) {
        val userId = uid ?: return
        database.reference.child("progress").child(userId).child("topics").child(topicId)
            .setValue(progress).await()
    }

    suspend fun addXp(amount: Int): Int {
        val userId = uid ?: return 0
        val snapshot = database.reference.child("users").child(userId).child("xp").get().await()
        val currentXp = (snapshot.getValue(Int::class.java) ?: 0) + amount
        val newLevel = calculateLevel(currentXp)
        database.reference.child("users").child(userId).updateChildren(
            mapOf("xp" to currentXp, "level" to newLevel)
        ).await()
        return currentXp
    }

    suspend fun updateStreak(): Int {
        val userId = uid ?: return 0
        val today = utcDateString()
        val snapshot = database.reference.child("users").child(userId).get().await()
        val lastActive = snapshot.child("lastActiveDate").getValue(String::class.java) ?: ""
        val currentStreak = snapshot.child("streak").getValue(Int::class.java) ?: 0
        val yesterday = utcYesterdayString()

        val newStreak = when (lastActive) {
            today -> currentStreak
            yesterday -> currentStreak + 1
            else -> 1
        }
        database.reference.child("users").child(userId).updateChildren(
            mapOf("streak" to newStreak, "lastActiveDate" to today)
        ).await()
        return newStreak
    }

    suspend fun getAchievements(): List<Achievement> {
        val userId = uid ?: return defaultAchievements()
        return try {
            val snapshot = database.reference.child("progress").child(userId)
                .child("achievements").get().await()
            if (!snapshot.exists()) return defaultAchievements()
            snapshot.children.mapNotNull { it.getValue(Achievement::class.java) }
        } catch (_: Exception) {
            defaultAchievements()
        }
    }

    suspend fun unlockAchievement(achievementId: String) {
        val userId = uid ?: return
        database.reference.child("progress").child(userId).child("achievements")
            .child(achievementId).child("unlocked").setValue(true).await()
        database.reference.child("progress").child(userId).child("achievements")
            .child(achievementId).child("unlockedAt").setValue(System.currentTimeMillis()).await()
    }

    private fun calculateLevel(xp: Int): Int = (xp / 100) + 1

    private fun utcDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun utcYesterdayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }

    fun defaultAchievements() = listOf(
        Achievement("first_star", "First Star", "Complete your first lesson", "⭐"),
        Achievement("streak_3", "Triple Forge", "Maintain a 3-day streak", "🔥", xpBonus = 50),
        Achievement("streak_7", "Week Warrior", "Maintain a 7-day streak", "⚡", xpBonus = 100),
        Achievement("math_master", "Math Forger", "Complete all math topics", "🔢", xpBonus = 200),
        Achievement("perfect_score", "Perfect Forge", "Score 100% on an assessment", "💎", xpBonus = 75),
        Achievement("ai_explorer", "Star Guide", "Ask the AI tutor 10 questions", "🤖", xpBonus = 30),
        Achievement("energy_saver", "Efficient Learner", "Complete 5 lessons without running out of energy", "⚙️", xpBonus = 40),
        Achievement("placement_pro", "Pathfinder", "Complete the placement test", "🧭", xpBonus = 25)
    )
}
