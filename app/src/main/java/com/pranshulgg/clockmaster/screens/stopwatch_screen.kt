package com.pranshulgg.clockmaster.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.models.TimerState
import com.pranshulgg.clockmaster.repository.StopwatchRepository
import com.pranshulgg.clockmaster.services.StopwatchForegroundService
import com.pranshulgg.clockmaster.services.TimerForegroundService
import com.pranshulgg.clockmaster.ui.components.Symbol
import java.sql.Date


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTextApi::class)
@Composable
fun StopwatchScreen() {
    val ctx = LocalContext.current

    val elapsed by StopwatchRepository.elapsedMs.collectAsState()
    val isRunning by StopwatchRepository.isRunning.collectAsState()
    val laps by StopwatchRepository.laps.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val formatted = remember(elapsed) { formatElapsed(elapsed) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val i = Intent(ctx, StopwatchForegroundService::class.java).apply {
                    action = StopwatchForegroundService.ACTION_START_FOREGROUND
                }
                ContextCompat.startForegroundService(ctx, i)
            } else {
                Toast.makeText(context, "Notification permission is required", Toast.LENGTH_LONG)
                    .show()
            }
        }
    )

    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingId by remember { mutableStateOf<String?>(null) }

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


    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(if (laps.isEmpty()) 60.dp else 20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = if (laps.isEmpty()) 0.4f else 0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatted,
                    fontSize = (screenWidth.value / 5).sp,
                    fontFamily = FontFamily(
                        Font(
                            R.font.roboto_flex,
                            variationSettings = FontVariation.Settings(
                                FontVariation.width(150f),
                                FontVariation.weight(1000),
                                FontVariation.Setting("YTFI", 999f)
                            )
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }


            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                itemsIndexed(laps) { idx, lapMs ->


                    Surface(
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer

                    ) {
                        Box(
                            modifier = Modifier
                                .width(96.dp)
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "#${laps.size - idx}",
                                    textAlign = TextAlign.Center,
                                    color = getLapColor(idx, laps, MaterialTheme.colorScheme)
                                )
                                Text(
                                    formatElapsed(lapMs),
                                    textAlign = TextAlign.Center,
                                    color = getLapColor(idx, laps, MaterialTheme.colorScheme)
                                )
                            }

                        }
                    }
                }

            }

            Column(
                Modifier.padding(end = 18.dp, start = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToggleButton(
                    modifier = Modifier
                        .height(ButtonDefaults.LargeContainerHeight - 5.dp)
                        .fillMaxWidth(),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shapes = ToggleButtonDefaults.shapes(),
                    checked = isRunning,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val permissionCheck = ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.POST_NOTIFICATIONS
                                )
                                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                    pendingId = System.currentTimeMillis().toString()
                                    showPermissionDialog = true
                                    return@ToggleButton
                                }
                            }
                            val i =
                                Intent(ctx, StopwatchForegroundService::class.java).apply {
                                    action =
                                        StopwatchForegroundService.ACTION_START_FOREGROUND
                                }
                            ContextCompat.startForegroundService(ctx, i)
                        } else {


                            val i =
                                Intent(ctx, StopwatchForegroundService::class.java).apply {
                                    action = StopwatchForegroundService.ACTION_PAUSE
                                }
                            ctx.startService(i)

                        }

                    }

                ) {

                    Text(
                        text = if (!isRunning) "Start" else "Pause",
                        fontSize = 26.sp,
                        color = if (!isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )

                }

//                val interactionSources = remember { List(2) { MutableInteractionSource() } }
//
//
//                ButtonGroup(
//
//                    overflowIndicator = { menuState ->
//                        FilledIconButton(
//                            onClick = {
//                                if (menuState.isShowing) {
//                                    menuState.dismiss()
//                                } else {
//                                    menuState.show()
//                                }
//                            }
//                        ) {
//
//                        }
//                    }
//                ) {
//                    customItem(
//                        {
//                            ToggleButton(
//                                interactionSource = interactionSources[0],
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(ButtonDefaults.LargeContainerHeight - 5.dp)
//                                    .animateWidth(interactionSources[0]),
//                                colors = ToggleButtonDefaults.toggleButtonColors(
//                                    containerColor = MaterialTheme.colorScheme.primary,
//                                ),
//                                checked = isRunning,
//
//                                shapes = ToggleButtonDefaults.shapes(),
////                                onClick = {
////                                    val i =
////                                        Intent(ctx, StopwatchForegroundService::class.java).apply {
////                                            action = StopwatchForegroundService.ACTION_RESET
////                                        }
////                                    ctx.startService(i)
//                                onCheckedChange = { checked ->
//                                    if (checked) {
//                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                            val permissionCheck = ContextCompat.checkSelfPermission(
//                                                context,
//                                                android.Manifest.permission.POST_NOTIFICATIONS
//                                            )
//                                            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                                                pendingId = System.currentTimeMillis().toString()
//                                                showPermissionDialog = true
//                                                return@ToggleButton
//                                            }
//                                        }
//                                        val i =
//                                            Intent(
//                                                ctx,
//                                                StopwatchForegroundService::class.java
//                                            ).apply {
//                                                action =
//                                                    StopwatchForegroundService.ACTION_START_FOREGROUND
//                                            }
//                                        ContextCompat.startForegroundService(ctx, i)
//                                    } else {
//
//
//                                        val i =
//                                            Intent(
//                                                ctx,
//                                                StopwatchForegroundService::class.java
//                                            ).apply {
//                                                action = StopwatchForegroundService.ACTION_PAUSE
//                                            }
//                                        ctx.startService(i)
//
//                                    }
//
//                                }
////                                }
//                            ) {
//
//                                Text(
//                                    text = if (!isRunning) "Start" else "Pause",
//                                    fontSize = 26.sp,
//                                    color = MaterialTheme.colorScheme.onPrimary
//                                )
//
//                            }
//                        },
//                        { state ->
//
//                        }
//                    )
//
//                    customItem(
//                        {
                Button(
                    enabled = isRunning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonDefaults.LargeContainerHeight - 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        val i =
                            Intent(ctx, StopwatchForegroundService::class.java).apply {
                                action = StopwatchForegroundService.ACTION_LAP
                            }
                        ctx.startService(i)
                    }

                ) {

                    Text(
                        text = "Lap",
                        fontSize = 26.sp,
                        color = if (isRunning) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )

                }
//                        },
//                        { state ->
//
//                        }
//                    )


            }

        }
    }
}

@Composable
fun getLapColor(index: Int, laps: List<Long>, colorScheme: ColorScheme): Color {
    if (laps.isEmpty()) return colorScheme.secondary

    val fastest = laps.minOrNull() ?: return colorScheme.secondary
    val slowest = laps.maxOrNull() ?: return colorScheme.secondary

    val lapDuration = laps[index]

    return when (lapDuration) {
        fastest -> Color(0xFF4CAF50)
        slowest -> Color(0xFFF44336)
        else -> colorScheme.secondary
    }
}


private fun formatElapsed(ms: Long): String {
    val totalSec = ms / 1000
    val minutes = (totalSec / 60).toInt()
    val seconds = (totalSec % 60).toInt()
    val millis = (ms % 1000) / 10 // hundredths of a second (00–99)
    return String.format("%02d:%02d.%02d", minutes, seconds, millis)
}

