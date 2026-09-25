package com.techexactly.eventmanager.data.repository

import com.techexactly.eventmanager.util.Resource
import com.techexactly.eventmanager.util.safeCall
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val isLoggedIn: Boolean
    suspend fun login(email: String, password: String): Resource<Unit>
    suspend fun register(email: String, password: String): Resource<Unit>
    suspend fun sendPasswordReset(email: String): Resource<Unit>
    fun logout()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    // Firebase Auth persists the session on-device automatically.
    override val isLoggedIn: Boolean get() = auth.currentUser != null

    override suspend fun login(email: String, password: String) = safeCall {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun register(email: String, password: String) = safeCall {
        auth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun sendPasswordReset(email: String) = safeCall {
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    override fun logout() = auth.signOut()
}
