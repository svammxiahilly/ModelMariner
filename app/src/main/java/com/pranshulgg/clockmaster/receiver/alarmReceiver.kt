package com.pranshulgg.clockmaster.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.pranshulgg.clockmaster.MainActivity
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.AlarmScheduler
import com.pranshulgg.clockmaster.roomDB.AppDatabase
import kotlinx.coroutines.runBlocking
import java.util.ArrayList
import android.app.NotificationManager
import com.pranshulgg.clockmaster.services.AlarmServiceForeground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.ceil
import kotlin.time.Duration

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID_LOCKED = "alarm_channel_locked_v1"
        private const val CHANNEL_ID_DEFAULT = "alarm_channel_default_v1"
    }

    private var mediaPlayer: MediaPlayer? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: "Alarm!"
        val dayOfWeek = intent.getIntegerArrayListExtra("dayOfWeek") ?: arrayListOf()
        val hour = intent.getIntExtra("hour", 7)
        val minute = intent.getIntExtra("minute", 0)
        val alarmId = intent.getIntExtra("id", -1)
        val soundUriString = intent.getStringExtra("sound")
        val vibrate = intent.getBooleanExtra("vibrate", false)
        val snoozeTime = intent.getIntExtra("snoozeTime", 10)

        val soundUri =
            soundUriString?.let { Uri.parse(it) } ?: Settings.System.DEFAULT_ALARM_ALERT_URI

        val requestCode =
            if (alarmId >= 0) alarmId else (System.currentTimeMillis() and 0x7FFFFFFF).toInt()

        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        val isLocked = keyguardManager.isKeyguardLocked

        if (isLocked) {
            val alarmActivityIntent =
                Intent(context, com.pranshulgg.clockmaster.AlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("id", alarmId)
                    putExtra("label", label)
                    putExtra("sound", soundUriString)
                    putExtra("vibrate", vibrate)
                    putExtra("snoozeTime", snoozeTime)
                }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                alarmActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID_LOCKED,
                    "Alarms (Locked)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarm notifications on lock screen"
                    enableVibration(vibrate)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(channel)
            }

            val notif = NotificationCompat.Builder(context, CHANNEL_ID_LOCKED)
                .setSmallIcon(R.drawable.alarm_filled)
                .setContentTitle(label)
                .setContentText("Alarm is ringing")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .build()

            nm.notify(requestCode, notif)
        } else {

            val serviceIntent = Intent(context, AlarmServiceForeground::class.java).apply {
                putExtra("id", alarmId)
                putExtra("label", label)
                putExtra("sound", soundUriString)
                putExtra("vibrate", vibrate)
                putExtra("snoozeTime", snoozeTime)
            }

            context.startForegroundService(serviceIntent)

            if (dayOfWeek.isNotEmpty() && alarmId != -1) {
                val dao = AppDatabase.getDatabase(context).alarmDao()
                val alarmExists = runBlocking { dao.getAlarmById(alarmId) != null }

                if (alarmExists) {
                    try {

                        AlarmScheduler.scheduleAlarm(
                            context,
                            alarmId,
                            dayOfWeek,
                            hour,
                            minute,
                            label,
                            soundUriString,
                            vibrate
                        )
                    } catch (e: SecurityException) {
                        Toast.makeText(
                            context,
                            "Unable to schedule exact alarm",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }

        }


        if (alarmId != -1) {
            val db = AppDatabase.getDatabase(context)
            val alarmDao = db.alarmDao()

            CoroutineScope(Dispatchers.IO).launch {
                val alarm = alarmDao.getAlarmById(alarmId)
                if (alarm != null && alarm.repeatDays.isEmpty()) {
                    alarmDao.update(alarm.copy(enabled = false))
                }
            }
        }

    }




    fun generateUniqueId(label: String, dayOfWeek: ArrayList<Int?>): Int {
        return (label.hashCode() + 1_212_2)
    }

    private fun playAlarmSound(context: Context, soundUriString: String?) {
        val uri = if (!soundUriString.isNullOrEmpty() && soundUriString != "default") {
            android.net.Uri.parse(soundUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            isLooping = true
            prepare()
            start()
        }
    }



}
