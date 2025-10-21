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
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PomodoroMode
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.math.max

class PomodoroService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentMode = PomodoroMode.FOCUS
    private var remainingMs: Long = 0L
    private var isRunning = false

    private var focusMin = PreferencesHelper.getInt("pomoFocusTime") ?: 25
    private var shortMin = PreferencesHelper.getInt("pomoShortBreakTime") ?: 5
    private var longMin = PreferencesHelper.getInt("pomoLongBreakTime") ?: 15
    private var autoNext = PreferencesHelper.getBool("autoStartSessionPomo") ?: false
    private var completedFocusCycles = 0
    private var cyclesBeforeLongBreak = PreferencesHelper.getInt("cyclesBeforeLongBreak") ?: 4

    private var tickerJob: Job? = null

    private lateinit var notificationManager: NotificationManager
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        remainingMs = focusMin * 60L * 1000L
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundServiceWithNotification()
            }

            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            ACTION_RESUME -> resumeTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_SKIP -> skipSession()
            ACTION_RESET -> resetSession()
            ACTION_SET_DURATIONS -> {
                focusMin = intent.getIntExtra(EXTRA_FOCUS_MIN, focusMin)
                shortMin = intent.getIntExtra(EXTRA_SHORT_MIN, shortMin)
                longMin = intent.getIntExtra(EXTRA_LONG_MIN, longMin)

                if (!isRunning) remainingMs = when (currentMode) {
                    PomodoroMode.FOCUS -> focusMin * 60L * 1000L
                    PomodoroMode.SHORT_BREAK -> shortMin * 60L * 1000L
                    PomodoroMode.LONG_BREAK -> longMin * 60L * 1000L
                }
                broadcastState()
                updateNotification()
            }

            ACTION_SET_MODE -> {
                val mode = intent.getSerializableExtra(EXTRA_MODE) as? PomodoroMode
                mode?.let { changeMode(it, startTimer = false) }
            }

            ACTION_SET_AUTO_NEXT -> {
                autoNext = intent.getBooleanExtra(EXTRA_AUTO_NEXT, autoNext)
                broadcastState()
                updateNotification()
            }

            ACTION_REQUEST_STATUS -> {
                broadcastState()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        scope.cancel()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        broadcastState()
    }

    private fun resumeTimer() {
        if (isRunning) return
        isRunning = true
        startTicker()
        updateNotification()
        broadcastState()
    }

    private fun pauseTimer() {
        if (!isRunning) return
        isRunning = false
        tickerJob?.cancel()
        tickerJob = null
        updateNotification()
        broadcastState()
    }

    private fun resetSession() {
        isRunning = false
        tickerJob?.cancel()
        remainingMs = when (currentMode) {
            PomodoroMode.FOCUS -> focusMin * 60L * 1000L
            PomodoroMode.SHORT_BREAK -> shortMin * 60L * 1000L
            PomodoroMode.LONG_BREAK -> longMin * 60L * 1000L
        }
        if (isRunning) {
            updateNotification()
        }
        broadcastState()
    }

    private fun skipSession() {
        when (currentMode) {
            PomodoroMode.FOCUS -> {
                completedFocusCycles++
                currentMode = if (completedFocusCycles >= cyclesBeforeLongBreak) {
                    completedFocusCycles = 0
                    PomodoroMode.LONG_BREAK
                } else {
                    PomodoroMode.SHORT_BREAK
                }
            }

            PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                currentMode = PomodoroMode.FOCUS
            }
        }

        remainingMs = when (currentMode) {
            PomodoroMode.FOCUS -> focusMin * 60L * 1000L
            PomodoroMode.SHORT_BREAK -> shortMin * 60L * 1000L
            PomodoroMode.LONG_BREAK -> longMin * 60L * 1000L
        }

        if (autoNext) resumeTimer() else pauseTimer()
        if (isRunning) {
            updateNotification()
        }
        broadcastState()
    }


    private fun changeMode(newMode: PomodoroMode, startTimer: Boolean) {
        currentMode = newMode
        remainingMs = when (newMode) {
            PomodoroMode.FOCUS -> focusMin * 60L * 1000L
            PomodoroMode.SHORT_BREAK -> shortMin * 60L * 1000L
            PomodoroMode.LONG_BREAK -> longMin * 60L * 1000L
        }
        if (startTimer) resumeTimer()
        updateNotification()
        broadcastState()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && isRunning) {
                delay(1000L)
                remainingMs = max(0L, remainingMs - 1000L)
                broadcastState()
                updateNotification()
                if (remainingMs <= 0L) {
                    playCompletionSound()
                    onSessionComplete()
                }
            }
        }
    }

    private fun onSessionComplete() {
        when (currentMode) {
            PomodoroMode.FOCUS -> {
                completedFocusCycles++
                currentMode = if (completedFocusCycles >= cyclesBeforeLongBreak) {
                    completedFocusCycles = 0
                    PomodoroMode.LONG_BREAK
                } else {
                    PomodoroMode.SHORT_BREAK
                }
            }

            PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                currentMode = PomodoroMode.FOCUS
            }
        }

        remainingMs = when (currentMode) {
            PomodoroMode.FOCUS -> focusMin * 60L * 1000L
            PomodoroMode.SHORT_BREAK -> shortMin * 60L * 1000L
            PomodoroMode.LONG_BREAK -> longMin * 60L * 1000L
        }

        if (autoNext) {
            isRunning = true
            startTicker()
        } else {
            isRunning = false
            tickerJob?.cancel()

            tickerJob = null
        }

        updateNotification()
        broadcastState()
    }

    private fun playCompletionSound() {
//        MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
//        mediaPlayer?.start()
        val uri =
            RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION)
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
            setDataSource(this@PomodoroService, uri)
            prepare()

            start()
        }
        mediaPlayer = mp
    }

    private fun updateNotification() {
        val notification = buildNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val title = when (currentMode) {
            PomodoroMode.FOCUS -> "Focus session"
            PomodoroMode.SHORT_BREAK -> "Short break"
            PomodoroMode.LONG_BREAK -> "Long break"
        }
        val pendingIntentOpen = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            pendingImmutableFlag()
        )

        val skipIntent = actionIntent(ACTION_SKIP)
        val pauseResumeAction = if (isRunning) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                actionPendingIntent(ACTION_PAUSE)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Resume",
                actionPendingIntent(ACTION_RESUME)
            ).build()
        }
        val skipAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_next,
            "Skip",
            actionPendingIntent(ACTION_SKIP)
        ).build()
        val resetAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_revert,
            "Reset",
            actionPendingIntent(ACTION_RESET)
        ).build()

        val timeText = formatMsToTime(remainingMs)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(timeText)
            .setSmallIcon(R.drawable.hourglass_top)
            .setContentIntent(pendingIntentOpen)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(skipAction)
            .addAction(pauseResumeAction)
            .addAction(resetAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, PomodoroService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            pendingImmutableFlag() or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun actionIntent(action: String): Intent {
        return Intent(this, PomodoroService::class.java).apply { this.action = action }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun broadcastState() {
        val i = Intent(BROADCAST_ACTION_STATE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setPackage(packageName) //Setting the package breaks the timer on Android < 12
            }
            putExtra(EXTRA_MODE, currentMode)
            putExtra(EXTRA_REMAIN_MS, remainingMs)
            putExtra(EXTRA_RUNNING, isRunning)
            putExtra(EXTRA_FOCUS_MIN, focusMin)
            putExtra(EXTRA_SHORT_MIN, shortMin)
            putExtra(EXTRA_LONG_MIN, longMin)
            putExtra(EXTRA_AUTO_NEXT, autoNext)
            putExtra(EXTRA_COMPLETED_CYCLES, completedFocusCycles)
            putExtra(EXTRA_CYCLES_BEFORE_LONG, cyclesBeforeLongBreak)
        }
        sendBroadcast(i)
    }


    private fun pendingImmutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    }

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_SERVICE = "com.pranshulgg.clockmaster.POMODORO_START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.pranshulgg.clockmaster.POMODORO_STOP_SERVICE"
        const val ACTION_RESUME = "com.pranshulgg.clockmaster.POMODORO_RESUME"
        const val ACTION_PAUSE = "com.pranshulgg.clockmaster.POMODORO_PAUSE"
        const val ACTION_SKIP = "com.pranshulgg.clockmaster.POMODORO_SKIP"
        const val ACTION_RESET = "com.pranshulgg.clockmaster.POMODORO_RESET"
        const val ACTION_SET_DURATIONS = "com.pranshulgg.clockmaster.POMODORO_SET_DURATIONS"
        const val ACTION_SET_MODE = "com.pranshulgg.clockmaster.POMODORO_SET_MODE"
        const val ACTION_SET_AUTO_NEXT = "com.pranshulgg.clockmaster.POMODORO_SET_AUTO_NEXT"
        const val ACTION_REQUEST_STATUS = "com.pranshulgg.clockmaster.POMODORO_REQUEST_STATUS"

        const val BROADCAST_ACTION_STATE = "com.pranshulgg.clockmaster.POMODORO_BROADCAST_STATE"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_REMAIN_MS = "extra_rem_ms"
        const val EXTRA_RUNNING = "extra_running"
        const val EXTRA_FOCUS_MIN = "extra_focus_min"
        const val EXTRA_SHORT_MIN = "extra_short_min"
        const val EXTRA_LONG_MIN = "extra_long_min"
        const val EXTRA_AUTO_NEXT = "extra_auto_next"

        const val EXTRA_COMPLETED_CYCLES = "extra_completed_cycles"
        const val EXTRA_CYCLES_BEFORE_LONG = "extra_cycles_before_long"


        fun enqueueStart(context: Context) {
            val i =
                Intent(context, PomodoroService::class.java).apply { action = ACTION_START_SERVICE }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i) else context.startService(
                i
            )
        }

        fun enqueueStop(context: Context) {
            val i =
                Intent(context, PomodoroService::class.java).apply { action = ACTION_STOP_SERVICE }
            context.startService(i)
        }

        fun enqueueAction(context: Context, action: String) {
            val i = Intent(context, PomodoroService::class.java).apply { this.action = action }
            context.startService(i)
        }

        fun enqueueSetDurations(context: Context, focus: Int, short: Int, long: Int) {
            val i = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_SET_DURATIONS
                putExtra(EXTRA_FOCUS_MIN, focus)
                putExtra(EXTRA_SHORT_MIN, short)
                putExtra(EXTRA_LONG_MIN, long)
            }
            context.startService(i)
        }

        fun enqueueSetMode(context: Context, mode: PomodoroMode) {
            val i = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_SET_MODE
                putExtra(EXTRA_MODE, mode)
            }
            context.startService(i)
        }

        fun enqueueSetAutoNext(context: Context, enable: Boolean) {
            val i = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_SET_AUTO_NEXT
                putExtra(EXTRA_AUTO_NEXT, enable)
            }
            context.startService(i)
        }

        fun enqueueUpdateFromApp(context: Context) {
            val i = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_REQUEST_STATUS
            }
            context.startService(i)
        }
    }
}

private fun formatMsToTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}
