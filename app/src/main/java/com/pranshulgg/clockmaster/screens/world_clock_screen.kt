package com.pranshulgg.clockmaster.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranshulgg.clockmaster.AnalogClockComposable
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.models.TimezoneViewModel
import com.pranshulgg.clockmaster.roomDB.Timezone
import com.pranshulgg.clockmaster.ui.components.ClockDisplayText
import com.pranshulgg.clockmaster.ui.components.Symbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun WorldClockScreen(viewModel: TimezoneViewModel) {
    val timezoneList by viewModel.timezones.collectAsState()
    var use24HourFormat by remember { mutableStateOf(PreferencesHelper.getBool("is24hr") ?: false) }

    val useAnalog by remember { mutableStateOf(PreferencesHelper.getString("WorldClockStyle") == "Analog") }
    val currentTime by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            val now = System.currentTimeMillis()
            value = now
            delay(1000 - (now % 1000))
        }
    }

    val formatter =
        remember { DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "hh:mm a") }
    Column {

        if (useAnalog) {
            AnalogWorldClock(currentMillis = currentTime)
        } else {
            ClockDisplayText(
                use24hr = PreferencesHelper.getBool("is24hr") ?: false,
                showSeconds = PreferencesHelper.getBool("showClockSeconds") ?: false
            )
        }




        if (timezoneList.isNotEmpty()) {
            Text(
                text = "Timezones",
                modifier = Modifier.padding(bottom = 10.dp, top = 26.dp, start = 12.dp),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W700,
            )


            LazyColumn(
                modifier = Modifier.padding(end = 12.dp, start = 12.dp)
            ) {
                items(timezoneList, key = { it.zoneId }) { timezone ->

                    val zonedTime = ZonedDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(currentTime),
                        timezone.zone
                    )
                    val time = zonedTime.format(formatter)

                    DismissibleTimezoneRow(
                        timezone = timezone,
                        time = time,
                        onDelete = { tz -> viewModel.removeTimezone(timezone.zoneId) },
                        currentTimeMs = currentTime,
                        useAnalog = useAnalog
                    )
                    Spacer(Modifier.height(6.dp))
                }
                item {
                    Spacer(Modifier.height(130.dp))
                }
            }
        }

    }


}


@Composable

fun TimezoneRow(timezone: Timezone, time: String, currentTimeMs: Long, useAnalog: Boolean) {

    Surface(
        shape = RoundedCornerShape(22.dp)
    ) {

        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            headlineContent = {
                Text(
                    timezone.zoneId,
                    fontSize = if (useAnalog) 24.sp else 18.sp,
                    modifier = if (useAnalog) Modifier.padding(start = 6.dp) else Modifier
                )
            },
            supportingContent = {
                Text(
                    timezone.offset,
                    fontSize = if (useAnalog) 21.sp else 16.sp,
                    modifier = if (useAnalog) Modifier.padding(start = 6.dp) else Modifier
                )
            },
            trailingContent = {
                if (useAnalog) {
                    AnalogWorldClock(
                        currentMillis = currentTimeMs,
                        100.dp,
                        showDate = false,
                        variantSmall = true
                    )
                } else {
                    Text(
                        time,
                        fontSize = 34.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }


            },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DismissibleTimezoneRow(
    timezone: Timezone,
    time: String,
    onDelete: (Timezone) -> Unit,
    currentTimeMs: Long,
    useAnalog: Boolean
) {
    var visible by remember { mutableStateOf(true) }

    val confirm = PreferencesHelper.getBool("confirmDeletingClockItem") ?: false

    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    )
    val scope = rememberCoroutineScope()
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
                            onDelete(timezone)
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
            title = { Text("Delete Timezone") },
            text = { Text("Are you sure you want to delete this timezone?") }
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
                            onDelete(timezone)
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
                            .padding(end = 20.dp),
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
                TimezoneRow(timezone, time, currentTimeMs, useAnalog)
            }

        )

    }

}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnalogWorldClock(
    currentMillis: Long,
    clockSize: Dp = 180.dp,
    showDate: Boolean = true,
    variantSmall: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val showSecondHand by remember {
        mutableStateOf(
            PreferencesHelper.getBool("showClockSeconds") ?: false
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (showDate) Modifier.fillMaxWidth() else Modifier

    ) {
        Surface(
            modifier = Modifier
                .size(clockSize),
            shape = MaterialShapes.Cookie12Sided.toShape(),
            color = if (variantSmall) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceContainer
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cal = Calendar.getInstance().apply { timeInMillis = currentMillis }
                    val seconds = cal.get(Calendar.SECOND)
                    val minutes = cal.get(Calendar.MINUTE)
                    val hours12 = cal.get(Calendar.HOUR)

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f * 0.88f

                    drawCircle(
                        color = Color.Transparent,
                        radius = radius + 8f,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = Color.Transparent,
                        radius = radius,
                        center = center
                    )

                    val tickLength = radius * 0.20f
                    val longTickLength = radius * 0.28f
                    val tickStroke = 3f
                    for (i in 0 until 12) {
                        val angleDeg = i * 30f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val outer = Offset(
                            x = center.x + (radius * kotlin.math.sin(angleRad)).toFloat(),
                            y = center.y - (radius * kotlin.math.cos(angleRad)).toFloat()
                        )
                        val inner = Offset(
                            x = center.x + ((radius - (if (i % 3 == 0) longTickLength else tickLength)) * kotlin.math.sin(
                                angleRad
                            )).toFloat(),
                            y = center.y - ((radius - (if (i % 3 == 0) longTickLength else tickLength)) * kotlin.math.cos(
                                angleRad
                            )).toFloat()
                        )
                        drawLine(
                            color = Color.Transparent,
                            start = inner,
                            end = outer,
                            strokeWidth = if (i % 3 == 0) (tickStroke + 1f) else tickStroke,
                            cap = StrokeCap.Round
                        )
                    }

                    val minuteFraction = minutes + seconds / 60f
                    val hourFraction = (hours12 % 12) + minuteFraction / 60f

                    val minuteAngleDeg = minuteFraction * 6f
                    val hourAngleDeg = hourFraction * 30f

                    val hourLength = radius * if (variantSmall) 0.4f else 0.5f
                    val hourAngleRad = Math.toRadians(hourAngleDeg.toDouble())
                    val hourEnd = Offset(
                        x = center.x + (hourLength * kotlin.math.sin(hourAngleRad)).toFloat(),
                        y = center.y - (hourLength * kotlin.math.cos(hourAngleRad)).toFloat()
                    )
                    drawLine(
                        color = colorScheme.secondary,
                        start = center,
                        end = hourEnd,
                        strokeWidth = if (variantSmall) 20f else 26f,
                        cap = StrokeCap.Round
                    )

                    val minuteLength = radius * if (variantSmall) 0.6f else 0.7f
                    val minuteAngleRad = Math.toRadians(minuteAngleDeg.toDouble())
                    val minuteEnd = Offset(
                        x = center.x + (minuteLength * kotlin.math.sin(minuteAngleRad)).toFloat(),
                        y = center.y - (minuteLength * kotlin.math.cos(minuteAngleRad)).toFloat()
                    )
                    drawLine(
                        color = colorScheme.primary,
                        start = center,
                        end = minuteEnd,
                        strokeWidth = if (variantSmall) 20f else 26f,
                        cap = StrokeCap.Round
                    )

                    if (showSecondHand) {
                        val secondAngleDeg = seconds * 6f
                        val secondAngleRad = Math.toRadians(secondAngleDeg.toDouble())
                        val secondLength = radius * if (variantSmall) 0.85f else 0.88f
                        val secondEnd = Offset(
                            x = center.x + (secondLength * kotlin.math.sin(secondAngleRad)).toFloat(),
                            y = center.y - (secondLength * kotlin.math.cos(secondAngleRad)).toFloat()
                        )

                        drawLine(
                            color = Color.Transparent,
                            start = center,
                            end = secondEnd,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        drawCircle(
                            color = colorScheme.tertiary,
                            radius = if (variantSmall) 10f else 14.5f,
                            center = secondEnd
                        )
                    }

                    drawCircle(
                        color = colorScheme.primary,
                        radius = 3.5f,
                        center = center
                    )
                }
            }
        }

        if (showDate) {
            Spacer(Modifier.height(8.dp))
            val dateStr = remember(currentMillis) {
                SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(currentMillis))
            }

            Text(
                text = dateStr,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

}

