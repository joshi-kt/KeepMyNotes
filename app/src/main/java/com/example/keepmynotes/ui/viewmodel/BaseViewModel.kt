package com.example.keepmynotes.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.utils.Utils

open class BaseViewModel (
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _authenticationState = MutableLiveData(if (AppPreferences.isLoggedIn) Utils.AuthenticationState.AUTHENTICATED else Utils.AuthenticationState.UNAUTHENTICATED)
    val authenticationState : LiveData<Utils.AuthenticationState>
        get() = _authenticationState

    fun updateAuthenticationState(state : Utils.AuthenticationState) {
        _authenticationState.value = state
    }

    open fun logOut() {
        authRepository.signOut()
        updateAuthenticationState(Utils.AuthenticationState.UNAUTHENTICATED)
    }

}