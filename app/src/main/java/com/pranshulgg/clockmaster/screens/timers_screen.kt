package com.pranshulgg.clockmaster.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.models.TimersViewModel
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
            .padding(end = 12.dp, start = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(timers, key = { it.id }) { timer ->
            var visible by remember { mutableStateOf(true) }

            val dismissState = rememberSwipeToDismissBoxState(
                initialValue = SwipeToDismissBoxValue.Settled,
                positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
            )

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
                            visible = false
                            scope.launch {
                                delay(300)
                                viewModel.removeTimer(timer.id)
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
                                if (timers.first { it.id == id }.state.name == "Running") {
                                    viewModel.pauseTimer(id)
                                } else {
                                    viewModel.resumeTimer(id)
                                }
                                TimerForegroundService.startServiceIfTimersExist(context)
                            },
                            onReset = { id -> viewModel.resetTimer(id) },
                            onDelete = { id -> viewModel.removeTimer(id) },
                            onEditLabel = { id, newLabel ->
                                viewModel.updateLabel(
                                    id,
                                    newLabel
                                )
                            },
                            onOpenFullscreen = { t ->
                                navController.navigate("fullscreen/${t.id}")
                            }
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