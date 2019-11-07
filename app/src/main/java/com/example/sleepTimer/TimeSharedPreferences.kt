package com.example.sleepTimer

import android.content.Context
import android.content.SharedPreferences

class TimeSharedPreferences(context: Context) {

    val PREFS_FILENAME = "times"
    val PREF_KEY_HOUR = "hour"
    val PREF_KEY_MINUTE = "minute"

    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var hour: Int
        get() = prefs.getInt(PREF_KEY_HOUR, 0)
        set(value) = prefs.edit().putInt(PREF_KEY_HOUR, value).apply()

    var minute: Int
        get() = prefs.getInt(PREF_KEY_MINUTE, 0)
        set(value) = prefs.edit().putInt(PREF_KEY_MINUTE, value).apply()
}