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
import com.pranshulgg.clockmaster.repository.StopwatchRepository
import com.pranshulgg.clockmaster.services.StopwatchForegroundService
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

            val interactionSources = remember { List(3) { MutableInteractionSource() } }


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

                        FilledIconButton(
                            enabled = isRunning,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                            onClick = {
                                val i = Intent(ctx, StopwatchForegroundService::class.java).apply {
                                    action = StopwatchForegroundService.ACTION_LAP
                                }
                                ctx.startService(i)
                            },
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = interactionSources[0],
                            modifier = Modifier
                                .size(84.dp, 96.dp)
                                .animateWidth(interactionSources[0])
                        ) {
                            Symbol(
                                R.drawable.laps,
                                color = if (isRunning) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }, size = 30.dp
                            )
                        }
                    },
                    { state ->

                    }
                )
                customItem(
                    {
                        FilledIconToggleButton(
                            checked = isRunning,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val permissionCheck = ContextCompat.checkSelfPermission(
                                            ctx, android.Manifest.permission.POST_NOTIFICATIONS
                                        )
                                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                            return@FilledIconToggleButton
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
                            },
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            shapes = IconButtonDefaults.toggleableShapes(),
                            interactionSource = interactionSources[1],
                            modifier = Modifier
                                .size(width = 128.dp, height = 96.dp)
                                .animateWidth(interactionSources[1])
                        ) {
                            if (!isRunning) {
                                Symbol(
                                    R.drawable.play_arrow,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    size = 30.dp
                                )
                            } else {
                                Symbol(
                                    R.drawable.pause,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    size = 30.dp
                                )
                            }
                        }
                    },
                    { state ->

                    }
                )

                customItem(
                    {

                        FilledIconButton(
                            enabled = !isRunning && elapsed != 0L,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                            onClick = {
                                val i = Intent(ctx, StopwatchForegroundService::class.java).apply {
                                    action = StopwatchForegroundService.ACTION_RESET
                                }
                                ctx.startService(i)

                            },
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = interactionSources[2],
                            modifier = Modifier
                                .size(84.dp, 96.dp)
                                .animateWidth(interactionSources[2])
                        ) {
                            Symbol(
                                R.drawable.restart_alt,
                                color = if (!isRunning && elapsed != 0L) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }, size = 30.dp
                            )
                        }
                    },
                    { state ->

                    }
                )

            }
//            Spacer(Modifier.height(12.dp))
//
//            FilledTonalButton(
//                enabled = isRunning,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
//                ),
//                onClick = {
//                    val i = Intent(ctx, StopwatchForegroundService::class.java).apply {
//                        action = StopwatchForegroundService.ACTION_LAP
//                    }
//                    ctx.startService(i)
//                },
//                shapes = ButtonDefaults.shapes(),
//                modifier = Modifier
//                    .size(width = 224.dp + 12.dp, height = 86.dp)
//            ) {
//                Text(
//                    text = "Lap",
//                    color = if (isRunning) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface.copy(
//                        alpha = 0.38f
//                    ),
//                    fontSize = 30.sp
//                )
//            }

            Spacer(Modifier.height(100.dp))
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
