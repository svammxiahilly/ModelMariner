package com.pranshulgg.clockmaster.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.START_STICKY
import androidx.core.app.ServiceCompat.startForeground
import com.pranshulgg.clockmaster.R
import android.os.Vibrator
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.ui.graphics.Color
import com.pranshulgg.clockmaster.MainActivity
import java.util.Date

class AlarmServiceForeground : Service() {

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
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "clockmaster:alarm_wakelock")
        wakeLock?.acquire(10 * 60 * 1000L)
        vibrator = getSystemService(Vibrator::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val label = intent?.getStringExtra("label") ?: "Alarm"
        val alarmId = intent?.getIntExtra("id", -1) ?: -1
        val vibrate = intent?.getBooleanExtra("vibrate", false) ?: false
        val soundUriString = intent?.getStringExtra("sound")
        val soundUri =
            soundUriString?.let { Uri.parse(it) } ?: Settings.System.DEFAULT_ALARM_ALERT_URI

        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_SNOOZE -> {
                stopSelf()
//                val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000
                val snoozeTime = System.currentTimeMillis() + 10_000
                val alarmManager =
                    getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

                val snoozeIntent = Intent(this, AlarmServiceForeground::class.java).apply {
                    putExtra("label", label)
                    putExtra("id", alarmId)
                    putExtra("vibrate", vibrate)
                    putExtra("sound", soundUri.toString())
                }

                val snoozePendingIntent = PendingIntent.getService(
                    this,
                    alarmId,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    snoozePendingIntent
                )


                return START_NOT_STICKY
            }
        }

        var fullScreenIntent = Intent(this, MainActivity::class.java)
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent =
            Intent(this, AlarmServiceForeground::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            alarmId,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeIntent =
            Intent(this, AlarmServiceForeground::class.java).apply { action = ACTION_SNOOZE }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            alarmId,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentTime = DateFormat.getTimeFormat(this).format(Date())
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm • $currentTime")
            .setContentText(
                "Ringing ${
                    if (label.isNotEmpty()) {
                        "• $label"
                    } else {
                        ""
                    }
                }"
            )
            .setSmallIcon(R.drawable.alarm_outlined)
            .setCategory(Notification.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.alarm_outlined, "SNOOZE", snoozePendingIntent)
            .addAction(R.drawable.alarm_outlined, "STOP", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@AlarmServiceForeground, soundUri)
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

        if (vibrate) {
            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
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
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Foreground Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alarm service channel" }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
