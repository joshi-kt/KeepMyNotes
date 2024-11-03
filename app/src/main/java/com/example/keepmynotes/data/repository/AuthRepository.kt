package com.example.keepmynotes.data.repository

import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.example.keepmynotes.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
    }

    fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    suspend fun signInWithFirebaseCredential(authCredential: AuthCredential): AuthResult? {
        return firebaseAuth.signInWithCredential(authCredential).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
        AppPreferences.clearPreferences()
    }

    fun currentUser() = firebaseAuth.currentUser
}