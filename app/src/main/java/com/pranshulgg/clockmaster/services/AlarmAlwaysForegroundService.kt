package com.pranshulgg.clockmaster.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pranshulgg.clockmaster.MainActivity
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.repository.AlarmRepository
import com.pranshulgg.clockmaster.roomDB.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

class AlarmAlwaysForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: AlarmRepository
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        repository = AlarmRepository(AppDatabase.getDatabase(applicationContext).alarmDao())
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        observeAlarms()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val placeholder = createNotification("Next alarm: None • 0 active")
        startForeground(NOTIFICATION_ID, placeholder)
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun observeAlarms() {
        serviceScope.launch {
            repository.allAlarms.collectLatest { alarms ->
                val activeAlarms = alarms.filter { it.enabled }
                val nextAlarm = activeAlarms.minByOrNull { it.hour * 60 + it.minute }
                val nextAlarmText = nextAlarm?.let {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, it.hour)
                        set(Calendar.MINUTE, it.minute)
                    }

                    val timeFormat =
                        android.text.format.DateFormat.getTimeFormat(applicationContext)
                    timeFormat.format(cal.time)
                } ?: "None"

                val text = "Next alarm: $nextAlarmText • ${activeAlarms.size} active"
                val notification = createNotification(text)

                startForeground(NOTIFICATION_ID, notification)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }


    private fun createNotification(content: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.alarm_filled)
            .setContentTitle("ClockMaster Running")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, "ClockMaster Service", importance).apply {
                description = "Shows info about active alarms"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "clockmaster_service_channel_always_on_alarm"
        const val NOTIFICATION_ID = 20012
    }
}
