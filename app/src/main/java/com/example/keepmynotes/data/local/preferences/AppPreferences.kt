package com.example.keepmynotes.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.keepmynotes.model.User
import com.example.keepmynotes.utils.Utils
import com.example.keepmynotes.utils.Utils.APP_PREFERENCE_NAME
import com.example.keepmynotes.utils.Utils.IS_LOGGED_IN
import com.example.keepmynotes.utils.Utils.LOGGED_IN
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AppPreferences {
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private val IS_LOGGED_IN_PREF = Pair(IS_LOGGED_IN, false)
    private val LOGGED_IN_PREF = Pair(LOGGED_IN, null)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(APP_PREFERENCE_NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    var isLoggedIn: Boolean
        get() = preferences.getBoolean(IS_LOGGED_IN_PREF.first, IS_LOGGED_IN_PREF.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_LOGGED_IN_PREF.first, value)
        }

    var loggedInUser : User?
        get() {
            val jsonStringUser = preferences.getString(LOGGED_IN_PREF.first, LOGGED_IN_PREF.second)
            return jsonStringUser?.let { Json.decodeFromString<User>(it) }
        }
        set(value) = preferences.edit {
            val jsonData = Json.encodeToString(value)
            it.putString(LOGGED_IN_PREF.first, jsonData)
        }
}