package com.pranshulgg.clockmaster.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pranshulgg.clockmaster.R
import java.util.Collections
import java.util.HashSet

class TimerAlarmService : Service() {

    companion object {
        private const val FOREGROUND_CHANNEL_ID = "timer_alarm_fg_channel"
        private const val FOREGROUND_NOTIF_ID = 2001

        private const val ALARM_CHANNEL_ID = "timer_alarm_channel"
        private const val ALARM_NOTIF_ID_BASE = 3000

        const val EXTRA_TIMER_ID = "extra_timer_id"
        const val EXTRA_LABEL = "extra_label"
        const val ACTION_STOP = "com.pranshulgg.clockmaster.ACTION_STOP_ALARM"

        private val activeAlarms: MutableSet<String> = Collections.synchronizedSet(HashSet())

        fun startAlarm(context: Context, timerId: String, label: String?) {
            val i = Intent(context, TimerAlarmService::class.java).apply {
                putExtra(EXTRA_TIMER_ID, timerId)
                putExtra(EXTRA_LABEL, label)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stopAlarm(context: Context, timerId: String) {
            val i = Intent(context, TimerAlarmService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_TIMER_ID, timerId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }
    }


    private var mediaPlayer: MediaPlayer? = null
    private var currentTimerId: String? = null

    override fun onCreate() {
        super.onCreate()
        createChannels()
        val fgNotif = NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Timer alarm service")
            .setContentText("Managing alarms")
            .setSmallIcon(R.drawable.hourglass_empty)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(FOREGROUND_NOTIF_ID, fgNotif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            val id = intent.getStringExtra(EXTRA_TIMER_ID)
            stopAlarmForId(id)
            if (activeAlarms.isEmpty()) stopSelf()
            return START_NOT_STICKY
        }

        val id = intent?.getStringExtra(EXTRA_TIMER_ID) ?: return START_NOT_STICKY
        val label = intent.getStringExtra(EXTRA_LABEL) ?: ""

        if (activeAlarms.contains(id)) {
            return START_STICKY
        }

        activeAlarms.add(id)
        currentTimerId = id

        startAlarmSound()

        postAlarmNotification(id, label)

        return START_STICKY
    }

    private fun startAlarmSound() {
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getActualDefaultRingtoneUri(
                    this,
                    RingtoneManager.TYPE_NOTIFICATION
                )
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@TimerAlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
            mediaPlayer = mp
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun postAlarmNotification(timerId: String, label: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifId = ALARM_NOTIF_ID_BASE + (timerId.hashCode() and 0xffff)

        val stopIntent = Intent(this, TimerAlarmService::class.java).apply {
            action = ACTION_STOP
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        val stopPending = PendingIntent.getService(
            this, notifId + 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val deleteIntent = Intent(this, TimerAlarmService::class.java).apply {
            action = ACTION_STOP
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        val deletePending = PendingIntent.getService(
            this, notifId + 2, deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val contentText = "Timer completed $label"

        val n = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setContentTitle("Timer completed")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setSmallIcon(R.drawable.notifications_active)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPending
            )
            .setColor(ContextCompat.getColor(applicationContext, R.color.notification_primary))
            .setColorized(true)
            .setAutoCancel(true)
            .setDeleteIntent(deletePending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setOnlyAlertOnce(false)
            .build()

        nm.notify(notifId, n)
    }

    private fun stopAlarmForId(timerId: String?) {
        if (timerId == null) return
        activeAlarms.remove(timerId)

        mediaPlayer?.let {
            try {
                it.stop()
            } catch (e: Exception) {
            }
            try {
                it.release()
            } catch (_: Exception) {
            }
        }
        mediaPlayer = null

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifId = ALARM_NOTIF_ID_BASE + (timerId.hashCode() and 0xffff)
        nm.cancel(notifId)

        if (activeAlarms.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        stopForeground(STOP_FOREGROUND_REMOVE)
        activeAlarms.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            val fgChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Alarm service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground helper for alarm service"
                setShowBadge(false)
            }
            nm.createNotificationChannel(fgChannel)

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Timer alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for completed timers (alarms)"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            nm.createNotificationChannel(alarmChannel)
        }
    }
}
