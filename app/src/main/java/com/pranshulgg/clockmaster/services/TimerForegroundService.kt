package com.pranshulgg.clockmaster.services


import android.app.*
import android.content.*
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.NotificationHelper
import com.pranshulgg.clockmaster.models.TimerState
import com.pranshulgg.clockmaster.repository.TimerRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class TimerForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastTickTime = 0L

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIF_ID = 1337
        const val ACTION_PAUSE = "com.pranshulgg.clockmaster.ACTION_PAUSE"
        const val ACTION_RESUME = "com.pranshulgg.clockmaster.ACTION_RESUME"
        const val ACTION_RESET = "com.pranshulgg.clockmaster.ACTION_RESET"
        const val ACTION_RESET_ALL = "com.pranshulgg.clockmaster.ACTION_RESET_ALL"

        const val ACTION_STOP_RINGTONE = "com.pranshulgg.clockmaster.ACTION_STOP_RINGTONE"


        fun startServiceIfTimersExist(context: Context) {
            CoroutineScope(Dispatchers.Default).launch {
                val timers = TimerRepository.timers.first()
                val runningTimers = timers.filter { it.state == TimerState.Running }
                if (runningTimers.isNotEmpty()) {
                    val intent = Intent(context, TimerForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }
            }
        }


        fun stopService(context: Context) {
            val i = Intent(context, TimerForegroundService::class.java)
            context.stopService(i)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val a = intent?.action ?: return
            scope.launch {
                when (a) {
                    ACTION_PAUSE -> {
                        val id = intent.getStringExtra("id") ?: return@launch
                        TimerRepository.pauseTimer(id)
                    }

                    ACTION_RESUME -> {
                        val id = intent.getStringExtra("id") ?: return@launch
                        TimerRepository.resumeTimer(id)
                    }

                    ACTION_RESET -> {
                        val id = intent.getStringExtra("id") ?: return@launch
                        TimerRepository.resetTimer(id)
                    }

                    ACTION_RESET_ALL -> {
                        TimerRepository.resetAll()
                    }

                    ACTION_STOP_RINGTONE -> {
                        stopSelf()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val filter = IntentFilter().apply {
            addAction(ACTION_PAUSE)
            addAction(ACTION_RESUME)
            addAction(ACTION_RESET)
            addAction(ACTION_RESET_ALL)
            addAction(ACTION_STOP_RINGTONE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
        lastTickTime = SystemClock.elapsedRealtime()
        scope.launch { tickerLoop() }
    }

    private val finishedTimers = mutableSetOf<String>()
    private var currentMediaPlayer: MediaPlayer? = null

    private suspend fun tickerLoop() {
        while (scope.isActive) {
            val now = SystemClock.elapsedRealtime()
            val elapsed = now - lastTickTime
            if (elapsed >= 250L) {
                lastTickTime = now
                val finished = TimerRepository.tick(elapsed)


                val timers = TimerRepository.timers.first()
                val running = timers.filter { it.state == TimerState.Running }

                if (finished.isNotEmpty()) {
                    finished.forEach { id ->
                        if (!finishedTimers.contains(id)) {
                            finishedTimers.add(id)
                            val t = timers.find { it.id == id }
                            val label = t?.label ?: ""
                            TimerAlarmService.startAlarm(applicationContext, id, label)
                        }
                    }
                }

                if (running.isEmpty()) {
                    stopSelf()
                    break
                }

                updateForegroundNotification()
            }
            delay(250L)
        }
    }


    private suspend fun updateForegroundNotification() {
        val timers = TimerRepository.timers.first()
        val running = timers.filter { it.state == TimerState.Running }

        if (running.isEmpty()) {

            stopSelf()
            return
        }

        val firstRunning = running.first()
        val notification = NotificationHelper.buildNotification(this, timers, firstRunning)
        startForeground(NOTIF_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        scope.launch {
            updateForegroundNotification()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timers"
            val chan = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
            chan.setShowBadge(false)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(chan)
        }
    }
}
