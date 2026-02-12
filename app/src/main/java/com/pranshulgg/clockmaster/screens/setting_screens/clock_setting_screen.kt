package com.pranshulgg.clockmaster.screens.setting_screens

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.ui.components.SettingSection
import com.pranshulgg.clockmaster.ui.components.SettingTile
import com.pranshulgg.clockmaster.ui.components.SettingsTileIcon
import com.pranshulgg.clockmaster.ui.components.Symbol

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ClockSettings(
    navController: NavController,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var is24hrFormat by remember {
        mutableStateOf(
            PreferencesHelper.getBool("is24hr") ?: false
        )
    }

    var showSecondsClock by remember {
        mutableStateOf(
            PreferencesHelper.getBool("showClockSeconds") ?: false
        )
    }

    var confirmDeletingClock by remember {
        mutableStateOf(
            PreferencesHelper.getBool("confirmDeletingClockItem") ?: false
        )
    }

    var currentWorldClockStyle by remember {
        mutableStateOf(
            PreferencesHelper.getString("WorldClockStyle") ?: "Digital"
        )
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = { Text("Clock") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                    title = "General",
                    tiles = listOf(
                        SettingTile.SwitchTile(
                            leading = { SettingsTileIcon(R.drawable.timer_10_select) },
                            title = "Display time with seconds",
                            checked = showSecondsClock,
                            onCheckedChange = { checked ->
                                showSecondsClock = checked
                                PreferencesHelper.setBool("showClockSeconds", checked)
                            }
                        ),

                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.nest_clock_farsight_analog_filled) },
                            title = "Time format",
                            options = listOf("12 hr", "24 hr"),
                            selectedOption = if (is24hrFormat) "24 hr" else "12 hr",
                            onOptionSelected = { selectedOption ->
                                if (selectedOption == "24 hr") {
                                    is24hrFormat = true
                                    PreferencesHelper.setBool("is24hr", true)
                                } else {
                                    is24hrFormat = false
                                    PreferencesHelper.setBool("is24hr", false)
                                }
                            }
                        ),
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.farsight_digital) },
                            title = "Clock style",
                            options = listOf("Digital", "Analog"),
                            selectedOption = currentWorldClockStyle,
                            onOptionSelected = { selectedOption ->
                                currentWorldClockStyle = selectedOption
                                PreferencesHelper.setString("WorldClockStyle", selectedOption)
                            }
                        ),
                    )


                )
                Spacer(Modifier.height(10.dp))
                SettingSection(
                    title = "Behavior",
                    tiles = listOf(
                        SettingTile.SwitchTile(
                            leading = { SettingsTileIcon(R.drawable.warning) },
                            title = "Confirm before deleting",
                            description = "Ask for confirmation before removing a world clock item",
                            checked = confirmDeletingClock,
                            onCheckedChange = { checked ->
                                confirmDeletingClock = checked
                                PreferencesHelper.setBool("confirmDeletingClockItem", checked)
                            }
                        ),
                    )
                )

            }
        }

    }

}

