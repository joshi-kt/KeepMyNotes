package com.example.keepmynotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository

class AuthViewModelFactory(private val authRepository: AuthRepository, private val firebaseDbRepository: FirebaseDbRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, firebaseDbRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}