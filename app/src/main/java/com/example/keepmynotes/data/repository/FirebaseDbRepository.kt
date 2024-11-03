package com.example.keepmynotes.data.repository

import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.example.keepmynotes.model.TodoItem
import com.example.keepmynotes.model.User
import com.example.keepmynotes.utils.Utils.getTodoPath
import com.example.keepmynotes.utils.Utils.getTodosPath
import com.example.keepmynotes.utils.Utils.getUserPath
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseDbRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    suspend fun createUserInFirebaseDb(user: User) {
        val userRef = getUserPath(firebaseDatabase, user.uid)
        userRef.setValue(user).await()
    }

    suspend fun fetchUserFromFirebaseDb(uid: String): DataSnapshot? {
        val userRef = getUserPath(firebaseDatabase, uid)
        return userRef.get().await()
    }

    suspend fun fetchTodosFromFirebaseDb(uid: String): DataSnapshot? {
        val userRef = getTodosPath(firebaseDatabase, uid)
        return userRef.get().await()
    }

    suspend fun saveTodoToDb(todo : TodoItem) {
        AppPreferences.loggedInUser?.uid?.let {
            val todoRef = getTodoPath(firebaseDatabase, it, todo.id)
            todoRef.setValue(todo).await()
        }
    }

    suspend fun deleteTodoFromDb(todoItem: TodoItem) {
        todoItem.isDeleted = true
        return saveTodoToDb(todoItem)
    }

    fun getUserReference(uid : String) : DatabaseReference {
        return getUserPath(firebaseDatabase, uid)
    }

}