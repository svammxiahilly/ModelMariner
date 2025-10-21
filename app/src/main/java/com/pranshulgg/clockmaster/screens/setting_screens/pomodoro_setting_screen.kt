package com.pranshulgg.clockmaster.screens.setting_screens

import android.media.AudioManager
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.ui.components.SettingSection
import com.pranshulgg.clockmaster.ui.components.SettingTile
import com.pranshulgg.clockmaster.ui.components.SettingsTileIcon
import com.pranshulgg.clockmaster.ui.components.Symbol
import kotlin.math.roundToInt

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun PomodoroSettings(
    navController: NavController,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    val context = LocalContext.current

    var pomoFocusTime by remember {
        mutableStateOf(
            PreferencesHelper.getInt("pomoFocusTime") ?: 25
        )
    }

    var pomoShortBreakTime by remember {
        mutableStateOf(
            PreferencesHelper.getInt("pomoShortBreakTime") ?: 5
        )
    }

    var pomoLongBreakTime by remember {
        mutableStateOf(
            PreferencesHelper.getInt("pomoLongBreakTime") ?: 15
        )
    }

    var autoStartSessionPomo by remember {
        mutableStateOf(
            PreferencesHelper.getBool("autoStartSessionPomo") ?: false
        )
    }

    var cyclesBeforeLongBreak by remember {
        mutableStateOf(
            PreferencesHelper.getInt("cyclesBeforeLongBreak") ?: 4
        )
    }


    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Pomodoro") },
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
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.center_focus_strong) },
                            optionLabel = { "$it minutes" },
                            title = "Focus length",
                            options = (15..60 step 5).map { it.toString() },
                            selectedOption = pomoFocusTime.toString(),
                            onOptionSelected = { selectedOption ->
                                pomoFocusTime = selectedOption.toInt()
                                PreferencesHelper.setInt("pomoFocusTime", selectedOption.toInt())

                            }
                        ),
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.hourglass_pause) },
                            optionLabel = { it + " ${if (it > "1") "minutes" else "minute"}" },
                            title = "Short break length",
                            options = (1..15).map { it.toString() },
                            selectedOption = pomoShortBreakTime.toString(),
                            onOptionSelected = { selectedOption ->
                                pomoShortBreakTime = selectedOption.toInt()
                                PreferencesHelper.setInt(
                                    "pomoShortBreakTime",
                                    selectedOption.toInt()
                                )

                            }
                        ),
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.coffee) },
                            optionLabel = { "$it minutes" },
                            title = "Long break length",
                            options = (5..60 step 5).map { it.toString() },
                            selectedOption = pomoLongBreakTime.toString(),
                            onOptionSelected = { selectedOption ->
                                pomoLongBreakTime = selectedOption.toInt()
                                PreferencesHelper.setInt(
                                    "pomoLongBreakTime",
                                    selectedOption.toInt()
                                )

                            }
                        ),
                    )
                )
                Spacer(Modifier.height(10.dp))

                SettingSection(
                    title = "Sessions",
                    tiles = listOf(
                        SettingTile.SwitchTile(
                            leading = { SettingsTileIcon(R.drawable.autostop) },
                            title = "Auto-Start Sessions",
                            description = "Automatically begin the next focus or break session without manual input",
                            checked = autoStartSessionPomo,
                            onCheckedChange = { checked ->
                                autoStartSessionPomo = checked
                                PreferencesHelper.setBool("autoStartSessionPomo", checked)

                            }
                        ),
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.cycle) },
                            optionLabel = { "$it cycles" },
                            title = "Focus Cycles Before Long Break",
                            dialogTitle = "Focus Cycles",
                            options = (4..10).map { it.toString() },
                            selectedOption = cyclesBeforeLongBreak.toString(),
                            onOptionSelected = { selectedOption ->
                                cyclesBeforeLongBreak = selectedOption.toInt()
                                PreferencesHelper.setInt(
                                    "cyclesBeforeLongBreak",
                                    selectedOption.toInt()
                                )

                            }
                        ),
                    )
                )
            }
        }

    }

}
