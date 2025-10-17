package com.pranshulgg.clockmaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranshulgg.clockmaster.models.TimerItem
import com.pranshulgg.clockmaster.models.TimerState
import com.pranshulgg.clockmaster.roomDB.AlarmEntity
import com.pranshulgg.clockmaster.utils.bottomPadding
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddTimerSheet(
    onAdd: (TimerItem) -> Unit,
    onDismiss: () -> Unit
) {
    var hours by remember { mutableStateOf(0f) }
    var minutes by remember { mutableStateOf(0f) }
    var seconds by remember { mutableStateOf(0f) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    val label = buildString {
        if (hours.toInt() > 0) append("${hours.toInt()}h ")
        if (minutes.toInt() > 0) append("${minutes.toInt()}m ")
        if (seconds.toInt() > 0) append("${seconds.toInt()}s ")
        append("Timer")
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(end = 16.dp, start = 16.dp, bottom = 16.dp)) {


            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                modifier = Modifier.fillMaxWidth()

            ) {
                CircularItemTimerText(
                    String.format(
                        Locale.getDefault(),
                        "%02dh",
                        hours.toInt(),
                    ), modifier = Modifier.size(68.dp, 50.dp)
                )

                CircularItemTimerText(
                    String.format(
                        Locale.getDefault(),
                        "%02dm",
                        minutes.toInt(),
                    ), modifier = Modifier.size(73.dp, 50.dp)
                )

                CircularItemTimerText(
                    String.format(
                        Locale.getDefault(),
                        "%02ds",
                        seconds.toInt(),
                    ), modifier = Modifier.size(68.dp, 50.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))


            Row(horizontalArrangement = Arrangement.Center) {
                CircularItem("H")
                Spacer(Modifier.width(10.dp))
                Slider(
                    value = hours,
                    onValueChange = { if (it <= 23f) hours = it },
                    valueRange = 0f..23f,
                )
            }
            Spacer(Modifier.height(10.dp))


            Row(horizontalArrangement = Arrangement.Center) {
                CircularItem("M")
                Spacer(Modifier.width(10.dp))
                Slider(
                    value = minutes,
                    onValueChange = { if (it <= 59f) minutes = it },
                    valueRange = 0f..59f,
                )
            }
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                CircularItem("S")
                Spacer(Modifier.width(10.dp))
                Slider(
                    value = seconds,
                    onValueChange = { if (it <= 59f) seconds = it },
                    valueRange = 0f..59f,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))



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
                    enabled = hours != 0f || minutes != 0f || seconds != 0f,
                    onClick = {
                        val totalSeconds =
                            (hours.toInt() * 3600) + (minutes.toInt() * 60) + seconds.toInt()
                        if (totalSeconds > 0) {
                            val millis = totalSeconds * 1000L
                            val item = TimerItem(
                                label = label,
                                initialMillis = millis,
                                remainingMillis = millis,
                                state = TimerState.Paused,
                                originalMillis = millis
                            )
                            onAdd(item)
                        }
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.defaultMinSize(minWidth = 90.dp, minHeight = 45.dp),
                ) {
                    Text("Add", fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircularItem(text: String) {
    Surface(
        shape = MaterialShapes.Cookie9Sided.toShape(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .width(46.dp)
            .height(46.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 18.sp)
        }
    }

}

@Composable
fun CircularItemTimerText(text: String, modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 24.sp,
                fontWeight = FontWeight.W600
            )
        }
    }

}
