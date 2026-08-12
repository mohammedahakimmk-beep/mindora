package com.mindora.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.mindora.app.BuildConfig
import com.mindora.app.MindoraApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(MindoraApp.normalizedRtdbUrl())
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user
            ?: throw IllegalStateException("Sign in failed")
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Sign up failed")
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()
        seedUserInDatabase(user, displayName)
        user
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Google sign in failed")
        if (result.additionalUserInfo?.isNewUser == true) {
            seedUserInDatabase(user, user.displayName ?: "Learner")
        }
        user
    }

    private suspend fun seedUserInDatabase(user: FirebaseUser, displayName: String) {
        val isAdmin = user.email?.equals(BuildConfig.ADMIN_EMAIL, ignoreCase = true) == true
        val role = if (isAdmin) "admin" else "learner"
        val userData = mapOf(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "displayName" to displayName,
            "role" to role,
            "xp" to 0,
            "level" to 1,
            "streak" to 0,
            "onboardingComplete" to false,
            "placementComplete" to false,
            "createdAt" to System.currentTimeMillis()
        )
        database.reference.child("users").child(user.uid).updateChildren(userData).await()
        if (isAdmin) {
            database.reference.child("admins").child(user.uid).setValue(
                mapOf("email" to user.email, "role" to "admin", "grantedAt" to System.currentTimeMillis())
            ).await()
        }
    }

    fun signOut() = auth.signOut()

    fun isAdmin(): Boolean {
        val email = auth.currentUser?.email ?: return false
        return email.equals(BuildConfig.ADMIN_EMAIL, ignoreCase = true)
    }
}
