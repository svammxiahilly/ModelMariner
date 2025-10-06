package com.pranshulgg.clockmaster.services


import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
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
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
        lastTickTime = SystemClock.elapsedRealtime()
        scope.launch { tickerLoop() }
    }

    private suspend fun tickerLoop() {
        while (scope.isActive) {
            val now = SystemClock.elapsedRealtime()
            val elapsed = now - lastTickTime
            if (elapsed >= 250L) {
                lastTickTime = now
                val finished = TimerRepository.tick(elapsed)

                val timers = TimerRepository.timers.first()
                val running = timers.filter { it.state == TimerState.Running }

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
