package com.pranshulgg.clockmaster.ui.components

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.ScreenSaverActivity
import com.pranshulgg.clockmaster.services.StopwatchForegroundService

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val i = Intent(context, StopwatchForegroundService::class.java).apply {
                    action = StopwatchForegroundService.ACTION_START_FOREGROUND
                }
                ContextCompat.startForegroundService(context, i)
            } else {
                Toast.makeText(context, "Notification permission is required", Toast.LENGTH_LONG)
                    .show()
            }
        }
    )

    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingId by remember { mutableStateOf<String?>(null) }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Notification Permission Needed") },
            text = {
                Text(
                    "Notification permission is required to run the foreground service, " +
                            "send alerts when timers finish, and allow the app to continue working " +
                            "even when it’s in the background."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Grant", fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            }
        )
    }

    Tooltip("More options", preferredPosition = TooltipAnchorPosition.Below, spacing = 10.dp) {
        IconButton(
            onClick = { expanded = !expanded }, shapes = IconButtonDefaults.shapes()
        ) {
            Symbol(
                R.drawable.more_vert,
                desc = "More options",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        shape = RoundedCornerShape(16.dp),
        offset = DpOffset(x = (-10).dp, y = (-48).dp)
    ) {
        DropdownMenuItem(
            text = { DropDownMenuText("Screen saver") },
            onClick = {
                expanded = false
                context.startActivity(Intent(context, ScreenSaverActivity::class.java))
            },
            leadingIcon = { DropDownMenuIcon(R.drawable.mobile_text_2_filled) }
        )
        DropdownMenuItem(
            text = { DropDownMenuText("Settings") },
            onClick = {
                expanded = false
                navController.navigate("OpenSettings")
            },
            leadingIcon = { DropDownMenuIcon(R.drawable.settings_filled) }
        )
        DropdownMenuItem(
            text = { DropDownMenuText("About") },
            onClick = {
                expanded = false
                navController.navigate("OpenAboutScreen")
            },
            leadingIcon = { DropDownMenuIcon(R.drawable.info_filled) }
        )
        DropdownMenuItem(
            text = { DropDownMenuText("Pomodoro") },
            onClick = {
                expanded = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        pendingId = System.currentTimeMillis().toString()
                        showPermissionDialog = true
                        return@DropdownMenuItem
                    }
                }
                navController.navigate("OpenPomodoroScreen")
            },
            leadingIcon = { DropDownMenuIcon(R.drawable.nest_clock_farsight_analog_filled) }
        )
    }

}


@Composable
fun DropDownMenuText(text: String) =
    Text(
        text,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(end = 10.dp)
    )

@Composable
fun DropDownMenuIcon(icon: Int) =
    Symbol(icon, color = MaterialTheme.colorScheme.onSurface, size = 22.dp, paddingStart = 3.dp)