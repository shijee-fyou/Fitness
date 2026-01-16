package com.example.fitness_demo.data

import android.content.Context

object SettingsStore {
    private const val PREFS = "settings_store"
    private const val KEY_DEFAULT_REST_SECONDS = "default_rest_seconds"
    private const val DEFAULT_REST_SECONDS = 60

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getDefaultRestSeconds(context: Context): Int =
        prefs(context).getInt(KEY_DEFAULT_REST_SECONDS, DEFAULT_REST_SECONDS)

    fun setDefaultRestSeconds(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_DEFAULT_REST_SECONDS, value).apply()
    }
}
