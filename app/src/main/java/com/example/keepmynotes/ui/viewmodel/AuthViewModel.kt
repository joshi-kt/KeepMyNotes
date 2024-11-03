package com.example.keepmynotes.ui.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.keepmynotes.data.local.dao.TodoDAO
import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import com.example.keepmynotes.model.User
import com.example.keepmynotes.utils.Utils
import com.example.keepmynotes.utils.Utils.logger
import com.example.keepmynotes.utils.Utils.setDeviceHashToUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseDbRepository: FirebaseDbRepository,
    todoDAO: TodoDAO
) : BaseViewModel(authRepository, todoDAO, firebaseDbRepository) {

    private val _authErrorText = MutableLiveData<String>()
    val authErrorText : LiveData<String>
        get() = _authErrorText
    private val _showLoading = MutableLiveData(false)
    val showLoading : LiveData<Boolean>
        get() = _showLoading

    fun createUser(name: String, email: String, password: String) {
        showLoading()
        logger("registering user name : $name , email : $email")

        viewModelScope.launch {
            try {
                val result = authRepository.createUserWithEmailAndPassword(email, password)
                result?.user?.let {
                    val user = User(it.uid, name, email, UUID.randomUUID().toString())
                    firebaseDbRepository.createUserInFirebaseDb(user)
                    saveUserLocally(user)
                    updateUiForUserLogin()
                    hideLoading()
                }
            } catch (e : Exception) {
                hideLoading()
                e.printStackTrace()
                e.localizedMessage?.let { updateErrorInUi(it) }
            }
        }
    }

    fun authenticateUsingGoogle(context : Context, isSignIn : Boolean) {
        viewModelScope.launch {
            try {
                showLoading()
                val credentialManager = CredentialManager.create(context)
                val credentialResponse = credentialManager.getCredential(context, authRepository.getCredentialRequest(context))
                val authCredential = authRepository.getAuthCredential(credentialResponse) ?: return@launch
                logger("got credentials")
                val firebaseUser = authRepository.signInWithFirebaseCredential(authCredential)?.user ?: return@launch
                var user = fetchUserFromFirebase(firebaseUser.uid)
                if (isSignIn && user == null) {
                    throw Exception("User not found, please sign up")
                } else if (!isSignIn && user != null) {
                    throw Exception("User already exists, please login")
                }
                user = user ?: User(uid = firebaseUser.uid, name = firebaseUser.displayName ?: "", email =  firebaseUser.email ?: "", deviceHash = UUID.randomUUID().toString())
                setDeviceHashToUser(user)
                firebaseDbRepository.createUserInFirebaseDb(user)
                saveUserLocally(user)
                syncTodosWithFirebase(user.uid)
                updateUiForUserLogin()
                hideLoading()
            } catch (e : Exception) {
                hideLoading()
                e.printStackTrace()
                e.localizedMessage?.let {
                    logger(it)
                    updateErrorInUi(it)
                }
            }
        }
    }

//    private suspend fun handleAuthToken(result: GetCredentialResponse) {
//        when(val credential = result.credential) {
//            is CustomCredential -> {
//                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
//                    val googleIdTokenCredential = GoogleIdTokenCredential
//                            .createFrom(credential.data)
//                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
//                    val authResult = authRepository.signInWithFirebaseCredential(authCredential)
//                    authResult?.user?.let {
//                        fetchUserFromFirebase()
//                    }
//                }
//            }
//        }
//
//    }

//    private fun handleSignInResult(result: GetCredentialResponse) {
//        showLoading()
//        when(val credential = result.credential) {
//            is CustomCredential -> {
//                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
//                    try {
//                        val googleIdTokenCredential = GoogleIdTokenCredential
//                            .createFrom(credential.data)
//                        val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
//                        viewModelScope.launch {
//                            val authResult = authRepository.signInWithFirebaseCredential(authCredential)
//                            authResult?.user?.let {

//                                fetchUserFromFirebase(it.uid, onUserFetched = { user ->
//                                    setDeviceHashToUser(user)

//                                    saveUserToFirebase(user, onUserSaved = {
//                                        saveUserLocally(user)
//                                        syncTodosWithFirebase(user.uid, onTodosFetched = {
//                                            updateUiForUserLogin()
//                                            hideLoading()
//                                        }, onFailed = {
//                                            hideLoading()
//                                        })
//                                    }, onFailed = {
//                                        hideLoading()
//                                    })

//                                }, onFailed = {
//
//                                    val user = User(uid = it.uid, name = it.displayName ?: "", email =  it.email ?: "", deviceHash = UUID.randomUUID().toString())

//                                    saveUserToFirebase(user, onUserSaved = {
//                                        saveUserLocally(user)
//                                        syncTodosWithFirebase(user.uid, onTodosFetched = {
//                                            updateUiForUserLogin()
//                                            hideLoading()
//                                        }, onFailed = {
//                                            hideLoading()
//                                        })
//                                    }, onFailed = {
//                                        hideLoading()
//                                    })

//                                })
//
//                            }
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//        }
//    }

    fun signIn(email: String, password: String) {
        showLoading()

        viewModelScope.launch {
            try {
                val result = authRepository.signInWithEmailAndPassword(email, password)
                result?.user?.let {
                    val user = fetchUserFromFirebase(it.uid)
                    user?.let {
                        setDeviceHashToUser(user)
                        firebaseDbRepository.createUserInFirebaseDb(user)
                        saveUserLocally(user)
                        syncTodosWithFirebase(user.uid)
                    }
                }
            } catch (e : Exception){
                hideLoading()
                e.message?.let {
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


    fun resetErrorText() {
        _authErrorText.value = ""
    }

    private suspend fun fetchUserFromFirebase(uid: String) : User? {
        val dataSnapshot = firebaseDbRepository.fetchUserFromFirebaseDb(uid)
        dataSnapshot?.let {
            val user = it.getValue(User::class.java)
            return user
        }
        return null
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
}