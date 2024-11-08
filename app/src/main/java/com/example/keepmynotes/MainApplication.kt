package com.example.keepmynotes

import android.app.Application
import androidx.room.Room
import com.example.keepmynotes.data.local.database.TodoDatabase
import com.example.keepmynotes.data.local.preferences.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
    }

}