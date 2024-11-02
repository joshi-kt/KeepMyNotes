package com.example.keepmynotes.ui.viewmodel;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keepmynotes.data.repository.AuthRepository;
import com.example.keepmynotes.data.repository.FirebaseDbRepository;

class TodoViewModelFactory(private val authRepository:AuthRepository, private val firebaseDbRepository:FirebaseDbRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(authRepository, firebaseDbRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
