package com.doodly.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.doodly.app.MainActivity

const val CHANNEL_ID = "doodly_reminder_channel"
const val REMINDER_NOTIFICATION_ID = 1001

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Doodly 알림",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "오늘의 일기 작성을 알려주는 채널입니다."
    }
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}

fun showReminderNotification(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("doodly://write"),
        context,
        MainActivity::class.java
    ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        2001,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val text = "짧게 한두 문장만 남겨도 오늘이 그림으로 기록돼요."
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("오늘 하루는 어땠나요?")
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
}
