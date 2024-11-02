package com.example.keepmynotes.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepmynotes.MainApplication
import com.example.keepmynotes.MainApplication.Companion.todoDatabase
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import com.example.keepmynotes.model.TodoItem
import com.example.keepmynotes.utils.RestrictedAPI
import com.example.keepmynotes.utils.Utils.generateID
import com.example.keepmynotes.utils.Utils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.UUID

class TodoViewModel(private val authRepository: AuthRepository, private val firebaseDbRepository: FirebaseDbRepository) : ViewModel() {

    private val todoDao = todoDatabase.getTodoDAO()
    var todoList : LiveData<List<TodoItem>> = todoDao.getAllTodo()
    private val _showLoading = MutableLiveData(false)
    val showLoading : LiveData<Boolean>
        get() = _showLoading

    fun addTodo(title : String, description : String) {
        _showLoading.value = true
        val todo = TodoItem(id = generateID(), title = title, description = description, createdAt = System.currentTimeMillis())
        firebaseDbRepository.saveTodoToDb(todo)?.addOnCompleteListener {
            if (it.isSuccessful) {
                viewModelScope.launch(Dispatchers.IO) {
                    todoDao.addTodo(todo)
                    withContext(Dispatchers.Main) {
                        _showLoading.value = false
                    }
                }
            } else {
                logger("todo saving failed")
            }
        }
    }

    fun deleteTodo(id : String) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.deleteTodo(id)
        }
    }

    @RestrictedAPI
    fun deleteAllTodo() {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.deleteAllTodo()
        }
    }

    fun searchTodo(searchText : String) {
        viewModelScope.launch(Dispatchers.IO) {
            val wildSearchText = "%$searchText%"
            todoList = if (searchText.isBlank()) {
                todoDao.getAllTodo()
            } else {
                todoDao.searchTodo(wildSearchText)
            }
        }
    }
}