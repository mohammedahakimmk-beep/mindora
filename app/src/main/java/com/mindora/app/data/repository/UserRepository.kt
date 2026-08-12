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
                val profile = snapshot.getValue(UserProfile::class.java)
                trySend(profile)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                trySend(null)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun syncProfile(uid: String) {
        val snapshot = database.reference.child("users").child(uid).get().await()
        val profile = snapshot.getValue(UserProfile::class.java) ?: return
        dataStore.saveUserProfile(profile)
    }

    suspend fun getProfile(uid: String): UserProfile? {
        val local = dataStore.userProfileFlow.first()
        if (local != null) return local
        val snapshot = database.reference.child("users").child(uid).get().await()
        return snapshot.getValue(UserProfile::class.java)?.also {
            dataStore.saveUserProfile(it)
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        dataStore.saveUserProfile(profile)
        database.reference.child("users").child(profile.uid).setValue(profile).await()
    }

    suspend fun completeOnboarding(uid: String, data: OnboardingData) {
        val updates = mapOf(
            "age" to data.age,
            "grade" to data.grade,
            "goals" to data.goals,
            "experienceLevel" to data.experienceLevel,
            "preferredDifficulty" to data.preferredDifficulty,
            "onboardingComplete" to true
        )
        database.reference.child("users").child(uid).updateChildren(updates).await()
        val current = getProfile(uid) ?: UserProfile(uid = uid)
        dataStore.saveUserProfile(current.copy(
            age = data.age,
            grade = data.grade,
            goals = data.goals,
            experienceLevel = data.experienceLevel,
            preferredDifficulty = data.preferredDifficulty,
            onboardingComplete = true
        ))
    }

    suspend fun isAdmin(uid: String): Boolean {
        val snapshot = database.reference.child("admins").child(uid).get().await()
        return snapshot.exists() || auth.currentUser?.email?.equals(BuildConfig.ADMIN_EMAIL, ignoreCase = true) == true
    }
}
