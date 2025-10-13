package com.pranshulgg.clockmaster.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.models.TimezoneViewModel
import com.pranshulgg.clockmaster.roomDB.Timezone
import com.pranshulgg.clockmaster.ui.components.ClockDisplayText
import com.pranshulgg.clockmaster.ui.components.Symbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WorldClockScreen(viewModel: TimezoneViewModel) {
    val timezoneList by viewModel.timezones.collectAsState()
    var use24HourFormat by remember { mutableStateOf(PreferencesHelper.getBool("is24hr") ?: false) }


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

        ClockDisplayText(
            use24hr = PreferencesHelper.getBool("is24hr") ?: false,
            showSeconds = PreferencesHelper.getBool("showClockSeconds") ?: false
        )

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
                        onDelete = { tz -> viewModel.removeTimezone(timezone.zoneId) }
                    )
                    Spacer(Modifier.height(4.dp))
                }
                item {
                    Spacer(Modifier.height(130.dp))
                }
            }
        }

    }


}


@Composable
fun TimezoneRow(timezone: Timezone, time: String) {

    Surface(
        shape = RoundedCornerShape(22.dp)
    ) {

        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            headlineContent = { Text(timezone.zoneId, fontSize = 18.sp) },
            supportingContent = { Text(timezone.offset, fontSize = 16.sp) },
            trailingContent = {
                Text(
                    time,
                    fontSize = 34.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DismissibleTimezoneRow(
    timezone: Timezone,
    time: String,
    onDelete: (Timezone) -> Unit
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
                TimezoneRow(timezone, time)
            }

        )

    }

}


