package com.pranshulgg.clockmaster.screens

import android.icu.text.DateFormat
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.models.TimerState
import com.pranshulgg.clockmaster.models.TimersViewModel
import com.pranshulgg.clockmaster.services.TimerAlarmService
import com.pranshulgg.clockmaster.services.TimerForegroundService
import com.pranshulgg.clockmaster.ui.components.Symbol
import com.pranshulgg.clockmaster.ui.components.Tooltip
import com.pranshulgg.clockmaster.utils.bottomPadding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalTextApi::class
)
@Composable
fun FullscreenTimerScreen(
    timerId: String,
    onBack: () -> Unit,
    viewModel: TimersViewModel
) {
    val timer = viewModel.timers.collectAsState().value.find { it.id == timerId }
        ?: return

    val context = LocalContext.current

    val progress = if (timer.initialMillis > 0) {
        val raw = 1f - (timer.remainingMillis.toFloat() / timer.initialMillis.toFloat())
        raw.coerceIn(0f, 1f)
    } else 0f


    val thickStrokeWidth = with(LocalDensity.current) { 14.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) { Stroke(width = thickStrokeWidth, cap = StrokeCap.Round) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val bgcolor =
        if (timer.remainingMillis.toInt() == 0) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = bgcolor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgcolor,
                ),
                title = { Text(timer.label, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                actions = {
                    Tooltip(
                        "Exit fullscreen",
                        preferredPosition = TooltipAnchorPosition.Below,
                        spacing = 10.dp
                    ) {

                        IconButton(
                            onClick = { onBack() },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Symbol(
                                R.drawable.close_fullscreen,
                                desc = "close full screen",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(26.dp))

            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(screenWidth.value.dp / 1.08f)
            ) {
                val circleSize = maxWidth
                val text = formatMillis(timer.remainingMillis)

                val factor = when {
                    timer.remainingMillis >= 3600_000 -> 0.7f
                    timer.remainingMillis < 60_000 -> 1.5f
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

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(
                            text = text,
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily(
                                Font(
                                    R.font.roboto_flex,
                                    variationSettings = FontVariation.Settings(
                                        FontVariation.width(150f),
                                        FontVariation.weight(1000)
                                    )
                                )
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-60).dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Symbol(
                                R.drawable.notification_sound,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 19.dp
                            )
                            Text(
                                getEndTime(timer.remainingMillis / 1000),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }


                }

            }

            Spacer(Modifier.height(100.dp))
            Column(
                Modifier.padding(end = 18.dp, start = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToggleButton(
                    modifier = Modifier
                        .height(ButtonDefaults.LargeContainerHeight - 5.dp)
                        .fillMaxWidth(),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shapes = ToggleButtonDefaults.shapes(),
                    checked = timer.state != TimerState.Paused,
                    onCheckedChange = { checked ->
                        if (timer.state == TimerState.Running) {
                            viewModel.pauseTimer(timer.id)
                        } else {
                            viewModel.resumeTimer(timer.id)
                        }
                        TimerForegroundService.startServiceIfTimersExist(context)
                        TimerAlarmService.stopAlarm(context, timer.id)

                    }

                ) {

                    Text(
                        text = if (timer.state == TimerState.Paused) "Start" else "Pause",
                        fontSize = 26.sp,
                        color = if (timer.state == TimerState.Paused) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )

                }

                val interactionSources = remember { List(2) { MutableInteractionSource() } }


                ButtonGroup(

                    overflowIndicator = { menuState ->
                        FilledIconButton(
                            onClick = {
                                if (menuState.isShowing) {
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
                                    viewModel.updateInitial(timer.id, timer.originalMillis)

                                    viewModel.resetTimer(timer.id)
                                    TimerAlarmService.stopAlarm(context, timer.id)
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
                                    val newRemaining = timer.remainingMillis + 60_000L
                                    val newInitial = timer.initialMillis + 60_000L

                                    viewModel.updateRemaining(timer.id, newRemaining)
                                    viewModel.updateInitial(timer.id, newInitial)
                                    TimerAlarmService.stopAlarm(context, timer.id)
                                }

                            ) {

                                Text(
                                    text = "+1:00",
                                    fontSize = 26.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                            }
                        },
                        { state ->

                        }
                    )

                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
private fun getEndTime(remainingSeconds: Long): String {
    val is24HourFormat = PreferencesHelper.getBool("is24hr") ?: false
    val now = LocalDateTime.now()
    val endTime = now.plusSeconds(remainingSeconds)

    val pattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

    return endTime.format(formatter)
}


private fun formatMillis(ms: Long): String {
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val h = TimeUnit.MILLISECONDS.toHours(ms)

    return when {
        h > 0 -> String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        m > 0 -> String.format(Locale.US, "%d:%02d", m, s)
        else -> String.format(Locale.US, "%02d", s)
    }
}
