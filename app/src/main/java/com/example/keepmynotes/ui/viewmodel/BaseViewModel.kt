package com.example.keepmynotes.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepmynotes.data.local.dao.TodoDAO
import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import com.example.keepmynotes.model.TodoItem
import com.example.keepmynotes.model.User
import com.example.keepmynotes.utils.RestrictedAPI
import com.example.keepmynotes.utils.Utils
import com.example.keepmynotes.utils.Utils.logger
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class BaseViewModel (
    private val authRepository: AuthRepository,
    private val todoDAO: TodoDAO,
    private val firebaseDbRepository: FirebaseDbRepository
) : ViewModel() {

    private lateinit var userListener : ValueEventListener

    private val _authenticationState = MutableLiveData(if (AppPreferences.isLoggedIn) Utils.AuthenticationState.AUTHENTICATED else Utils.AuthenticationState.UNAUTHENTICATED)
    val authenticationState : LiveData<Utils.AuthenticationState>
        get() = _authenticationState

    private fun handleDeviceHashChange() {
        logOut()
    }

    fun syncTodosWithFirebase(uid: String, onTodosFetched : (List<TodoItem>) -> Unit = {}, onFailed: () -> Unit = {}) {
        firebaseDbRepository.fetchTodosFromFirebaseDb(uid)
            .addOnCompleteListener {
                if (it.isSuccessful && AppPreferences.isLoggedIn) {
                    val dataSnapshot = it.result
                    val todos = dataSnapshot.children.mapNotNull { snapshot ->
                        snapshot.getValue(TodoItem::class.java)
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        todoDAO.insertAllTodo(todos)
                        withContext(Dispatchers.Main) {
                            onTodosFetched(todos)
                        }
                    }
                } else {
                    onFailed()
                }
            }
    }

    fun updateAuthenticationState(state : Utils.AuthenticationState) {
        _authenticationState.value = state
    }

    fun registerUserListener() {
        if (AppPreferences.isLoggedIn) {
            val ref = firebaseDbRepository.getUserReference(AppPreferences.loggedInUser!!.uid)
            userListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        if (AppPreferences.isLoggedIn) {
                            val loggedInUser = AppPreferences.loggedInUser!!
                            if ( !user.multiLogin &&
                                it.deviceHash != loggedInUser.deviceHash ) {
                                handleDeviceHashChange()
                            } else if (loggedInUser.multiLogin != user.multiLogin) {
                                loggedInUser.multiLogin = user.multiLogin
                                AppPreferences.loggedInUser = loggedInUser
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            ref.addValueEventListener(userListener)
        }
    }

    private fun unregisterUserListener() {
        val ref = firebaseDbRepository.getUserReference(AppPreferences.loggedInUser!!.uid)
        ref.removeEventListener(userListener)
    }

    @OptIn(RestrictedAPI::class)
    fun logOut() {
        unregisterUserListener()
        authRepository.signOut()
        updateAuthenticationState(Utils.AuthenticationState.UNAUTHENTICATED)
        viewModelScope.launch(Dispatchers.IO) {
            todoDAO.deleteAllTodo()
        }
    }
}