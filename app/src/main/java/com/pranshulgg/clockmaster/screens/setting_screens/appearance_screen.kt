package com.pranshulgg.clockmaster.screens.setting_screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.drawColorIndicator
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.ui.components.SettingSection
import com.pranshulgg.clockmaster.ui.components.SettingTile
import com.pranshulgg.clockmaster.ui.components.SettingsTileIcon
import com.pranshulgg.clockmaster.ui.components.Symbol
import com.pranshulgg.clockmaster.utils.bottomPadding
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun AppearanceScreen(
    navController: NavController,
    context: Context,
    onThemeChanged: (Boolean) -> Unit,
    onSeedChanged: (String) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onExpressiveColorChanged: (Boolean) -> Unit

) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var currentTheme by remember {
        mutableStateOf(
            PreferencesHelper.getString("AppTheme") ?: "Light"
        )
    }

    val isSysDark = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val controller = remember { ColorPickerController() }
    var pickedColor = PreferencesHelper.getString("seedColor") ?: "0xff0000FF"
    val defaultPickerColor = PreferencesHelper.getString("seedColor") ?: "0xff0000FF"
    val initialColorInt = remember(defaultPickerColor) {
        Color(defaultPickerColor.removePrefix("0x").toLong(16).toInt())
    }

    var useCustomColor by remember {
        mutableStateOf(
            PreferencesHelper.getBool("useCustomColor") ?: false
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


    fun hideColorSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }

    @Composable
    fun openColorPickerLead() {
        Surface(
            shape = RoundedCornerShape(50.dp),
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(

                modifier = Modifier
                    .width(24.dp)
                    .height(36.dp)
                    .background(
                        color = Color(
                            defaultPickerColor.removePrefix("0x").toLong(16).toInt()
                        )
                    )
                    .clickable(
                        onClick = { showBottomSheet = true }
                    ),
            ) {
            }
        }
    }


    Scaffold(
//        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            LargeTopAppBar(
                title = { Text("Appearance") },
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
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
//                )
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
                    title = "App looks",
                    tiles = listOf(
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.routine) },
                            title = "App Theme",
                            options = listOf("Light", "Dark", "System"),
                            selectedOption = currentTheme,
                            onOptionSelected = { selectedOption ->
                                currentTheme = selectedOption
                                val isDark = when (selectedOption) {
                                    "Light" -> false
                                    "Dark" -> true
                                    "System" -> isSysDark
                                    else -> false
                                }
                                PreferencesHelper.setBool("dark_theme", isDark)
                                PreferencesHelper.setString("AppTheme", selectedOption)

                                onThemeChanged(isDark)

                            }
                        ),

                        SettingTile.SwitchTile(
                            leading = {
                                if (useCustomColor) openColorPickerLead() else SettingsTileIcon(
                                    R.drawable.colorize
                                )
                            },
                            title = "Use custom color",
                            description = "Select a seed color to generate the theme",
                            checked = useCustomColor,
                            enabled = !useDynamicColor,
                            onCheckedChange = { checked ->
                                PreferencesHelper.setBool("useCustomColor", checked)

                                useCustomColor = checked

                                if (!checked) {
                                    PreferencesHelper.setString("seedColor", "0xff0000FF")
                                    onSeedChanged("0xff0000FF")
                                }

                            }
                        ),
                        SettingTile.SwitchTile(
                            leading = {
                                SettingsTileIcon(
                                    R.drawable.wallpaper_filled
                                )
                            },
                            title = "Dynamic colors",
                            description = "Use wallpaper colors",
                            checked = useDynamicColor,
                            enabled = !useCustomColor,
                            onCheckedChange = { checked ->
                                PreferencesHelper.setBool("useDynamicColors", checked)
                                PreferencesHelper.setBool("useCustomColor", !checked)
                                onDynamicColorChanged(checked)
                                if (useCustomColor) {
                                    useCustomColor = !checked
                                }
                                useDynamicColor = checked
                            }
                        ),


                        )

                )
            }

        }


        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                dragHandle = {
                    Box(
                        modifier = Modifier.padding(top = 22.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                    RoundedCornerShape(50.dp)
                                )
                        )
                    }
                }

            ) {

                Column {

                    Spacer(Modifier.height(12.dp))


                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(vertical = 10.dp),
                        initialColor = initialColorInt,
                        controller = controller,
                        drawOnPosSelected = {
                            drawColorIndicator(
                                controller.selectedPoint.value,
                                controller.selectedColor.value,
                            )
                        },
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            val hexColor = colorEnvelope.hexCode

                            val argbHex = "0xFF$hexColor"

                            pickedColor = argbHex

                        }
                    )
                    Spacer(Modifier.height(12.dp))

                    SettingSection(
                        tiles = listOf(


                            SettingTile.SingleSwitchTile(
                                title = "Use expressive palette",
                                checked = useExpressiveColor,
                                onCheckedChange = { checked ->
                                    PreferencesHelper.setBool("useExpressiveColor", checked)

                                    useExpressiveColor = checked
                                    onExpressiveColorChanged(checked)
                                }
                            )

                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, start = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = {
                            hideColorSheet()

                        }, shapes = ButtonDefaults.shapes()) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            PreferencesHelper.setString("seedColor", pickedColor)
                            onSeedChanged(pickedColor)
                            hideColorSheet()

                        }, shapes = ButtonDefaults.shapes()) {
                            Text("Save")
                        }
                    }
                }

            }
            Spacer(Modifier.height(bottomPadding() + 10.dp))

        }

    }

}


