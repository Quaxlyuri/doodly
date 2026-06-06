package com.doodly.app.data.repository

import android.content.Context

class SettingsRepository(context: Context) {
    private val preferences =
        context.getSharedPreferences("doodly_settings", Context.MODE_PRIVATE)

    var reminderEnabled: Boolean
        get() = preferences.getBoolean("reminder_enabled", false)
        set(value) = preferences.edit().putBoolean("reminder_enabled", value).apply()

    var reminderHour: Int
        get() = preferences.getInt("reminder_hour", 21)
        set(value) = preferences.edit().putInt("reminder_hour", value).apply()

    var reminderMinute: Int
        get() = preferences.getInt("reminder_minute", 0)
        set(value) = preferences.edit().putInt("reminder_minute", value).apply()

    var cloudBackupEnabled: Boolean
        get() = preferences.getBoolean("cloud_backup_enabled", false)
        set(value) = preferences.edit().putBoolean("cloud_backup_enabled", value).apply()
}
