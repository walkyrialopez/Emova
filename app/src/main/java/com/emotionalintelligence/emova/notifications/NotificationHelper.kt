package com.emotionalintelligence.emova.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.emotionalintelligence.emova.MainActivity

const val CHANNEL_ID = "emova_checkin_channel"
const val NOTIFICATION_ID = 1001


fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Daily Check-In",
        NotificationManager.IMPORTANCE_HIGH  // HIGH so it shows as heads-up
    ).apply {
        description = "Reminds you to do your daily emotional check-in"
        enableLights(true)
        enableVibration(true)
    }
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}

fun showCheckInNotification(context: Context) {
    // Make sure channel exists
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val messages = listOf(
        "How are you feeling right now? 💙" to "Take 2 minutes to check in with yourself.",
        "Your daily check-in is waiting 🌿" to "Name what you're feeling — it helps.",
        "Time to pause and reflect 🌿" to "Emotional awareness starts with a single check-in.",
        "Hey, how's your day going? 😊" to "Your feelings are worth noting.",
        "A moment for yourself 🌙" to "How are you really doing today?",
        "Breathe for 3 minutes today 🌬️" to "Open Emova and try a breathing exercise.",
        "Gratitude time! 🌟" to "What are 10 things you're grateful for today?",
        "Small check-in, big difference 💜" to "Name one emotion you've felt today.",
        "Your emotional health matters 🌈" to "A quick check-in can shift your whole day.",
        "Don't forget your gratitude diary ✨" to "Pause and count your blessings today."
    ).random()

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(messages.first)
        .setContentText(messages.second)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(NOTIFICATION_ID, notification)
}

class CheckInReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showCheckInNotification(context)
        // Reschedule for tomorrow — setExactAndAllowWhileIdle fires only once
        val prefs = context.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE)
        val hour   = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)
        NotificationScheduler.scheduleDailyReminder(context, hour, minute)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationScheduler.scheduleDailyReminder(context)
        }
    }
}