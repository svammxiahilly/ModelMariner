package com.pranshulgg.clockmaster

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import java.util.*

class MainActivity: FlutterActivity() {
  private val CHANNEL = "com.pranshulgg.alarm/alarm"


  private val REQUEST_CODE_POST_NOTIFICATIONS = 1044

  private var permissionResultPending: MethodChannel.Result? = null

  private var pendingResult: MethodChannel.Result? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
      when (call.method) {
        "checkNotificationPermission" -> {
          result.success(isNotificationPermissionGranted())
        }
        "requestNotificationPermission" -> {
          requestNotificationPermission(result)
        }
        "setBrightness" -> {
          val brightness = call.argument<Double>("brightness") ?: 1.0
          setBrightness(brightness)
          result.success(null)
        }
        "resetBrightness" -> {
          resetBrightness()
          result.success(null)
        }
        "scheduleAlarm" -> {
          val id = call.argument<Int>("id")!!
          val triggerAtMillisUtc = call.argument<Long>("triggerAtMillisUtc")!!
          val label = call.argument<String>("label") ?: "Alarm"
          val sound = call.argument<String>("sound")
          val recurring = call.argument<Boolean>("recurring") ?: false
          val hour = call.argument<Int>("hour") ?: 0
          val minute = call.argument<Int>("minute") ?: 0
          val repeatDays = call.argument<List<Int>>("repeatDays") ?: listOf<Int>()
          val vibrate = call.argument<Boolean>("vibrate") ?: false
          val snoozeTimer = call.argument<Int>("snoozeMinutes") ?: 5

          scheduleAlarm(id, triggerAtMillisUtc, label, recurring, sound, hour, minute, repeatDays, vibrate, snoozeTimer)
          result.success(null)
        }
        "cancelAlarm" -> {
          val id = call.argument<Int>("id")!!
          cancelAlarm(id)
          result.success(null)
        }
        "pickSound" -> {
          pendingResult = result
          val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultUri)
          }
          startActivityForResult(intent, 1234)
        }
        "refreshAlwaysRunning" -> {
          refreshAlwaysOnService()
        }
        "StopAlwaysRunning" -> {
          stopAlwaysOnService()
        }

        else -> result.notImplemented()
      }
    }
  }

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 1234 && resultCode == RESULT_OK) {
        val pickedUri = data?.getParcelableExtra<android.net.Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

        val soundUriString = pickedUri?.toString() ?: "default"
        val ringtoneTitle = pickedUri?.let { uri ->
            RingtoneManager.getRingtone(this, uri)?.getTitle(this)
        } ?: "Default"

        val resultMap = mapOf(
            "uri" to soundUriString,
            "title" to ringtoneTitle
        )
        pendingResult?.success(resultMap)
        pendingResult = null
    } else if (requestCode == 1234) {
        pendingResult?.success(null)
        pendingResult = null
    }
}

  private fun isNotificationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
      // Below Android 13, permission not needed
      true
    }
  }

  private fun requestNotificationPermission(result: MethodChannel.Result) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (!isNotificationPermissionGranted()) {
        permissionResultPending = result
        requestPermissions(
          arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
          REQUEST_CODE_POST_NOTIFICATIONS
        )
      } else {
        result.success(true)
      }
    } else {
      result.success(true)
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        permissionResultPending?.success(true)
      } else {
        permissionResultPending?.success(false)
      }
      permissionResultPending = null
    }
  }

  private fun scheduleAlarm(
    id: Int,
    triggerAtMillisUtc: Long,
    label: String,
    recurring: Boolean,
    sound: String?,
    hour: Int,
    minute: Int,
    repeatDays: List<Int>,
    vibrate: Boolean,
    snoozeTimer: Int,
  ) {
    val am = getSystemService(ALARM_SERVICE) as AlarmManager

    val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
      action = "com.pranshulgg.alarm.ALARM_TRIGGER"
      putExtra("id", id)
      putExtra("label", label)
      putExtra("sound", sound)
      putExtra("hour", hour)
      putExtra("minute", minute)
      putIntegerArrayListExtra("daysOfWeek", ArrayList(repeatDays))
      putExtra("recurring", recurring)
      putExtra("vibrate", vibrate)
      putExtra("snoozeTime", snoozeTimer)

    }

    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val pendingAlarm = PendingIntent.getBroadcast(this, id, alarmIntent, flags)

    val showIntent = Intent(this, MainActivity::class.java).apply {
      putExtra("fromAlarmIcon", true)
    }
    val showPending = PendingIntent.getActivity(this, id + 1000000, showIntent, flags)

    val info = AlarmManager.AlarmClockInfo(triggerAtMillisUtc, showPending)
    am.setAlarmClock(info, pendingAlarm)
    refreshAlwaysOnService()

  }


  private fun cancelAlarm(id: Int) {
    val am = getSystemService(ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, AlarmReceiver::class.java).apply { action = "com.pranshulgg.alarm.ALARM_TRIGGER"; putExtra("id", id) }
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val pending = PendingIntent.getBroadcast(this, id, intent, flags)
    am.cancel(pending)
    val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
    nm.cancel(id)
    refreshAlwaysOnService()

  }


  private fun refreshAlwaysOnService() {
    val prefs = getSharedPreferences("FlutterSharedPreferences", MODE_PRIVATE)
    val alwaysRunService = prefs.getBoolean("flutter.alwaysRunService", false)

    if (alwaysRunService) {
      val intent = Intent(this, AlwaysOnAlarmService::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
      } else {
        startService(intent)
      }
    }
  }

  private fun stopAlwaysOnService() {
    val intent = Intent(this, AlwaysOnAlarmService::class.java)
    stopService(intent)
  }
  private fun setBrightness(brightness: Double) {
    val layoutParams = window.attributes
    layoutParams.screenBrightness = brightness.toFloat()
    window.attributes = layoutParams
}

  private fun resetBrightness() {
    val layoutParams = window.attributes
    layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    window.attributes = layoutParams
  }


}