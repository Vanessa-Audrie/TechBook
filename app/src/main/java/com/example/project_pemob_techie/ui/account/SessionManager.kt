package com.example.project_pemob_techie.ui.account

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Check if user is logged in
    fun isLoggedIn(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Get the logged-in user ID
    fun getUserId(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    // Start the user session
    fun startSession(context: Context, userId: String) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    // End the user session (logout)
    fun endSession(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()  // This will clear all session data
        editor.apply()
    }
}