package com.example.keepmynotes.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import com.example.keepmynotes.model.User
import com.example.keepmynotes.utils.RestrictedAPI
import com.example.keepmynotes.utils.Utils
import com.example.keepmynotes.utils.Utils.logger
import com.example.keepmynotes.utils.Utils.setDeviceHashToUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository, private val firebaseDbRepository: FirebaseDbRepository) : ViewModel() {

    private val _authenticationState = MutableLiveData(if (AppPreferences.isLoggedIn) Utils.AuthenticationState.AUTHENTICATED else Utils.AuthenticationState.UNAUTHENTICATED)
    val authenticationState : LiveData<Utils.AuthenticationState>
        get() = _authenticationState
    private val _authErrorText = MutableLiveData<String>()
    val authErrorText : LiveData<String>
        get() = _authErrorText
    private val _showLoading = MutableLiveData(false)
    val showLoading : LiveData<Boolean>
        get() = _showLoading

    fun createUser(name: String, email: String, password: String) {
        showLoading()
        logger("registering user name : $name , email : $email")
        authRepository.createUserWithEmailAndPassword(email, password).addOnCompleteListener { userCreationTask ->
            if (userCreationTask.isSuccessful) {
                logger("create user API result success : ${userCreationTask.isSuccessful}" )
                val user = userCreationTask.result.user?.let {
                    User(it.uid, name, email, UUID.randomUUID().toString())
                }
                logger("saving user $user")
                saveUserToFirebase(user!!, onUserSaved = {
                    saveUserLocally(user)
                    updateUiForUserLogin()
                    hideLoading()
                }, onFailed = {
                    hideLoading()
                })
            } else {
                hideLoading()
                logger("user creation task failed : ${userCreationTask.exception?.message}")
                userCreationTask.exception?.message?.let {
                    updateErrorInUi(it)
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        showLoading()
        authRepository.signInWithEmailAndPassword(email, password).addOnCompleteListener { userSignInTask ->
            if (userSignInTask.isSuccessful) {
                userSignInTask.result.user?.let { firebaseUser ->
                    fetchUserFromFirebase(firebaseUser.uid, onUserFetched = { user ->
                        setDeviceHashToUser(user)
                        saveUserToFirebase(user, onUserSaved = {
                            saveUserLocally(user)
                            updateUiForUserLogin()
                            hideLoading()
                        }, onFailed = {
                            hideLoading()
                        })
                    }, onFailed = {
                        hideLoading()
                    })
                }
            } else {
                hideLoading()
                userSignInTask.exception?.message?.let {
                    updateErrorInUi(it)
                }
            }
        }
    }

    private fun showLoading() {
        _showLoading.value = true
    }

    private fun hideLoading() {
        _showLoading.value = false
    }

    private fun signOut() {
        logger("logging out")
        authRepository.signOut()
    }

    private fun updateErrorInUi(message : String) {
        _authErrorText.value = message
        updateAuthenticationState(Utils.AuthenticationState.UNAUTHENTICATED)
    }

    private fun updateUiForUserLogin() {
        updateAuthenticationState(Utils.AuthenticationState.AUTHENTICATED)
    }

    private fun updateAuthenticationState(state : Utils.AuthenticationState){
        _authenticationState.value = state
    }

    fun resetErrorText() {
        _authErrorText.value = ""
    }

    private fun saveUserToFirebase(user: User, onUserSaved : () -> Unit, onFailed : () -> Unit) {
        firebaseDbRepository.createUserInFirebaseDb(user).addOnCompleteListener { userSavingTask ->
            if (userSavingTask.isSuccessful) {
                logger("saving user result success : ${userSavingTask.isSuccessful}" )
                onUserSaved()
            } else {
                logger("user saving task failed : ${userSavingTask.exception?.message}")
                logger("logging out")
                signOut()
                onFailed()
                userSavingTask.exception?.message?.let {
                    updateErrorInUi(it)
                }
            }
        }
    }


    private fun fetchUserFromFirebase(uid: String, onUserFetched : (User) -> Unit, onFailed: () -> Unit) {
        firebaseDbRepository.fetchUserFromFirebaseDb(uid)
            .addOnSuccessListener { dataSnapshot ->
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    logger("Fetched user: $user")
                    onUserFetched(user)
                } else {
                    logger("User not found in the database.")
                    onFailed()
                }
            }
            .addOnFailureListener { exception ->
                logger("Error fetching user: ${exception.message}")
                updateErrorInUi(exception.message ?: "Error fetching user")
                onFailed()
            }
    }

    private fun saveUserLocally(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            AppPreferences.loggedInUser = user
            AppPreferences.isLoggedIn = true
            withContext(Dispatchers.Main) {
                updateUiForUserLogin()
            }
        }
    }

    fun logOut() {
        authRepository.signOut()
        updateAuthenticationState(Utils.AuthenticationState.UNAUTHENTICATED)
    }
}