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

class FirebaseDbRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun createUserInFirebaseDb(user: User) : Task<Void> {
        val userRef = getUserPath(firebaseDatabase, user.uid)
        return userRef.setValue(user)
    }

    fun fetchUserFromFirebaseDb(uid: String): Task<DataSnapshot> {
        val userRef = getUserPath(firebaseDatabase, uid)
        return userRef.get()
    }

    fun fetchTodosFromFirebaseDb(uid: String): Task<DataSnapshot> {
        val userRef = getTodosPath(firebaseDatabase, uid)
        return userRef.get()
    }

    fun saveTodoToDb(todo : TodoItem) : Task<Void>? {
        AppPreferences.loggedInUser?.uid?.let {
            val todoRef = getTodoPath(firebaseDatabase, it, todo.id)
            return todoRef.setValue(todo)
        }
        return null
    }

    fun deleteTodoFromDb(todoItem: TodoItem) : Task<Void>? {
        todoItem.isDeleted = true
        return saveTodoToDb(todoItem)
    }

    fun getUserReference(uid : String) : DatabaseReference {
        return getUserPath(firebaseDatabase, uid)
    }

}