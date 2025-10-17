package com.pranshulgg.clockmaster.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pranshulgg.clockmaster.helpers.AlarmScheduler
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.models.AlarmViewModel
import com.pranshulgg.clockmaster.ui.components.EmptyContainerPlaceholder
import com.pranshulgg.clockmaster.ui.components.Symbol
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.services.AlarmAlwaysForegroundService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmScreen(alarmViewModel: AlarmViewModel = viewModel()) {
    var use24HourFormat by remember { mutableStateOf(PreferencesHelper.getBool("is24hr") ?: false) }
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }
    var soundUri by remember { mutableStateOf<String?>(null) }
    var soundTitle by remember { mutableStateOf("Default") }
    var repeatDays by remember { mutableStateOf(listOf<Int>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var useInputMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var useExpressiveColor by remember {
        mutableStateOf(
            PreferencesHelper.getBool("useExpressiveColor") ?: true
        )
    }

    var darkTheme by remember {
        mutableStateOf(
            PreferencesHelper.getBool("dark_theme") ?: false
        )
    }
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysOfWeekShort = listOf("S", "M", "T", "W", "T", "F", "S")

    val context = LocalContext.current

    fun repeatDaysText(days: List<Int>): String {
        if (days.isEmpty()) return "One time"
        if (days.size == 7) return "Every day"
        return days.sorted().joinToString(", ") { daysOfWeek[it] }
    }

    val alarms by alarmViewModel.alarms.collectAsState(initial = emptyList())

    if (alarms.isEmpty()) {
        EmptyContainerPlaceholder(
            icon = R.drawable.alarm_filled,
            text = "No alarms"
        )
    }

    LazyColumn(
        modifier = Modifier.padding(end = 12.dp, start = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        items(alarms, key = { it.id }) { alarm ->

//        alarms.forEach { alarm ->
            val hourMinute: String
            var amPm: String? = null
            var isEnabled by remember { mutableStateOf(alarm.enabled) }

            val tileColor: Color = if (isEnabled) {
                if (useExpressiveColor) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    if (darkTheme) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                }
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }

            if (use24HourFormat) {
                hourMinute = String.format("%02d:%02d", alarm.hour, alarm.minute)
            } else {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                }
                hourMinute = SimpleDateFormat("hh:mm", Locale.getDefault()).format(cal.time)
                amPm = SimpleDateFormat("a", Locale.getDefault()).format(cal.time)
            }

            var visible by remember { mutableStateOf(true) }

            val dismissState = rememberSwipeToDismissBoxState(
                initialValue = SwipeToDismissBoxValue.Settled,
                positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
            )

            val confirm = PreferencesHelper.getBool("confirmDeletingAlarmItem") ?: false
            var showDialog by remember { mutableStateOf(false) }


            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        scope.launch { dismissState.reset() }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                visible = false
                                scope.launch {
                                    delay(300)
                                    alarmViewModel.removeAlarm(context, alarm)
                                }
                            }, shapes = ButtonDefaults.shapes()
                        ) {
                            Text("Delete", fontWeight = FontWeight.W600, fontSize = 16.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            scope.launch { dismissState.reset() }
                        }, shapes = ButtonDefaults.shapes()) {
                            Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
                        }
                    },
                    title = { Text("Delete alarm") },
                    text = { Text("Are you sure you want to delete this alarm?") }
                )
            }

            AnimatedVisibility(
                visible = visible,
                exit = fadeOut()
            ) {
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    onDismiss = { direction ->
                        if (direction == SwipeToDismissBoxValue.EndToStart) {
                            if (confirm) {
                                showDialog = true
                            } else {
                                visible = false
                                scope.launch {
                                    delay(300)
                                    alarmViewModel.removeAlarm(context, alarm)
                                }
                            }
                        }
                    },
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Surface(
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(end = 30.dp),
                                contentAlignment = Alignment.CenterEnd

                            ) {
                                Symbol(
                                    com.pranshulgg.clockmaster.R.drawable.delete,
                                    size = 30.dp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    },

                    content = {
                        Surface(
                            color = tileColor,
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        end = 16.dp,
                                        start = 16.dp,
                                        bottom = 10.dp,
                                        top = 10.dp,
                                    ),
                            ) {
                                Column {
                                    Text(
                                        text = repeatDaysText(alarm.repeatDays),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = hourMinute,
                                            fontSize = 46.sp,
                                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.alignByBaseline()
                                        )

                                        amPm?.let {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = amPm,
                                                fontSize = 24.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,

                                                modifier = Modifier.alignByBaseline()
                                            )
                                        }


                                    }


                                    if (alarm.label.isNotEmpty())
                                        Text(
                                            text = alarm.label,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                }

                                Switch(

                                    checked = isEnabled,
                                    thumbContent = {
                                        if (isEnabled) {
                                            Symbol(
                                                com.pranshulgg.clockmaster.R.drawable.notifications_active,
                                                size = SwitchDefaults.IconSize,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onCheckedChange = { checked ->
                                        isEnabled = checked
                                        alarmViewModel.updateAlarm(alarm.copy(enabled = checked))
                                        try {
                                            if (checked) {
                                                AlarmScheduler.scheduleAlarm(
                                                    context,
                                                    alarm.id,
                                                    dayOfWeek = alarm.repeatDays,
                                                    hour = alarm.hour,
                                                    minute = alarm.minute,
                                                    label = alarm.label,
                                                    soundUri = alarm.sound
                                                )
                                            } else {
                                                AlarmScheduler.cancelAlarm(context, alarm.id)
                                            }

                                        } catch (e: SecurityException) {
                                            Toast.makeText(
                                                context,
                                                "Unable to schedule exact alarm",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    },

                                    )

                            }


                        }


                    }


                )


            }

        }
        item {
            Spacer(Modifier.height(130.dp))
        }

//        item {
//
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Column {
//                    Text("Keep service running", fontWeight = FontWeight.W600)
//                    Text(
//                        "Ensures alarms work even if app is closed",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//
//                Switch(
//                    checked = keepServiceRunning,
//                    onCheckedChange = { checked ->
//                        keepServiceRunning = checked
//
//                        val intent = Intent(context, AlarmAlwaysForegroundService::class.java)
//
//                        if (checked) {
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                context.startForegroundService(intent)
//                            } else {
//                                context.startService(intent)
//                            }
//                        } else {
//                            context.stopService(intent)
//                        }
//                    }
//                )
//            }

//        }
    }


}

