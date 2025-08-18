package com.pranshulgg.clockmaster

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "alarm_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val label = intent.getStringExtra("label") ?: "Alarm"
        val vibrate = intent.getBooleanExtra("vibrate", false)
        val sound = intent.getStringExtra("sound")
        val hour = intent.getIntExtra("hour", 7)
        val minute = intent.getIntExtra("minute", 0)
        val daysOfWeekFlutter = intent.getIntegerArrayListExtra("daysOfWeek") ?: arrayListOf()
        val recurring = intent.getBooleanExtra("recurring", false)

        val daysOfWeek = daysOfWeekFlutter.map {
            when (it) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                7 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
        }



        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        val isLocked = keyguardManager.isKeyguardLocked

        if (isLocked) {

            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("id", id)
                putExtra("label", label)
                putExtra("vibrate", vibrate)
                putExtra("sound", sound)
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                id,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            val channelId = "alarm_channel_locked"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Alarms Locked",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarm notifications on lock screen"
                    enableVibration(vibrate)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setSound(null, null)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, channelId)
            } else {
                Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH)
            }

            val notification = notificationBuilder
                .setSmallIcon(R.drawable.baseline_timer_24)
                .setContentTitle(label)
                .setContentText("Alarm is ringing")
                .setCategory(Notification.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setOngoing(true)
                .build()

            notificationManager.notify(id, notification)
        } else {

            val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
                putExtra("id", id)
                putExtra("label", label)
                putExtra("vibrate", vibrate)
                putExtra("sound", sound)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }


        }

        if (recurring || daysOfWeek.isNotEmpty()) {
            val now = Calendar.getInstance()
            val candidate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            fun advanceOneDay(c: Calendar) {
                c.add(Calendar.DAY_OF_MONTH, 1)
            }

            var nextTriggerMillis: Long = candidate.timeInMillis

            if (daysOfWeek.isNotEmpty()) {
                var attempts = 0
                while (attempts < 8) {
                    val weekday = candidate.get(Calendar.DAY_OF_WEEK)
                    if (daysOfWeek.contains(weekday) && candidate.timeInMillis > now.timeInMillis) {
                        break
                    }
                    advanceOneDay(candidate)
                    attempts++
                }
                nextTriggerMillis = candidate.timeInMillis
            } else {
                if (candidate.timeInMillis <= now.timeInMillis) {
                    candidate.add(Calendar.DAY_OF_MONTH, 1)
                }
                nextTriggerMillis = candidate.timeInMillis
            }

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.pranshulgg.alarm.ALARM_TRIGGER"
                putExtra("id", id)
                putExtra("label", label)
                putExtra("sound", sound)
                putExtra("hour", hour)
                putExtra("minute", minute)
                putIntegerArrayListExtra("daysOfWeek", ArrayList(daysOfWeekFlutter))
                putExtra("recurring", recurring)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingAlarm = PendingIntent.getBroadcast(context, id, alarmIntent, flags)

            // same showPending as MainActivity used:
            val showIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("fromAlarmIcon", true)
            }
            val showPending = PendingIntent.getActivity(context, id + 1000000, showIntent, flags)

            val info = AlarmManager.AlarmClockInfo(nextTriggerMillis, showPending)
            am.setAlarmClock(info, pendingAlarm)
        }
    }


}



