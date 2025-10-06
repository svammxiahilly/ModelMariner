package com.pranshulgg.clockmaster.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.models.TimerItem
import com.pranshulgg.clockmaster.services.TimerForegroundService
import java.util.concurrent.TimeUnit

object NotificationHelper {

    private fun formatMillis(ms: Long): String {
        val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val h = TimeUnit.MILLISECONDS.toHours(ms)
        return String.format("%02d:%02d:%02d", h, m, s)
    }


    fun buildNotification(
        context: Context,
        timers: List<TimerItem>,
        firstRunning: TimerItem?
    ): android.app.Notification {
        val builder = NotificationCompat.Builder(context, TimerForegroundService.CHANNEL_ID)
            .setSmallIcon(R.drawable.hourglass_top)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setColor(ContextCompat.getColor(context, R.color.notification_primary))

        if (firstRunning == null) {
            builder.setContentTitle("No timers running")
                .setContentText("Open app to manage timers")
        } else {
            val runningCount = timers.count { it.state.name == "Running" }
            builder.setContentTitle(formatMillis(firstRunning.remainingMillis))
                .setContentText(firstRunning.label)
            if (runningCount == 1) {
                val pauseIntent = Intent(TimerForegroundService.ACTION_PAUSE).apply {
                    putExtra("id", firstRunning.id)
                }
                val resumeIntent = Intent(TimerForegroundService.ACTION_RESUME).apply {
                    putExtra("id", firstRunning.id)
                }

                val pausePending = PendingIntent.getBroadcast(
                    context,
                    firstRunning.id.hashCode(),
                    if (firstRunning.state.name == "Running") pauseIntent else resumeIntent,
                    pendingFlags()
                )

                val pauseLabel = if (firstRunning.state.name == "Running") "Pause" else "Resume"
                builder.addAction(android.R.drawable.ic_media_pause, pauseLabel, pausePending)

                val resetIntent = Intent(TimerForegroundService.ACTION_RESET).apply {
                    putExtra("id", firstRunning.id)
                }
                val resetPending = PendingIntent.getBroadcast(
                    context,
                    ("reset${firstRunning.id}").hashCode(),
                    resetIntent,
                    pendingFlags()
                )
                builder.addAction(android.R.drawable.ic_menu_revert, "Reset", resetPending)
            } else {
                val resetAllIntent = Intent(TimerForegroundService.ACTION_RESET_ALL)
                val resetAllPending = PendingIntent.getBroadcast(
                    context,
                    "reset_all".hashCode(),
                    resetAllIntent,
                    pendingFlags()
                )
                builder.addAction(android.R.drawable.ic_menu_revert, "Reset all", resetAllPending)
            }
        }

        return builder.build()
    }

    private fun pendingFlags(): Int {
        val mut = PendingIntent.FLAG_UPDATE_CURRENT
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mut or PendingIntent.FLAG_IMMUTABLE
        } else mut
    }
}