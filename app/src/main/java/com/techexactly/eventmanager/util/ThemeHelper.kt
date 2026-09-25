package com.techexactly.eventmanager.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

/** Persists and applies the light / dark mode choice (defaults to following the system). */
object ThemeHelper {
    private const val PREFS = "settings"
    private const val KEY_NIGHT_MODE = "night_mode"

    fun applySaved(context: Context) {
        val mode = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun toggle(context: Context) {
        val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val newMode = if (isNight) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_NIGHT_MODE, newMode).apply()
        AppCompatDelegate.setDefaultNightMode(newMode)
    }
}
