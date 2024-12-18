package com.example.project_pemob_techie.ui.account

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_SESSION_START = "session_start_time"

    private const val SESSION_DURATION = 3 * 24 * 60 * 60 * 1000L

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isLoggedIn(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        val sessionStart = sharedPreferences.getLong(KEY_SESSION_START, 0L)

        if (sessionStart == 0L) {
            return false
        }

        val currentTime = System.currentTimeMillis()
        return if (currentTime - sessionStart < SESSION_DURATION) {
            sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        } else {
            endSession(context)
            false
        }
    }

    fun getUserId(context: Context): String? {
        return if (isLoggedIn(context)) {
            val sharedPreferences = getSharedPreferences(context)
            sharedPreferences.getString(KEY_USER_ID, null)
        } else {
            null
        }
    }

    fun startSession(context: Context, userId: String) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_SESSION_START, System.currentTimeMillis())
        editor.apply()
    }

    fun endSession(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
