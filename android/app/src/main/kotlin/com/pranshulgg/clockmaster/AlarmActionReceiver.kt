package com.pranshulgg.clockmaster

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmActionReceiver : BroadcastReceiver() {
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("id", 0)
        val action = intent.action ?: return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when (action) {
            "com.pranshulgg.clockmaster.ACTION_SNOOZE" -> {
                val label = intent.getStringExtra("label") ?: "Snoozed Alarm"
                val vibrate = intent.getBooleanExtra("vibrate", false)
                val sound = intent.getStringExtra("sound")


                val stopIntent = Intent(context, AlarmForegroundService::class.java)
                stopIntent.setAction(AlarmForegroundService.ACTION_STOP)
                context.startService(stopIntent)


                val trigger = System.currentTimeMillis() + 5 * 60 * 1000L

                val alarmIntent = Intent(context, AlarmForegroundService::class.java).apply {
                    putExtra("id", alarmId)
                    putExtra("label", label)
                    putExtra("vibrate", vibrate)
                    putExtra("sound", sound)
                }
                val pending = PendingIntent.getService(
                    context,
                    alarmId,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, trigger, pending)
                }
            }

            "com.pranshulgg.clockmaster.ACTION_STOP" -> {
                val stopIntent = Intent(context, AlarmForegroundService::class.java)
                stopIntent.setAction(AlarmForegroundService.ACTION_STOP)
                context.startService(stopIntent)
            }
        }

    }
    }
