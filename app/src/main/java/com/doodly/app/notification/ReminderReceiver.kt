package com.doodly.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.doodly.app.data.repository.SettingsRepository

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val settings = SettingsRepository(context)
        if (!settings.reminderEnabled) return
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            showReminderNotification(context)
        }
        ReminderScheduler.schedule(context, settings.reminderHour, settings.reminderMinute)
    }
}
