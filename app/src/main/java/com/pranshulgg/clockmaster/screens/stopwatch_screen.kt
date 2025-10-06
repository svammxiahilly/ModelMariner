package com.pranshulgg.clockmaster.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatted,
                    fontSize = (screenWidth.value / 3).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider()

            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                itemsIndexed(laps) { idx, lapMs ->


                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(50.dp)

                    ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(96.dp),
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    ctx, android.Manifest.permission.POST_NOTIFICATIONS
                                )
                                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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

                val interactionSources = remember { List(2) { MutableInteractionSource() } }


                ButtonGroup(

                    overflowIndicator = { menuState ->
                        FilledIconButton(
                            onClick = {
                                if (menuState.isExpanded) {
                                    menuState.dismiss()
                                } else {
                                    menuState.show()
                                }
                            }
                        ) {

                        }
                    }
                ) {
                    customItem(
                        {
                            Button(
                                interactionSource = interactionSources[0],
                                modifier = Modifier
                                    .weight(1f)
                                    .height(ButtonDefaults.LargeContainerHeight - 5.dp)
                                    .animateWidth(interactionSources[0]),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ),

                                shapes = ButtonDefaults.shapes(),
                                onClick = {
                                    val i =
                                        Intent(ctx, StopwatchForegroundService::class.java).apply {
                                            action = StopwatchForegroundService.ACTION_RESET
                                        }
                                    ctx.startService(i)
                                }
                            ) {

                                Text(
                                    text = "Reset",
                                    fontSize = 26.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                            }
                        },
                        { state ->

                        }
                    )

                    customItem(
                        {
                            Button(
                                enabled = isRunning,
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .weight(1f)
                                    .height(ButtonDefaults.LargeContainerHeight - 5.dp)
                                    .animateWidth(interactionSources[1]),
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                            }
                        },
                        { state ->

                        }
                    )

                }



                Spacer(Modifier.height(50.dp))
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
    val seconds = (totalSec % 60).toInt()
    val minutes = (totalSec / 60).toInt()
    return String.format("%02d:%02d", minutes, seconds)
}
