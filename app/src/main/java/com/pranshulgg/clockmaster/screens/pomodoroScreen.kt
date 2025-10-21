package com.pranshulgg.clockmaster.screens

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.models.PomodoroViewModel
import com.pranshulgg.clockmaster.models.TimerState
import com.pranshulgg.clockmaster.services.PomodoroService
import com.pranshulgg.clockmaster.services.TimerAlarmService
import com.pranshulgg.clockmaster.services.TimerForegroundService
import com.pranshulgg.clockmaster.ui.components.Symbol

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PomodoroScreen(viewModel: PomodoroViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current.applicationContext

    val mode by viewModel.mode.collectAsState()
    val remainingMs by viewModel.remainingMs.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val focusMin by viewModel.focusMinutes.collectAsState()
    val shortMin by viewModel.shortMinutes.collectAsState()
    val longMin by viewModel.longMinutes.collectAsState()
    val autoNext by viewModel.autoStartNext.collectAsState()
    val completed by viewModel.completedCycles.collectAsState()
    val beforeLong by viewModel.cyclesBeforeLong.collectAsState()
    val (nextLabel, nextTime) = viewModel.getUpcomingSessionInfo()


    val totalMs = when (mode) {
        com.pranshulgg.clockmaster.helpers.PomodoroMode.FOCUS -> focusMin * 60_000L
        com.pranshulgg.clockmaster.helpers.PomodoroMode.SHORT_BREAK -> shortMin * 60_000L
        com.pranshulgg.clockmaster.helpers.PomodoroMode.LONG_BREAK -> longMin * 60_000L
    }

    val currentMode =
        if (mode == com.pranshulgg.clockmaster.helpers.PomodoroMode.FOCUS) "Focus" else if (mode == com.pranshulgg.clockmaster.helpers.PomodoroMode.SHORT_BREAK) "Short break" else "Long break"


    val progress = if (totalMs > 0) {
        val raw = 1f - (remainingMs.toFloat() / totalMs.toFloat())
        raw.coerceIn(0f, 1f)
    } else 0f


    val thickStrokeWidth = with(LocalDensity.current) { 14.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) { Stroke(width = thickStrokeWidth, cap = StrokeCap.Round) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                title = {
                    Text(
                        currentMode,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Symbol(
                            R.drawable.arrow_back,
                            desc = "Back",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

//        TabRow(selectedTabIndex = mode.ordinal) {
//            Tab(
//                selected = mode == com.pranshulgg.clockmaster.helpers.PomodoroMode.FOCUS,
//                onClick = { viewModel.changeMode(com.pranshulgg.clockmaster.helpers.PomodoroMode.FOCUS) }) {
//                Text("Focus")
//            }
//            Tab(
//                selected = mode == com.pranshulgg.clockmaster.helpers.PomodoroMode.SHORT_BREAK,
//                onClick = { viewModel.changeMode(com.pranshulgg.clockmaster.helpers.PomodoroMode.SHORT_BREAK) }) {
//                Text("Short")
//            }
//            Tab(
//                selected = mode == com.pranshulgg.clockmaster.helpers.PomodoroMode.LONG_BREAK,
//                onClick = { viewModel.changeMode(com.pranshulgg.clockmaster.helpers.PomodoroMode.LONG_BREAK) }) {
//                Text("Long")
//            }
//        }


            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
                        .defaultMinSize(minHeight = 20.dp, minWidth = 100.dp),
                    contentAlignment = Alignment.Center,

                    ) {
                    Text(
                        "Next • $nextLabel",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.W600
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(screenWidth.value.dp / 1.15f)
            ) {
                val circleSize = maxWidth
                val text = formatMsToTime(remainingMs)

                val factor = when {
                    remainingMs >= 3600_000 -> 0.7f
                    remainingMs < 60_000 -> 1.5f
                    else -> 0.8f
                }

                val fontSize = circleSize.value / (text.length * factor)

                val animatedProgress by
                animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,

                    )

                CircularWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    stroke = thickStroke,
                    trackStroke = thickStroke,
                    wavelength = 60.dp,
                    waveSpeed = 40.dp,
                    amplitude = { _ -> (progress.coerceIn(0f, 1f) * 1.2f).coerceAtMost(1f) }

                )


                Text(
                    text = text,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )


            }


            Spacer(Modifier.height(26.dp))

            Column(
                Modifier.padding(end = 28.dp, start = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ToggleButton(
                    modifier = Modifier
                        .height(ButtonDefaults.LargeContainerHeight + 10.dp)
                        .fillMaxWidth(),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shapes = ToggleButtonDefaults.shapes(),
                    checked = isRunning,
                    onCheckedChange = { checked ->
                        viewModel.startPauseToggle()
                        PomodoroService.enqueueStart(context)

                    }

                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 5.dp,
                            alignment = Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Symbol(
                            if (!isRunning) R.drawable.play_arrow else R.drawable.pause,
                            color = if (!isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            size = 36.dp
                        )
                        Text(
                            text = if (!isRunning) "Start" else "Pause",
                            fontSize = 30.sp,
                            color = if (!isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

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
                                    .height(ButtonDefaults.MediumContainerHeight + 18.dp)
                                    .animateWidth(interactionSources[0]),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                ),

                                shapes = ButtonDefaults.shapes(),
                                onClick = {
                                    viewModel.reset()
                                    PomodoroService.enqueueStop(context)
                                }
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        space = 5.dp,
                                        alignment = Alignment.CenterHorizontally
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Symbol(
                                        R.drawable.refresh,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Reset",
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                            }
                        },
                        { state ->

                        }
                    )

                    customItem(
                        {
                            Button(
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .weight(1f)
                                    .height(ButtonDefaults.MediumContainerHeight + 18.dp)
                                    .animateWidth(interactionSources[1]),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                ),
                                shapes = ButtonDefaults.shapes(),
                                onClick = {
                                    viewModel.skip()

                                }

                            ) {


                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        space = 5.dp,
                                        alignment = Alignment.CenterHorizontally
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Symbol(
                                        R.drawable.fast_forward,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Skip",
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        { state ->

                        }
                    )

                }
            }
        }

//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text("Auto start next")
//            Spacer(modifier = Modifier.width(8.dp))
//            Switch(checked = autoNext, onCheckedChange = { viewModel.setAutoNext(it) })
//        }

//        DurationSetting(
//            label = "Focus (min)",
//            value = focusMin,
//            onChange = { viewModel.setFocusMinutes(it) }
//        )
//        DurationSetting(
//            label = "Short break (min)",
//            value = shortMin,
//            onChange = { viewModel.setShortMinutes(it) }
//        )
//        DurationSetting(
//            label = "Long break (min)",
//            value = longMin,
//            onChange = { viewModel.setLongMinutes(it) }
//        )
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                Button(onClick = {
//                    PomodoroService.enqueueStart(context)
//                }) {
//                    Text("Enable background")
//                }
//                Button(onClick = {
//                    PomodoroService.enqueueStop(context)
//                }) {
//                    Text("Stop background")
//                }
//            }
    }
}

@Composable
private fun DurationSetting(label: String, value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("$label: $value")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (value > 1) onChange(value - 1) }) { Text("-") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onChange(value + 1) }) { Text("+") }
        }
    }
}

private fun formatMsToTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}
