package com.example.keepmynotes.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.keepmynotes.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

object Utils {
    const val APP_PREFERENCE_NAME = "AppPreferences"
    const val IS_LOGGED_IN = "is_logged_in"
    const val LOGGED_IN = "logged_in"
    const val PLEASE_WAIT = "Please Wait ..."
    const val SCREEN_AUTH = "SCREEN_AUTH"
    const val SCREEN_NOTES = "SCREEN_NOTES"

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    fun logger(value : String) = Log.d("notesLogs",value)

    fun showToast(context: Context, text : String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    fun validateName(name: String): Boolean {
        return name.trim().isNotBlank()
    }

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.trim().length >= 8
    }

    fun isSignInCredentialValid(email: String, password: String) : Boolean {
        return validateEmail(email) && validatePassword(password)
    }

    fun isSignUpCredentialValid(name: String, email: String, password: String) : Boolean {
        return validateName(name) && validateEmail(email) && validatePassword(password)
    }

    fun getUserPath(firebaseDatabase: FirebaseDatabase, uid : String) : DatabaseReference {
        return firebaseDatabase.reference.child("Users").child(uid)
    }

    fun getTodoPath(firebaseDatabase: FirebaseDatabase, uid : String, tid : String) : DatabaseReference {
        return firebaseDatabase.reference.child("Todos").child(uid).child(tid)
    }

    fun generateID() : String {
        return UUID.randomUUID().toString()
    }

    fun setDeviceHashToUser(user: User) {
        user.deviceHash = generateID()
    }
}