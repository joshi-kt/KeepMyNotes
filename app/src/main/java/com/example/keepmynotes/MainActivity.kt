package com.example.keepmynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import com.example.keepmynotes.ui.screens.AuthScreen
import com.example.keepmynotes.ui.viewmodel.TodoViewModel
import com.example.keepmynotes.ui.screens.ToDoListPage
import com.example.keepmynotes.ui.viewmodel.AuthViewModel
import com.example.keepmynotes.ui.viewmodel.AuthViewModelFactory
import com.example.keepmynotes.ui.viewmodel.TodoViewModelFactory
import com.example.keepmynotes.utils.Utils.SCREEN_AUTH
import com.example.keepmynotes.utils.Utils.SCREEN_NOTES

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val authRepository = AuthRepository()
        val firebaseDbRepository = FirebaseDbRepository()

        val authViewModel = ViewModelProvider(this, AuthViewModelFactory(authRepository, firebaseDbRepository))[AuthViewModel::class.java]
        val todoViewModel = ViewModelProvider(this, TodoViewModelFactory(authRepository, firebaseDbRepository))[TodoViewModel::class.java]

        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = SCREEN_AUTH) {
                composable(SCREEN_AUTH) { 
                    AuthScreen(navController, viewModel = authViewModel)
                }
                composable(SCREEN_NOTES) { 
                    ToDoListPage(viewModel = todoViewModel)
                }
            }
        }

    }
}