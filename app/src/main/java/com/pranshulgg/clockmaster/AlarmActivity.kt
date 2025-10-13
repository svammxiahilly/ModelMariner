package com.pranshulgg.clockmaster

import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.TypedValue
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import android.view.WindowInsets
import androidx.core.view.WindowCompat
import android.view.View
import com.pranshulgg.clockmaster.receiver.AlarmReceiver
import com.pranshulgg.clockmaster.services.AlarmServiceForeground

class AlarmActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {
            // idk why this happened
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km?.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarm)

        val label = intent.getStringExtra("label") ?: "Alarm"
        val soundUriString = intent.getStringExtra("sound")
        val vibrate = intent.getBooleanExtra("vibrate", false)
        val alarmId = intent.getIntExtra("id", -1) ?: -1
        val soundUri =
            soundUriString?.let { Uri.parse(it) } ?: Settings.System.DEFAULT_ALARM_ALERT_URI
        val snoozeTime = intent.getIntExtra("snoozeTime", 10)

        val alarmTimeHour = findViewById<TextView>(R.id.alarm_hour)
        val alarmTimeMinute = findViewById<TextView>(R.id.alarm_minute)
        val screenWidth = resources.displayMetrics.widthPixels
        alarmTimeHour.setTextSize(TypedValue.COMPLEX_UNIT_PX, screenWidth / 2.5f)
        alarmTimeMinute.setTextSize(TypedValue.COMPLEX_UNIT_PX, screenWidth / 2.5f)



        findViewById<TextView>(R.id.alarmLabel)?.text = label

        startRingtone(soundUriString)
        if (vibrate) startVibration()

        findViewById<Button>(R.id.dismissButton)?.setOnClickListener {
            stopAlarm()
            finish()
        }

        findViewById<Button>(R.id.snoozeButton)?.setOnClickListener {
            stopAlarm()
            scheduleSnooze(alarmId, label, snoozeTime)
//            val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 mins
//            val snoozeTime = System.currentTimeMillis() + 10_000 // 10 sec
//
//            val alarmManager =
//                getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
//
//            val snoozeIntent = Intent(this, AlarmActivity::class.java).apply {
//                putExtra("label", label)
//                putExtra("id", alarmId)
//                putExtra("vibrate", vibrate)
//                putExtra("sound", soundUri.toString())
//            }
//
//            val snoozePendingIntent = PendingIntent.getService(
//                this,
//                alarmId,
//                snoozeIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//
//            try {
//
//                alarmManager.setExactAndAllowWhileIdle(
//                    android.app.AlarmManager.RTC_WAKEUP,
//                    snoozeTime,
//                    snoozePendingIntent
//                )
//
//            } catch (e: SecurityException) {
//            }
            finish()
        }
    }

    private fun startRingtone(soundUriString: String?) {
        val uri = if (!soundUriString.isNullOrEmpty() && soundUriString != "default") {
            android.net.Uri.parse(soundUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, uri)
            setAudioAttributes(attributes)
            isLooping = true
            prepare()
            start()
        }


    }

    private fun startVibration() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
        vibrator?.cancel()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmId = intent.getIntExtra("id", -1)
        if (alarmId != -1) nm.cancel(alarmId)
    }


    private fun scheduleSnooze(id: Int, label: String, mins: Int) {
        val am = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val trigger = System.currentTimeMillis() + mins * 60 * 1000L
        android.widget.Toast.makeText(
            this@AlarmActivity,
            "Snoozed for $mins ${if (mins == 1) "minute" else "minutes"}",
            android.widget.Toast.LENGTH_LONG
        ).show()

        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("label", "Snoozed Alarm • $label")
            putExtra("snoozeTime", trigger)
        }
        val pending = android.app.PendingIntent.getBroadcast(
            this, id, alarmIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, trigger, pending)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
