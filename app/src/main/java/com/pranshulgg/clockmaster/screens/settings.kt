package com.pranshulgg.clockmaster.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.ui.components.SettingSection
import com.pranshulgg.clockmaster.ui.components.SettingTile
import com.pranshulgg.clockmaster.ui.components.SettingsTileIcon
import com.pranshulgg.clockmaster.ui.components.Symbol


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsPage(navController: NavController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = { Text("Settings") },
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
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {

            item {
                SettingSection(
                    tiles = listOf(
                        SettingTile.ActionTile(
                            title = "Appearance",
                            description = "Themes, animations, and display",
                            leading = { SettingsTileIcon(R.drawable.format_paint_filled) },
                            onClick = {
                                navController.navigate("OpenAppearanceSettingsScreen")
                            }
                        ),
                    )
                )

                Spacer(Modifier.height(16.dp))

                SettingSection(
                    tiles = listOf(
                        SettingTile.ActionTile(
                            title = "Clock",
                            description = "Clock style, format, and more",
                            leading = { SettingsTileIcon(R.drawable.schedule_filled) },
                            onClick = {
                                navController.navigate("OpenClockSettingScreen")
                            }
                        ),
                        SettingTile.ActionTile(
                            title = "Alarm",
                            description = "Volume, background service",
                            leading = { SettingsTileIcon(R.drawable.alarm_filled) },
                            onClick = {
                                navController.navigate("OpenAlarmSettingScreen")
                            }
                        ),
                        SettingTile.ActionTile(
                            title = "Screen saver",
                            description = "Brightness, style, theme",
                            leading = { SettingsTileIcon(R.drawable.mobile_text_2_filled) },
                            onClick = {
                                navController.navigate("OpenScreenSaverSettingScreen")
                            }
                        ),
                        SettingTile.ActionTile(
                            title = "Pomodoro",
                            description = "Breaks, cycles, autostart",
                            leading = { SettingsTileIcon(R.drawable.nest_clock_farsight_analog_filled) },
                            onClick = {
                                navController.navigate("OpenPomodoroSettingScreen")
                            }
                        ),
                    )
                )
            }
        }
    }

}
