package com.pranshulgg.clockmaster.screens.setting_screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.services.AlarmAlwaysForegroundService
import com.pranshulgg.clockmaster.ui.components.SettingSection
import com.pranshulgg.clockmaster.ui.components.SettingTile
import com.pranshulgg.clockmaster.ui.components.SettingsTileIcon
import com.pranshulgg.clockmaster.ui.components.Symbol
import kotlin.math.roundToInt

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun AlarmSettings(
    navController: NavController,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    var selectedValueAlarmVol by remember { mutableStateOf((currentVolume.toFloat() / maxVolume * 100f)) }

    var confirmDeletingAlarm by remember {
        mutableStateOf(
            PreferencesHelper.getBool("confirmDeletingAlarmItem") ?: false
        )
    }

    var keepServiceRunning by remember {
        mutableStateOf(PreferencesHelper.getBool("keepServiceRunning") ?: false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = { Text("Alarm") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Symbol(
                            R.drawable.arrow_back,
                            desc = "Back",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,

                )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingSection(
                    title = "General",
                    tiles = listOf(
                        SettingTile.DialogSliderTile(
                            title = "Alarm volume",
                            dialogTitle = "Alarm volume",
                            description = "${selectedValueAlarmVol.roundToInt()}%",
                            isDescriptionAsValue = true,
                            leading = { SettingsTileIcon(R.drawable.volume_up) },
                            initialValue = selectedValueAlarmVol,
                            valueRange = 0f..100f,
                            labelFormatter = { "${it.roundToInt()}%" },
                            onValueSubmitted = { newValue ->
                                selectedValueAlarmVol = newValue
                                val volumeLevel = ((newValue / 100f) * maxVolume).toInt()
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_ALARM,
                                    volumeLevel,
                                    AudioManager.FLAG_SHOW_UI
                                )
                            }
                        )
                    )
                )

                Spacer(Modifier.height(10.dp))
                SettingSection(
                    title = "Behavior",
                    tiles = listOf(
                        SettingTile.SwitchTile(
                            leading = { SettingsTileIcon(R.drawable.warning) },
                            title = "Confirm before deleting",
                            description = "Ask for confirmation before deleting any alarms",
                            checked = confirmDeletingAlarm,
                            onCheckedChange = { checked ->
                                confirmDeletingAlarm = checked
                                PreferencesHelper.setBool("confirmDeletingAlarmItem", checked)
                            }
                        ),
                    )
                )

                Spacer(Modifier.height(10.dp))
                SettingSection(
                    title = "Services",
                    tiles = listOf(
                        SettingTile.SwitchTile(
                            leading = { SettingsTileIcon(R.drawable.construction) },
                            title = "Always run background service",
                            description = "Use this if alarms aren’t working; it may prevent the app from being killed but uses more battery",
                            checked = keepServiceRunning,
                            onCheckedChange = { checked ->
                                keepServiceRunning = checked
                                PreferencesHelper.setBool("keepServiceRunning", checked)
                                val intent =
                                    Intent(context, AlarmAlwaysForegroundService::class.java)

                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                } else {
                                    context.stopService(intent)
                                }
                            }
                        ),
                    )
                )
            }

        }
    }

}
