package com.emotionalintelligence.emova.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object NotificationScheduler {

    private const val REQ_REMINDER = 0
    private const val REQ_TIP = 1

    // ── Daily check-in reminder ──────────────────────────────────────────────
    fun scheduleDailyReminder(context: Context, hourOfDay: Int = 20, minute: Int = 0) {
        createNotificationChannel(context)
        schedule(context, REQ_REMINDER, CheckInReminderReceiver::class.java, hourOfDay, minute)
    }

    fun cancelReminder(context: Context) = cancel(context, REQ_REMINDER, CheckInReminderReceiver::class.java)

    // ── Daily tip (a different time than the reminder) ────────────────────────
    fun scheduleDailyTip(context: Context, hourOfDay: Int = 11, minute: Int = 0) {
        createTipChannel(context)
        schedule(context, REQ_TIP, DailyTipReceiver::class.java, hourOfDay, minute)
    }

    fun cancelDailyTip(context: Context) = cancel(context, REQ_TIP, DailyTipReceiver::class.java)

    // ── Shared ───────────────────────────────────────────────────────────────
    private fun pendingIntent(context: Context, requestCode: Int, receiver: Class<*>): PendingIntent =
        PendingIntent.getBroadcast(
            context, requestCode, Intent(context, receiver),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun schedule(context: Context, requestCode: Int, receiver: Class<*>, hourOfDay: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context, requestCode, receiver)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If time already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        // Android 12+ requires the canScheduleExactAlarms check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pending
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
        }
    }

    private fun cancel(context: Context, requestCode: Int, receiver: Class<*>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, requestCode, receiver))
    }
}
