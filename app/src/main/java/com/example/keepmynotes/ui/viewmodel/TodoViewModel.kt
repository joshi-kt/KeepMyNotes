package com.example.keepmynotes.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.keepmynotes.data.local.dao.TodoDAO
import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import com.example.keepmynotes.model.TodoItem
import com.example.keepmynotes.model.User
import com.example.keepmynotes.utils.Utils.generateID
import com.example.keepmynotes.utils.Utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val firebaseDbRepository: FirebaseDbRepository,
    authRepository: AuthRepository,
    private val todoDao: TodoDAO
) : BaseViewModel(authRepository, todoDao, firebaseDbRepository) {

    var todoList : LiveData<List<TodoItem>> = todoDao.getAllTodo()
    private val _isSavingTodo = MutableLiveData(false)
    val isSavingTodo : LiveData<Boolean>
        get() = _isSavingTodo
    private val _isDeletingTodoID = MutableLiveData<String>()
    val isDeletingTodo : LiveData<String>
        get() = _isDeletingTodoID
    private val _todoToastText = MutableLiveData<String>()
    val todoToastText : LiveData<String>
        get() = _todoToastText

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

    fun setupMultiLogin(user: User, multiLoginConfig : Boolean) {
        user.multiLogin = multiLoginConfig
        firebaseDbRepository.createUserInFirebaseDb(user).addOnCompleteListener {
            if (it.isSuccessful) {
                AppPreferences.loggedInUser = user
                _todoToastText.value = "Multiple login ${if (multiLoginConfig) "enabled" else "disabled"}"
            }
        }
    }

    private fun updateErrorInUI(error : String) {
        _todoToastText.value = error
    }

    fun resetErrorText() {
        _todoToastText.value = ""
    }

}