package com.pranshulgg.clockmaster.receiver

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

        val soundUri =
            soundUriString?.let { Uri.parse(it) } ?: Settings.System.DEFAULT_ALARM_ALERT_URI

        val serviceIntent = Intent(context, AlarmServiceForeground::class.java).apply {
            putExtra("id", alarmId)
            putExtra("label", label)
            putExtra("sound", soundUri)
            putExtra("vibrate", vibrate)
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
                    Toast.makeText(context, "Unable to schedule exact alarm", Toast.LENGTH_SHORT)
                        .show()
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
