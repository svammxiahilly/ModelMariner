package com.pranshulgg.clockmaster.helpers

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.pranshulgg.clockmaster.ui.components.SettingTile

@Composable
fun rememberAlarmSoundPickerLauncher(
    onSoundPicked: (uri: String?, title: String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val pickedUri: Uri? =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }

            val soundUriString = pickedUri?.toString() ?: "default"
            val ringtoneTitle = pickedUri?.let { uri ->
                RingtoneManager.getRingtone(context, uri)
                    ?.getTitle(context)
            } ?: "Default"

            onSoundPicked(soundUriString, ringtoneTitle)
        }
    }

    return {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultUri)
        }
        launcher.launch(intent)
    }
}
