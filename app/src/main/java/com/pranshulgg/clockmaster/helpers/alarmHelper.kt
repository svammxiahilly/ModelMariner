package com.pranshulgg.clockmaster.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.pranshulgg.clockmaster.MainActivity
import com.pranshulgg.clockmaster.receiver.AlarmReceiver
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {


    fun generateUniqueId(label: String, dayOfWeek: Int): Int {
        return (label.hashCode() + dayOfWeek)
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleAlarm(
        context: Context,
        id: Int,
        dayOfWeek: List<Int>,
        hour: Int,
        minute: Int,
        label: String,
        soundUri: String? = null,
        vibrate: Boolean = false,
        snoozeTime: Int? = null
    ) {


        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("label", label)
            putExtra("label", label)
            putExtra("dayOfWeek", ArrayList(dayOfWeek))
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("id", id)
            putExtra("sound", soundUri)
            putExtra("vibrate", vibrate)
            putExtra("snoozeTime", snoozeTime)

        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = LocalDateTime.now()
        var triggerTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (triggerTime.isBefore(now)) {
            triggerTime = triggerTime.plusDays(1)
        }


        val triggerMillis = triggerTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context, id, showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val info = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)

    }

    fun cancelAlarm(
        context: Context,
        id: Int,
        dayOfWeek: List<Int> = emptyList(),
        hour: Int = 0,
        minute: Int = 0,
        label: String = ""
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("label", label)
            putExtra("dayOfWeek", ArrayList(dayOfWeek))
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("id", id)

        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextTrigger(dayOfWeek: Int, hour: Int, minute: Int): LocalDateTime {
        val now = LocalDateTime.now()
        var trigger = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

        val todayDayOfWeek = now.dayOfWeek.value % 7
        var daysUntilNext = (dayOfWeek - todayDayOfWeek + 7) % 7
        if (daysUntilNext == 0 && trigger.isBefore(now)) {
            daysUntilNext = 7
        }

        return trigger.plusDays(daysUntilNext.toLong())
    }
}

