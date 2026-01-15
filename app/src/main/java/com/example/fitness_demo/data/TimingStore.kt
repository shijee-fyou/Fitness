package com.example.fitness_demo.data

import android.content.Context
import android.os.SystemClock

/**
 * Lightweight timing persistence using SharedPreferences (Stage A).
 * Stores per-session active duration and last resume baseline.
 */
object TimingStore {
    private const val PREFS = "timing_store"
    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getActiveMs(context: Context, sessionId: Int): Long =
        prefs(context).getLong("activeMs_$sessionId", 0L)

    fun setActiveMs(context: Context, sessionId: Int, value: Long) {
        prefs(context).edit().putLong("activeMs_$sessionId", value).apply()
    }

    fun getLastResumeElapsed(context: Context, sessionId: Int): Long? {
        val v = prefs(context).getLong("lastResume_$sessionId", -1L)
        return if (v < 0L) null else v
    }

    fun setLastResumeElapsed(context: Context, sessionId: Int, value: Long?) {
        val e = prefs(context).edit()
        if (value == null) e.remove("lastResume_$sessionId") else e.putLong("lastResume_$sessionId", value)
        e.apply()
    }

    fun isRunning(context: Context, sessionId: Int): Boolean =
        prefs(context).getBoolean("running_$sessionId", false)

    fun setRunning(context: Context, sessionId: Int, running: Boolean) {
        prefs(context).edit().putBoolean("running_$sessionId", running).apply()
    }
}

