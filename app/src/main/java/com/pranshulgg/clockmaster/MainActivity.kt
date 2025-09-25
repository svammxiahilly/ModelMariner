package com.pranshulgg.clockmaster

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.screens.HomeScreen
import com.pranshulgg.clockmaster.screens.SettingsPage
import com.pranshulgg.clockmaster.screens.setting_screens.AppearanceScreen
import com.pranshulgg.clockmaster.ui.theme.ClockMasterTheme
import com.pranshulgg.clockmaster.utils.NavTransitions

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterial3ExpressiveApi::class,
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        PreferencesHelper.init(this)

        val darkTheme = PreferencesHelper.getBool("dark_theme") ?: false

        setTheme(
            if (darkTheme) R.style.Theme_ClockMaster_DarkLaunch
            else R.style.Theme_ClockMaster_LightLaunch
        )
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()
            val motionScheme = motionScheme
            var darkTheme by remember {
                mutableStateOf(
                    PreferencesHelper.getBool("dark_theme") ?: false
                )
            }
            var colorSeed by remember {
                mutableStateOf(
                    PreferencesHelper.getString("seedColor") ?: "0xff0000FF"
                )
            }

            var useDynamicColor by remember {
                mutableStateOf(
                    PreferencesHelper.getBool("useDynamicColors") ?: false
                )
            }

            var useExpressiveColor by remember {
                mutableStateOf(
                    PreferencesHelper.getBool("useExpressiveColor") ?: true
                )
            }
            val context = LocalContext.current;

            ClockMasterTheme(
                darkTheme = darkTheme,
                seedColor = Color(colorSeed.removePrefix("0x").toLong(16).toInt()),
                dynamicColor = useDynamicColor,
                useExpressive = useExpressiveColor
            ) {
                window.setBackgroundDrawableResource(
                    if (darkTheme) R.color.black else R.color.white
                )
                NavHost(navController = navController, startDestination = "main") {
                    composable(
                        "main",
                        popEnterTransition = {
                            NavTransitions.popEnter(motionScheme)

                        },
                        popExitTransition = {
                            NavTransitions.popExit(motionScheme)
                        }
                    ) {
                        HomeScreen(navController)
                    }
                    composable(
                        "OpenSettings",
                        enterTransition = {
                            NavTransitions.enter(motionScheme)
                        },
                        exitTransition = {
                            NavTransitions.exit(motionScheme)

                        },
                        popEnterTransition = {
                            NavTransitions.popEnter(motionScheme)

                        },
                        popExitTransition = {
                            NavTransitions.popExit(motionScheme)
                        }
                    ) {
                        SettingsPage(navController)
                    }
                    composable(
                        "OpenAppearanceSettingsScreen",
                        enterTransition = {
                            NavTransitions.enter(motionScheme)
                        },
                        exitTransition = {
                            NavTransitions.exit(motionScheme)

                        },
                        popEnterTransition = {
                            NavTransitions.popEnter(motionScheme)

                        },
                        popExitTransition = {
                            NavTransitions.popExit(motionScheme)
                        }
                    ) {
                        AppearanceScreen(
                            navController,
                            context,
                            onThemeChanged = { isDark ->
                                darkTheme = isDark
                            },
                            onSeedChanged = { color ->
                                colorSeed = color
                            },
                            onDynamicColorChanged = { useDynamicColors ->
                                useDynamicColor = useDynamicColors
                            },
                            onExpressiveColorChanged = { useExpressiveColors ->
                                useExpressiveColor = useExpressiveColors
                            },
                        )
                    }
                }
            }
        }
    }

}

