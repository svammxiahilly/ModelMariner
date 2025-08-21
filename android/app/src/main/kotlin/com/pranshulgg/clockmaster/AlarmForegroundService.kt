package com.pranshulgg.clockmaster

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.core.app.NotificationCompat
import android.app.Service

class AlarmForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "alarm_foreground_channel"
        const val NOTIFICATION_ID = 10001

        const val ACTION_STOP = "com.pranshulgg.clockmaster.ACTION_STOP"
        const val ACTION_SNOOZE = "com.pranshulgg.clockmaster.ACTION_SNOOZE"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "clockmaster:alarm_service_wakelock")
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val alarmId = intent?.getIntExtra("id", 0) ?: 0
        val label = intent?.getStringExtra("label") ?: "Alarm"
        val vibrate = intent?.getBooleanExtra("vibrate", false) ?: false
        val soundUriString = intent?.getStringExtra("sound")
        val soundUri = soundUriString?.let { Uri.parse(it) } ?: Settings.System.DEFAULT_ALARM_ALERT_URI
        val snooze = intent?.getIntExtra("snoozeTime", 5)


        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }


        val snoozeIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("id", alarmId)
            putExtra("label", label)
            putExtra("vibrate", vibrate)
            putExtra("sound", soundUri.toString())
            putExtra("snoozeTime", snooze)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val stopIntent = Intent(this, AlarmForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            alarmId,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("id", alarmId)
            putExtra("label", label)
            putExtra("vibrate", vibrate)
            putExtra("sound", soundUri.toString())
            putExtra("snoozeTime", snooze)

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pattern = longArrayOf(0, 500, 500)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(label)
            .setContentText("Alarm ringing")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze", snoozePendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setVibrate(pattern)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmForegroundService, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }


        if (vibrate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()

        wakeLock?.release()
        wakeLock = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Foreground Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm service channel"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
