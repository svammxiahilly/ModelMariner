package com.pranshulgg.clockmaster.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.models.HomeViewModel
import com.pranshulgg.clockmaster.models.TimersViewModel
import com.pranshulgg.clockmaster.models.TimezoneViewModel
import com.pranshulgg.clockmaster.services.TimerForegroundService
import com.pranshulgg.clockmaster.ui.components.AddTimerSheet
import com.pranshulgg.clockmaster.ui.components.AlarmBottomSheet
import com.pranshulgg.clockmaster.ui.components.BottomNav
import com.pranshulgg.clockmaster.ui.components.DropdownMenu
import com.pranshulgg.clockmaster.ui.components.Symbol
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    viewModelTimezone: TimezoneViewModel,
) {
    val selectedItem = viewModel.selectedItem
    val appBarTitles = listOf("Alarm", "World clock", "Stopwatch", "Timer")
    var showSheetAlarm by remember { mutableStateOf(false) }
    var showingAddTimer by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModelTimers: TimersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(appBarTitles[selectedItem])
                },
                actions = {
                    DropdownMenu(navController)
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedItem != 2,
                enter = scaleIn(
                    initialScale = 0.8f,
                    animationSpec = motionScheme.defaultSpatialSpec()
                ) + fadeIn(),
                exit = scaleOut(
                    targetScale = 0.8f,
                    animationSpec = motionScheme.defaultSpatialSpec()
                ) + fadeOut(),
            ) {
                FloatingActionButton(

                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        0.dp,
                        pressedElevation = 0.dp,
                    ),
                    onClick = {
                        when (selectedItem) {
                            0 -> showSheetAlarm = true
                            1 -> navController.navigate("OpenTimezoneSearch")
                            3 -> showingAddTimer = true
                        }
                    },
                ) {
                    Symbol(
                        R.drawable.add,
                        size = 30.dp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

        },
        bottomBar = {
            BottomNav(
                selectedItem = selectedItem,
                onItemSelected = { index -> viewModel.selectedItem = index }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> AlarmScreen()
                1 -> WorldClockScreen(viewModel = viewModelTimezone)
                2 -> StopwatchScreen()
                3 -> TimersScreen(navController = navController)
            }
            if (showSheetAlarm) {
                AlarmBottomSheet(
                    onDismiss = { showSheetAlarm = false }
                )
            }

            if (showingAddTimer) {
                AddTimerSheet(onAdd = { timer ->
                    scope.launch {
                        viewModelTimers.addTimer(timer)
                        TimerForegroundService.startServiceIfTimersExist(context)
                    }
                }, onDismiss = { showingAddTimer = false })
            }
        }
    }


}
