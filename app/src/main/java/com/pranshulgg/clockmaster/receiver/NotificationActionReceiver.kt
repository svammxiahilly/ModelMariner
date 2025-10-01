package com.pranshulgg.clockmaster.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.pranshulgg.clockmaster.services.StopwatchForegroundService

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        if (action == null) return

        val svcIntent = Intent(context, StopwatchForegroundService::class.java).apply {
            this.action = action
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, svcIntent)
        } else {
            context.startService(svcIntent)
        }
    }
}
