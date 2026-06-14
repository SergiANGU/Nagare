package org.nagare.project.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository {

    private val auth = Firebase.auth

    val currentUser: Flow<FirebaseUser?> = auth.authStateChanged

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
    }

    suspend fun signUp(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password)
        return result.user ?: error("L'usuari no s'ha creat correctament")
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun currentUid(): String? = auth.currentUser?.uid
}
