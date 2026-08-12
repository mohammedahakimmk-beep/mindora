package com.mindora.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.mindora.app.BuildConfig
import com.mindora.app.MindoraApp
import com.mindora.app.data.local.DataStoreManager
import com.mindora.app.data.models.OnboardingData
import com.mindora.app.data.models.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(MindoraApp.normalizedRtdbUrl()),
    private val dataStore: DataStoreManager,
    private val gson: Gson
) {
    val profileFlow: Flow<UserProfile?> = dataStore.userProfileFlow

    fun observeRemoteProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val ref = database.reference.child("users").child(uid)
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(runCatching { mapSnapshot(snapshot, uid) }.getOrNull())
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                trySend(null)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun mapSnapshot(
        snapshot: com.google.firebase.database.DataSnapshot,
        uid: String
    ): UserProfile? {
        if (!snapshot.exists()) return null
        return UserProfile(
            uid = snapshot.child("uid").getValue(String::class.java) ?: uid,
            email = snapshot.child("email").getValue(String::class.java).orEmpty(),
            displayName = snapshot.child("displayName").getValue(String::class.java).orEmpty(),
            photoUrl = snapshot.child("photoUrl").getValue(String::class.java).orEmpty(),
            role = snapshot.child("role").getValue(String::class.java) ?: "learner",
            age = snapshot.child("age").getValue(Int::class.java)
                ?: snapshot.child("age").getValue(Long::class.java)?.toInt() ?: 0,
            grade = snapshot.child("grade").getValue(String::class.java).orEmpty(),
            goals = snapshot.child("goals").children.mapNotNull { it.getValue(String::class.java) },
            experienceLevel = snapshot.child("experienceLevel").getValue(String::class.java) ?: "beginner",
            preferredDifficulty = snapshot.child("preferredDifficulty").getValue(String::class.java) ?: "medium",
            onboardingComplete = snapshot.child("onboardingComplete").getValue(Boolean::class.java) ?: false,
            placementComplete = snapshot.child("placementComplete").getValue(Boolean::class.java) ?: false,
            xp = snapshot.child("xp").getValue(Int::class.java)
                ?: snapshot.child("xp").getValue(Long::class.java)?.toInt() ?: 0,
            level = snapshot.child("level").getValue(Int::class.java)
                ?: snapshot.child("level").getValue(Long::class.java)?.toInt() ?: 1,
            streak = snapshot.child("streak").getValue(Int::class.java)
                ?: snapshot.child("streak").getValue(Long::class.java)?.toInt() ?: 0,
            lastActiveDate = snapshot.child("lastActiveDate").getValue(String::class.java).orEmpty(),
            createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L,
            selectedSubjects = snapshot.child("selectedSubjects").children
                .mapNotNull { it.getValue(String::class.java) }
                .ifEmpty { listOf("math") }
        )
    }

    suspend fun syncProfile(uid: String) {
        runCatching {
            val snapshot = database.reference.child("users").child(uid).get().await()
            if (!snapshot.exists()) return
            // Avoid fragile getValue(UserProfile) with generic lists — map fields manually.
            val profile = UserProfile(
                uid = snapshot.child("uid").getValue(String::class.java) ?: uid,
                email = snapshot.child("email").getValue(String::class.java).orEmpty(),
                displayName = snapshot.child("displayName").getValue(String::class.java).orEmpty(),
                photoUrl = snapshot.child("photoUrl").getValue(String::class.java).orEmpty(),
                role = snapshot.child("role").getValue(String::class.java) ?: "learner",
                age = snapshot.child("age").getValue(Int::class.java)
                    ?: snapshot.child("age").getValue(Long::class.java)?.toInt() ?: 0,
                grade = snapshot.child("grade").getValue(String::class.java).orEmpty(),
                goals = snapshot.child("goals").children.mapNotNull { it.getValue(String::class.java) },
                experienceLevel = snapshot.child("experienceLevel").getValue(String::class.java) ?: "beginner",
                preferredDifficulty = snapshot.child("preferredDifficulty").getValue(String::class.java) ?: "medium",
                onboardingComplete = snapshot.child("onboardingComplete").getValue(Boolean::class.java) ?: false,
                placementComplete = snapshot.child("placementComplete").getValue(Boolean::class.java) ?: false,
                xp = snapshot.child("xp").getValue(Int::class.java)
                    ?: snapshot.child("xp").getValue(Long::class.java)?.toInt() ?: 0,
                level = snapshot.child("level").getValue(Int::class.java)
                    ?: snapshot.child("level").getValue(Long::class.java)?.toInt() ?: 1,
                streak = snapshot.child("streak").getValue(Int::class.java)
                    ?: snapshot.child("streak").getValue(Long::class.java)?.toInt() ?: 0,
                lastActiveDate = snapshot.child("lastActiveDate").getValue(String::class.java).orEmpty(),
                createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L,
                selectedSubjects = snapshot.child("selectedSubjects").children
                    .mapNotNull { it.getValue(String::class.java) }
                    .ifEmpty { listOf("math") }
            )
            dataStore.saveUserProfile(profile)
        }
    }

    suspend fun getProfile(uid: String): UserProfile? {
        val local = runCatching { dataStore.userProfileFlow.first() }.getOrNull()
        if (local != null) return local
        syncProfile(uid)
        return runCatching { dataStore.userProfileFlow.first() }.getOrNull()
    }

    suspend fun updateProfile(profile: UserProfile) {
        dataStore.saveUserProfile(profile)
        runCatching {
            val payload = mapOf(
                "uid" to profile.uid,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "photoUrl" to profile.photoUrl,
                "role" to profile.role,
                "age" to profile.age,
                "grade" to profile.grade,
                "goals" to ArrayList(profile.goals),
                "experienceLevel" to profile.experienceLevel,
                "preferredDifficulty" to profile.preferredDifficulty,
                "onboardingComplete" to profile.onboardingComplete,
                "placementComplete" to profile.placementComplete,
                "xp" to profile.xp,
                "level" to profile.level,
                "streak" to profile.streak,
                "lastActiveDate" to profile.lastActiveDate,
                "createdAt" to profile.createdAt,
                "selectedSubjects" to ArrayList(profile.selectedSubjects)
            )
            database.reference.child("users").child(profile.uid).updateChildren(payload).await()
        }
    }

    suspend fun completeOnboarding(uid: String, data: OnboardingData) {
        val updates = mapOf(
            "age" to data.age,
            "grade" to data.grade,
            "goals" to ArrayList(data.goals),
            "experienceLevel" to data.experienceLevel,
            "preferredDifficulty" to data.preferredDifficulty,
            "onboardingComplete" to true
        )
        runCatching {
            database.reference.child("users").child(uid).updateChildren(updates).await()
        }
        val current = runCatching { dataStore.userProfileFlow.first() }.getOrNull()
            ?: UserProfile(uid = uid, email = auth.currentUser?.email.orEmpty())
        dataStore.saveUserProfile(
            current.copy(
                uid = uid,
                age = data.age,
                grade = data.grade,
                goals = data.goals,
                experienceLevel = data.experienceLevel,
                preferredDifficulty = data.preferredDifficulty,
                onboardingComplete = true
            )
        )
    }

    suspend fun isAdmin(uid: String): Boolean {
        val snapshot = database.reference.child("admins").child(uid).get().await()
        return snapshot.exists() || auth.currentUser?.email?.equals(BuildConfig.ADMIN_EMAIL, ignoreCase = true) == true
    }
}
