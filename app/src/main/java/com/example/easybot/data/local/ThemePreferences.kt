package com.example.easybot.data.local


import android.content.Context
import android.content.Context.MODE_PRIVATE

object ThemePreferences {
    private const val PREFS_NAME = "easybot_settings"
    private const val KEY_DARK_THEME = "dark_theme"

    fun getTheme(context: Context, defaultValue: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_THEME, defaultValue)
    }

    fun saveTheme(context: Context, isDarkTheme: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_THEME, isDarkTheme).apply()
    }
}