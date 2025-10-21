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
fun ScreenSaverSettings(
    navController: NavController,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var selectedScreenSaverBrightness by remember {
        mutableStateOf(
            PreferencesHelper.getInt("ScreenSaverBrightness") ?: 30
        )
    }


    var useFullBlackForScreenSaver by remember {
        mutableStateOf(
            PreferencesHelper.getBool("useFullBlackForScreenSaver") ?: false
        )
    }

    var currentScreenSaverClockStyle by remember {
        mutableStateOf(
            PreferencesHelper.getString("ScreenSaverClockStyle") ?: "Digital"
        )
    }

    val context = LocalContext.current


    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Screen saver") },
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
                        SettingTile.DialogSliderTile(
                            title = "Screen saver brightness",
                            dialogTitle = "Brightness",
                            description = "${selectedScreenSaverBrightness}%",
                            isDescriptionAsValue = true,
                            leading = { SettingsTileIcon(R.drawable.brightness_7) },
                            initialValue = selectedScreenSaverBrightness.toFloat(),
                            valueRange = 0f..100f,
                            labelFormatter = { "${it.roundToInt()}%" },
                            onValueSubmitted = { newValue ->
                                val screenSaverBrightnessLevel = newValue.toInt()
                                selectedScreenSaverBrightness = screenSaverBrightnessLevel
                                PreferencesHelper.setInt(
                                    "ScreenSaverBrightness",
                                    screenSaverBrightnessLevel
                                )

                            }
                        ),

                        SettingTile.SwitchTile(
                            leading = { SettingsTileIcon(R.drawable.backlight_high_off) },
                            title = "Night mode",
                            description = "Uses a full black scheme for dark rooms",
                            checked = useFullBlackForScreenSaver,
                            onCheckedChange = { checked ->
                                useFullBlackForScreenSaver = checked
                                PreferencesHelper.setBool("useFullBlackForScreenSaver", checked)
                            }
                        ),
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.farsight_digital) },
                            title = "Clock style",
                            options = listOf("Digital", "Analog"),
                            selectedOption = currentScreenSaverClockStyle,
                            onOptionSelected = { selectedOption ->
                                currentScreenSaverClockStyle = selectedOption
                                PreferencesHelper.setString("ScreenSaverClockStyle", selectedOption)
                            }
                        ),
                    )
                )
            }
        }

    }

}

