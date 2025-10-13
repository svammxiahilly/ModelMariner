package com.pranshulgg.clockmaster.ui.components

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pranshulgg.clockmaster.helpers.rememberAlarmSoundPickerLauncher
import com.pranshulgg.clockmaster.models.AlarmViewModel
import com.pranshulgg.clockmaster.roomDB.AlarmEntity
import com.pranshulgg.clockmaster.utils.bottomPadding
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmBottomSheet(
    onDismiss: () -> Unit = {},
    alarmViewModel: AlarmViewModel = viewModel(),
    use24hr: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    val now = LocalTime.now()
    var hour by remember { mutableIntStateOf(now.hour) }
    var minute by remember { mutableIntStateOf(now.minute) }
    var label by remember { mutableStateOf("") }
    var soundUri by remember { mutableStateOf<String?>(null) }
    var soundTitle by remember { mutableStateOf("Default") }
    var repeatDays by remember { mutableStateOf(listOf<Int>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var useInputMode by remember { mutableStateOf(false) }
    var selectedValueSnooze by remember { mutableStateOf(10f) }
    var vibrate by remember { mutableStateOf(false) }

    val launchSoundPicker = rememberAlarmSoundPickerLauncher { uri, title ->

        soundUri = uri
        soundTitle = title
    }

    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysOfWeekShort = listOf("S", "M", "T", "W", "T", "F", "S")

    val context = LocalContext.current

    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        ExactAlarmPermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onGoToSettings = {
                openExactAlarmSettings(context)
                showPermissionDialog = false
            }
        )
    }


    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismiss()
                }
            }
        },
        sheetState = sheetState
    ) {

        val context = LocalContext.current

        val timeFormatter24 = DateTimeFormatter.ofPattern("HH:mm")
        val timeFormatter12 = DateTimeFormatter.ofPattern("hh:mm")
        val timeFormatter12AmPm = DateTimeFormatter.ofPattern("a")
        val currentTime = LocalTime.of(hour, minute)
        Column(
            modifier = Modifier.padding(
                end = 16.dp,
                start = 16.dp,
                bottom = bottomPadding()
            )
        ) {


            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (use24hr) currentTime.format(timeFormatter24)
                        else currentTime.format(timeFormatter12),
                        fontSize = 46.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )

                    if (!use24hr) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = currentTime.format(timeFormatter12AmPm),
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,

                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }

                FilledTonalButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { showTimePicker = true }
                ) {
                    Text("Edit")
                }
            }

            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = use24hr
                )

                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text("Select time") },
                    confirmButton = {
                        TextButton(onClick = {
                            hour = timePickerState.hour
                            minute = timePickerState.minute
                            showTimePicker = false
                        }, shapes = ButtonDefaults.shapes()) {
                            Text("Confirm", fontWeight = FontWeight.W600, fontSize = 15.sp)
                        }
                    },
                    dismissButton = {

                        TextButton(
                            onClick = { showTimePicker = false },
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Text("Cancel", fontWeight = FontWeight.W600, fontSize = 15.sp)
                        }
                    },
                    text = {
                        Column {

                            Spacer(Modifier.height(8.dp))

                            if (useInputMode) {
                                TimeInput(state = timePickerState)
                            } else {
                                TimePicker(state = timePickerState)
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                val buttonModifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                daysOfWeek.forEachIndexed { index, day ->
                    val selected = index in repeatDays
                    ToggleButton(
                        checked = selected,
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,

                            ),
                        onCheckedChange = {
                            repeatDays =
                                if (it) repeatDays + index else repeatDays - index
                        },
                        modifier = buttonModifier,
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                daysOfWeek.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Text(
                            daysOfWeekShort[index],
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


            Spacer(Modifier.height(16.dp))


            SettingSection(
                noPadding = true,
                tiles = listOf(
                    SettingTile.DialogTextFieldTile(
                        title = "Label",
                        onTextSubmitted = { label = it },
                        initialText = label,
                        placeholder = "Your label",
                        placeholderTextField = "Label"
                    ),

                    SettingTile.ActionTile(
                        title = "Sound",
                        description = soundTitle,
                        onClick = {
                            launchSoundPicker()
                        },
                        colorDesc = MaterialTheme.colorScheme.tertiary
                    ),

                    SettingTile.DialogSliderTile(
                        title = "Snooze time",
                        initialValue = selectedValueSnooze,
                        description = "${selectedValueSnooze.roundToInt()} ${if (selectedValueSnooze.roundToInt() < 2) "minute" else "minutes"}",
                        isDescriptionAsValue = true,
                        valueRange = 1f..30f,
                        steps = 15,
                        labelFormatter = { "${it.roundToInt()}m" },
                        onValueSubmitted = { newValue ->
                            selectedValueSnooze = newValue
                        },
                        dialogTitle = "Snooze"
                    ),
                    SettingTile.SwitchTile(
                        title = "Vibrate",
                        checked = vibrate,
                        onCheckedChange = { checked ->
                            vibrate = checked
                        }
                    ),

                    )
            )


            Spacer(Modifier.height(16.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.defaultMinSize(minWidth = 90.dp, minHeight = 45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    onClick = {

                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    }, shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = {
                        if (canScheduleExactAlarms(context)) {
                            alarmViewModel.addAlarm(
                                context,
                                AlarmEntity(
                                    hour = hour,
                                    minute = minute,
                                    repeatDays = repeatDays,
                                    label = label,
                                    sound = soundUri,
                                    snoozeTime = selectedValueSnooze.toInt()
                                )
                            )
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onDismiss()
                                }
                            }
                        } else {
                            showPermissionDialog = true
                        }
                    },

                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.defaultMinSize(minWidth = 90.dp, minHeight = 45.dp),
                ) {
                    Text("Save", fontSize = 16.sp)
                }
            }
        }
    }

}

fun canScheduleExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExactAlarmPermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission required") },
        text = { Text("To schedule exact alarms, please grant permission in system settings.") },
        confirmButton = {
            TextButton(onClick = onGoToSettings, shapes = ButtonDefaults.shapes()) {
                Text("Go to settings", fontWeight = FontWeight.W600, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
            }
        }
    )
}
