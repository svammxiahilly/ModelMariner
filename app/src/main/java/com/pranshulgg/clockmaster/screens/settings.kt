package com.pranshulgg.clockmaster.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.ui.components.SettingSection
import com.pranshulgg.clockmaster.ui.components.SettingTile
import com.pranshulgg.clockmaster.ui.components.Symbol


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(navController: NavController) {
    @Composable
    fun containerColor(isDark: Boolean) = object {
        val appearanceTile = if (isDark) Color(0xff534600) else Color(0xfff8e287)
        val OnAppearanceTile = if (isDark) Color(0xfff8e287) else Color(0xff534600)

        val clockTile = if (isDark) Color(0xff354e16) else Color(0xffcdeda3)
        val OnClockTile = if (isDark) Color(0xffcdeda3) else Color(0xff354e16)

        val alarmTile = if (isDark) Color(0xff284777) else Color(0xffd6e3ff)
        val OnAlarmTile = if (isDark) Color(0xffd6e3ff) else Color(0xff284777)

        val screenSaverTile = if (isDark) Color(0xff723523) else Color(0xffffdbd1)
        val OnScreenSaverTile = if (isDark) Color(0xffffdbd1) else Color(0xff723523)

        val pomodoroTile = if (isDark) Color(0xff004f54) else Color(0xff9df0f8)
        val OnPomodoroTile = if (isDark) Color(0xff9df0f8) else Color(0xff004f54)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    val darkTheme = PreferencesHelper.getBool("dark_theme") ?: false
    val colors = containerColor(darkTheme)
    Scaffold(
        topBar = {
            LargeTopAppBar(
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
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingSection(
                    tiles = listOf(
                        SettingTile.CategoryTile(
                            title = "Appearance",
                            description = "Themes, animations, and display",
                            color = colors.appearanceTile,
                            leading = R.drawable.format_paint_filled,
                            onColor = colors.OnAppearanceTile,
                            onClick = {
                                navController.navigate("OpenAppearanceSettingsScreen")
                            }
                        ),
                    )
                )

                Spacer(Modifier.height(16.dp))

                SettingSection(
                    tiles = listOf(
                        SettingTile.CategoryTile(
                            title = "Clock",
                            description = "Clock style, format, and more",
                            color = colors.clockTile,
                            leading = R.drawable.schedule_filled,
                            onColor = colors.OnClockTile,
                            onClick = {
                                navController.navigate("OpenClockSettingScreen")
                            }
                        ),
                        SettingTile.CategoryTile(
                            title = "Alarm",
                            description = "Volume, background service",
                            color = colors.alarmTile,
                            leading = R.drawable.alarm_filled,
                            onColor = colors.OnAlarmTile,
                            onClick = {}
                        ),
                        SettingTile.CategoryTile(
                            title = "Screen saver",
                            description = "Brightness, style, theme",
                            color = colors.screenSaverTile,
                            leading = R.drawable.mobile_text_2_filled,
                            onColor = colors.OnScreenSaverTile,
                            onClick = {}
                        ),
                        SettingTile.CategoryTile(
                            title = "Pomodoro",
                            description = "Breaks, cycles, autostart",
                            color = colors.pomodoroTile,
                            leading = R.drawable.nest_clock_farsight_analog_filled,
                            onColor = colors.OnPomodoroTile,
                            onClick = {}
                        ),
                    )
                )
            }
        }
    }

}
