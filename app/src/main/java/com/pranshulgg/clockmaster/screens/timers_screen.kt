package com.pranshulgg.clockmaster.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.models.TimersViewModel
import com.pranshulgg.clockmaster.services.StopwatchForegroundService
import com.pranshulgg.clockmaster.services.TimerAlarmService
import com.pranshulgg.clockmaster.services.TimerForegroundService
import com.pranshulgg.clockmaster.ui.components.AddTimerSheet
import com.pranshulgg.clockmaster.ui.components.EmptyContainerPlaceholder
import com.pranshulgg.clockmaster.ui.components.Symbol
import com.pranshulgg.clockmaster.ui.components.TimerItemRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimersScreen(
    viewModel: TimersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavController
) {
    val timers by viewModel.timers.collectAsState()
    val scope = rememberCoroutineScope()
    var showingAdd by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingId by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val i = Intent(context, StopwatchForegroundService::class.java).apply {
                    action = StopwatchForegroundService.ACTION_START_FOREGROUND
                }
                ContextCompat.startForegroundService(context, i)
            } else {
                Toast.makeText(context, "Notification permission is required", Toast.LENGTH_LONG)
                    .show()
            }
        }
    )

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Notification Permission Needed") },
            text = {
                Text(
                    "Notification permission is required to run the foreground service, " +
                            "send alerts when timers finish, and allow the app to continue working " +
                            "even when it’s in the background."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Grant", fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            }
        )
    }

    if (timers.isEmpty()) {
        Column {
            EmptyContainerPlaceholder(
                icon = R.drawable.hourglass_empty,
                text = "No timers"
            )
        }

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        items(timers, key = { it.id }) { timer ->
            var visible by remember { mutableStateOf(true) }

            val dismissState = rememberSwipeToDismissBoxState(
                initialValue = SwipeToDismissBoxValue.Settled,
                positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
            )



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
                                    viewModel.removeTimer(timer.id)
                                }
                            }, shapes = ButtonDefaults.shapes()
                        ) {
                            Text("Delete", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            scope.launch { dismissState.reset() }
                        }, shapes = ButtonDefaults.shapes()) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    title = { Text("Delete timer") },
                    text = { Text("Are you sure you want to delete this timer?") }
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

                            showDialog = true
                        }
                    },
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Surface(
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(end = 30.dp),
                                contentAlignment = Alignment.CenterEnd

                            ) {
                                Symbol(
                                    R.drawable.delete,
                                    size = 30.dp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    },

                    content = {
                        TimerItemRow(
                            timer,
                            onPauseResume = { id ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val permissionCheck = ContextCompat.checkSelfPermission(
                                        context, android.Manifest.permission.POST_NOTIFICATIONS
                                    )
                                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                        pendingId = id
                                        showPermissionDialog = true
                                        return@TimerItemRow
                                    }
                                }

                                if (timers.first { it.id == id }.state.name == "Running") {
                                    viewModel.pauseTimer(id)
                                } else {
                                    viewModel.resumeTimer(id)
                                }
                                TimerForegroundService.startServiceIfTimersExist(context)
                                TimerAlarmService.stopAlarm(context, id)
                            },
                            onReset = { id ->
                                val t = timers.firstOrNull { it.id == id }
                                if (t != null) {
                                    viewModel.updateInitial(id, t.originalMillis)
                                    viewModel.resetTimer(id)
                                }
                                TimerAlarmService.stopAlarm(context, id)
                            },
                            onDelete = { id ->
                                viewModel.removeTimer(id)
                                TimerAlarmService.stopAlarm(context, id)

                            },
                            onEditLabel = { id, newLabel ->
                                viewModel.updateLabel(
                                    id,
                                    newLabel
                                )
                            },
                            onOpenFullscreen = { t ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val permissionCheck = ContextCompat.checkSelfPermission(
                                        context, android.Manifest.permission.POST_NOTIFICATIONS
                                    )
                                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                        pendingId = t.id
                                        showPermissionDialog = true
                                        return@TimerItemRow
                                    }
                                }

                                navController.navigate("fullscreen/${t.id}")
                            },
                            results = timers,
                            index = timers.indexOf(timer)
                        )
                    }
                )

            }
        }

        item {
            Spacer(Modifier.height(130.dp))
        }
    }


}