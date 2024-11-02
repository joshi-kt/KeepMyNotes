package com.example.keepmynotes.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepmynotes.MainApplication
import com.example.keepmynotes.data.local.dao.TodoDAO
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import com.example.keepmynotes.model.TodoItem
import com.example.keepmynotes.utils.RestrictedAPI
import com.example.keepmynotes.utils.Utils.generateID
import com.example.keepmynotes.utils.Utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val firebaseDbRepository: FirebaseDbRepository,
    private val todoDao: TodoDAO
) : ViewModel() {

    var todoList : LiveData<List<TodoItem>> = todoDao.getAllTodo()
    private val _isSavingTodo = MutableLiveData(false)
    val isSavingTodo : LiveData<Boolean>
        get() = _isSavingTodo
    private val _isDeletingTodoID = MutableLiveData<String>()
    val isDeletingTodo : LiveData<String>
        get() = _isDeletingTodoID
    private val _todoErrorText = MutableLiveData<String>()
    val todoErrorText : LiveData<String>
        get() = _todoErrorText

    fun addTodo(title : String, description : String) {
        _isSavingTodo.value = true
        val todo = TodoItem(id = generateID(), title = title, description = description, createdAt = System.currentTimeMillis())
        firebaseDbRepository.saveTodoToDb(todo)?.addOnCompleteListener {
            if (it.isSuccessful) {
                viewModelScope.launch(Dispatchers.IO) {
                    todoDao.addTodo(todo)
                    withContext(Dispatchers.Main) {
                        _isSavingTodo.value = false
                    }
                }
            } else {
                _isSavingTodo.value = false
                it.exception?.localizedMessage?.let { it1 -> updateErrorInUI(it1) }
                logger("todo saving failed")
            }
        }
    }

    fun deleteTodo(todoItem: TodoItem) {
        _isDeletingTodoID.value = todoItem.id
        firebaseDbRepository.deleteTodoFromDb(todoItem)?.addOnCompleteListener {
            if (it.isSuccessful) {
                viewModelScope.launch(Dispatchers.IO) {
                    todoDao.deleteTodo(todoItem.id)
                    withContext(Dispatchers.Main) {
                        _isDeletingTodoID.value = ""
                    }
                }
            } else {
                it.exception?.localizedMessage?.let { it1 -> updateErrorInUI(it1) }
                logger("todo deletion failed")
            }
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

    private fun updateErrorInUI(error : String) {
        _todoErrorText.value = error
    }

    fun resetErrorText() {
        _todoErrorText.value = ""
    }
}