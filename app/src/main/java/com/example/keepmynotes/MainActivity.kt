package com.example.keepmynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.keepmynotes.ui.screens.AuthScreen
import com.example.keepmynotes.ui.screens.ToDoListPage
import com.example.keepmynotes.utils.Utils.SCREEN_AUTH
import com.example.keepmynotes.utils.Utils.SCREEN_NOTES
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = SCREEN_AUTH) {
                composable(SCREEN_AUTH) { 
                    AuthScreen(navController)
                }
                composable(SCREEN_NOTES) { 
                    ToDoListPage(navController)
                }
            }
        }

    }
}