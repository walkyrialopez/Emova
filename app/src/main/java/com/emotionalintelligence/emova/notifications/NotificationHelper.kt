package com.emotionalintelligence.emova.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.emotionalintelligence.emova.MainActivity
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.util.createLocalizedContext
import com.emotionalintelligence.emova.util.loadLanguage
import java.util.Calendar

const val CHANNEL_ID = "emova_checkin_channel"
const val NOTIFICATION_ID = 1001
const val TIP_CHANNEL_ID = "emova_tip_channel"
const val TIP_NOTIFICATION_ID = 1002

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.notif_channel_name),
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = context.getString(R.string.notif_channel_desc)
        enableLights(true)
        enableVibration(true)
    }
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}

fun showCheckInNotification(context: Context) {
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Use the language chosen inside the app, not the phone's language
    val localized = context.createLocalizedContext(loadLanguage(context))
    val titles = localized.resources.getStringArray(R.array.notif_titles)
    val bodies  = localized.resources.getStringArray(R.array.notif_bodies)
    val index   = (System.currentTimeMillis() / 86_400_000L % titles.size).toInt()

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(titles[index])
        .setContentText(bodies[index])
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(NOTIFICATION_ID, notification)
}

fun createTipChannel(context: Context) {
    val localized = context.createLocalizedContext(loadLanguage(context))
    val channel = NotificationChannel(
        TIP_CHANNEL_ID,
        localized.getString(R.string.notif_tip_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT   // a tip should not be as loud as the reminder
    ).apply { description = localized.getString(R.string.notif_tip_channel_desc) }
    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
}

/** Same tip that the Today screen shows: day of the year modulo the number of tips. */
fun showDailyTipNotification(context: Context) {
    createTipChannel(context)

    val localized = context.createLocalizedContext(loadLanguage(context))
    val titles = localized.resources.getStringArray(R.array.wellness_tip_titles)
    val bodies = localized.resources.getStringArray(R.array.wellness_tip_bodies)
    if (titles.isEmpty() || bodies.isEmpty()) return
    val index = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % titles.size
    val body = bodies[index.coerceAtMost(bodies.lastIndex)]

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, TIP_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("💡 " + titles[index])
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .notify(TIP_NOTIFICATION_ID, notification)
}

class DailyTipReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("tip_notif_enabled", false)) return
        showDailyTipNotification(context)
        NotificationScheduler.scheduleDailyTip(
            context, prefs.getInt("tip_hour", 11), prefs.getInt("tip_minute", 0)
        )
    }
}

class CheckInReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showCheckInNotification(context)
        val prefs  = context.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE)
        val hour   = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)
        NotificationScheduler.scheduleDailyReminder(context, hour, minute)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore alarms only for what the person actually turned on, at the time they chose.
            val prefs = context.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE)
            if (prefs.getBoolean("notif_enabled", false)) {
                NotificationScheduler.scheduleDailyReminder(
                    context, prefs.getInt("reminder_hour", 20), prefs.getInt("reminder_minute", 0)
                )
            }
            if (prefs.getBoolean("tip_notif_enabled", false)) {
                NotificationScheduler.scheduleDailyTip(
                    context, prefs.getInt("tip_hour", 11), prefs.getInt("tip_minute", 0)
                )
            }
        }
    }
}

