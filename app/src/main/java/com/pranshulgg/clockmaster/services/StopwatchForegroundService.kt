package com.pranshulgg.clockmaster.services

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.receiver.NotificationActionReceiver
import com.pranshulgg.clockmaster.repository.StopwatchRepository
import kotlinx.coroutines.flow.combine

class StopwatchForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        const val CHANNEL_ID = "stopwatch_channel"
        const val NOTIF_ID = 101

        const val ACTION_START_FOREGROUND = "action_start_foreground"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_RESUME = "action_resume"
        const val ACTION_LAP = "action_lap"
        const val ACTION_RESET = "action_reset"
        const val ACTION_STOP = "action_stop"
    }

    private var isForegroundStarted = false

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        super.onCreate()
        createChannel()

        scope.launch {
            combine(
                StopwatchRepository.elapsedMs,
                StopwatchRepository.isRunning
            ) { elapsed, running -> elapsed to running }
                .collect { (elapsed, running) ->
                    updateNotification(elapsed, running)
                }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                isForegroundStarted = true
                startForeground(
                    NOTIF_ID,
                    buildNotification(StopwatchRepository.elapsedMs.value, isRunning = true)
                )
                StopwatchRepository.start()
            }

            ACTION_PAUSE -> {
                StopwatchRepository.pause()
                updateNotification(StopwatchRepository.elapsedMs.value, false)
            }

            ACTION_RESUME -> {
                StopwatchRepository.start()
                updateNotification(StopwatchRepository.elapsedMs.value, true)
            }

            ACTION_LAP -> {
                StopwatchRepository.lap()
                updateNotification(
                    StopwatchRepository.elapsedMs.value,
                    StopwatchRepository.isRunning.value
                )
            }

            ACTION_RESET -> {
                StopwatchRepository.pause()
                StopwatchRepository.reset()
                stopForeground(STOP_FOREGROUND_REMOVE)
                scope.cancel()
                isForegroundStarted = false
                stopSelf()
            }

            ACTION_STOP -> {
                StopwatchRepository.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                isForegroundStarted = false
                stopSelf()
            }

            else -> {
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stopwatch",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Stopwatch running"
            }
            nm.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(elapsedMs: Long, isRunning: Boolean) {
        if (!isForegroundStarted) return
        val notif = buildNotification(elapsedMs, isRunning)

        if (isForegroundStarted) {
            startForeground(NOTIF_ID, notif)
        } else {
            NotificationManagerCompat.from(this).notify(NOTIF_ID, notif)
        }
    }


    private fun actionPendingIntent(action: String): PendingIntent {
        val i = Intent(this, NotificationActionReceiver::class.java).apply { this.action = action }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT

        return PendingIntent.getBroadcast(this, action.hashCode(), i, flags)
    }


    private fun buildNotification(elapsedMs: Long, isRunning: Boolean): Notification {
        val format = formatElapsed(elapsedMs)

        val pauseResumeAction = if (isRunning) {
            NotificationCompat.Action.Builder(0, "Pause", actionPendingIntent(ACTION_PAUSE)).build()
        } else {
            NotificationCompat.Action.Builder(0, "Resume", actionPendingIntent(ACTION_RESUME))
                .build()
        }

        val lapAction =
            NotificationCompat.Action.Builder(0, "Lap", actionPendingIntent(ACTION_LAP)).build()
        val stopAction =
            NotificationCompat.Action.Builder(0, "Reset", actionPendingIntent(ACTION_RESET)).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(format)
            .setContentText("Stopwatch")
            .setSmallIcon(R.drawable.timer_outlined)
            .setOnlyAlertOnce(true)
            .setOngoing(isRunning)
            .setColor(ContextCompat.getColor(this, R.color.notification_primary))

            .addAction(pauseResumeAction)
            .addAction(lapAction)
            .addAction(stopAction)
            .build()
    }

    private fun formatElapsed(ms: Long): String {
        val totalSec = ms / 1000
        val seconds = (totalSec % 60).toInt()
        val minutes = (totalSec / 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

}
