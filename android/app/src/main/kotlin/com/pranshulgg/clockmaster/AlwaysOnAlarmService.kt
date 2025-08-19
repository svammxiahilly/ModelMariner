package com.pranshulgg.clockmaster

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pranshulgg.clockmaster.MainActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AlwaysOnAlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "always_on_alarm_channel"
        const val NOTIFICATION_ID = 1000
        const val PREF_KEY = "alarms_v1"

        var instance: AlwaysOnAlarmService? = null

    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        updateNotification()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            createNotificationChannel()

    val placeholder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("ClockMaster Service")
        .setContentText("Loading alarms…")
        .setSmallIcon(R.drawable.baseline_schedule_24)
        .setOngoing(true)
        .build()

    startForeground(NOTIFICATION_ID, placeholder)
        
        updateNotification()
        return START_STICKY
    }

    fun updateNotification() {
        serviceScope.launch {
            val alarms = loadAlarms()
            val activeAlarms = alarms.filter { it.enabled }

            if (alarms.isEmpty() || activeAlarms.isEmpty()) {

                delay(3000)   
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return@launch
            }

            val nextAlarm = activeAlarms.minByOrNull { it.nextTriggerMillis() }
            val nextAlarmText = nextAlarm?.let {
                val sdf = SimpleDateFormat("EEE, MMM d | hh:mm a", Locale.getDefault())
                sdf.format(Date(it.nextTriggerMillis()))
            } ?: "No alarms scheduled"

            val soundOn = nextAlarm?.sound?.isNotEmpty() == true
            val vibrateOn = nextAlarm?.vibrate == true

            val notificationText = buildString {
                append("Next alarm: $nextAlarmText • ${activeAlarms.size} active")
            }

            val openAppIntent = Intent(this@AlwaysOnAlarmService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this@AlwaysOnAlarmService,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this@AlwaysOnAlarmService, CHANNEL_ID)
                .setContentTitle("ClockMaster Service")
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.baseline_schedule_24)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Always-On Alarm Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows info about active alarms"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun loadAlarms(): List<Alarm> {
        val flutterPrefs = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)

        try {
        } catch (e: Exception) {
        }

        val possibleJsonKeys = listOf("flutter.${PREF_KEY}_json", "${PREF_KEY}_json", "flutter.$PREF_KEY", PREF_KEY)

        for (k in possibleJsonKeys) {
            val raw = flutterPrefs.getString(k, null)
            if (!raw.isNullOrEmpty()) {
                try {
                    val jsonArray = JSONObject("{\"data\":$raw}").getJSONArray("data")
                    return List(jsonArray.length()) { i -> Alarm.fromJson(jsonArray.getString(i)) }
                } catch (e: Exception) {
                }
            }
        }

        val possibleSetKeys = listOf("flutter.$PREF_KEY", PREF_KEY)
        for (k in possibleSetKeys) {
            val rawSet = flutterPrefs.getStringSet(k, null)
            if (rawSet != null) {
                return rawSet.map { Alarm.fromJson(it) }
            }
        }

        return emptyList()
    }


    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    data class Alarm(
        val id: Int,
        val label: String,
        val sound: String?,
        val vibrate: Boolean,
        val hour: Int,
        val minute: Int,
        val repeatDays: List<Int>,
        val enabled: Boolean
    ) {
        fun nextTriggerMillis(): Long {
            val now = Calendar.getInstance()
            val candidate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (repeatDays.isNotEmpty()) {
                var attempts = 0
                val daysOfWeek = repeatDays.map {
                    when (it) {
                        1 -> Calendar.MONDAY
                        2 -> Calendar.TUESDAY
                        3 -> Calendar.WEDNESDAY
                        4 -> Calendar.THURSDAY
                        5 -> Calendar.FRIDAY
                        6 -> Calendar.SATURDAY
                        7 -> Calendar.SUNDAY
                        else -> Calendar.MONDAY
                    }
                }
                while (attempts < 8) {
                    if (daysOfWeek.contains(candidate.get(Calendar.DAY_OF_WEEK)) &&
                        candidate.timeInMillis > now.timeInMillis) break
                    candidate.add(Calendar.DAY_OF_MONTH, 1)
                    attempts++
                }
            } else if (candidate.timeInMillis <= now.timeInMillis) {
                candidate.add(Calendar.DAY_OF_MONTH, 1)
            }
            return candidate.timeInMillis
        }

        companion object {
            fun fromJson(json: String): Alarm {
                val obj = JSONObject(json)
                return Alarm(
                    id = obj.getInt("id"),
                    label = obj.optString("label", "Alarm"),
                    sound = obj.optString("sound", ""),
                    vibrate = obj.optBoolean("vibrate", false),
                    hour = obj.getInt("hour"),
                    minute = obj.getInt("minute"),
                    repeatDays = obj.optJSONArray("repeatDays")?.let { array ->
                        List(array.length()) { i -> array.getInt(i) }
                    } ?: emptyList(),
                    enabled = obj.optBoolean("enabled", true)
                )
            }
        }
    }
}
