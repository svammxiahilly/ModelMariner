package com.pranshulgg.clockmaster.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.ScreenSaverActivity

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

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