package com.pranshulgg.clockmaster.ui.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.utils.Radius

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNav(selectedItem: Int, onItemSelected: (Int) -> Unit, onClick: () -> Unit) {
    val systemInsets = WindowInsets.systemBars.asPaddingValues()


    Box(
        Modifier.fillMaxWidth(),
    ) {

        HorizontalFloatingToolbar(
            modifier = Modifier
                .padding(
                    top = ScreenOffset,
                    bottom = systemInsets.calculateBottomPadding() + ScreenOffset
                )
                .align(Alignment.BottomCenter)
                .zIndex(1f),
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            expanded = true,

            floatingActionButton = {
                FloatingToolbarDefaults.VibrantFloatingActionButton(
                    onClick = {
                        onClick()
                    }
                ) {
                    Symbol(
                        if (selectedItem == 2) R.drawable.restart_alt else R.drawable.add,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            },
            contentPadding = PaddingValues(top = 0.dp, bottom = 0.dp, end = 8.dp, start = 8.dp),
            content = {
                BottomNavRow(selectedItem, onItemSelected)
            }
        )
    }

}

@Composable
fun BottomNavRow(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val labelList = listOf("Alarm", "Clock", "Stopwatch", "Timer")
    val unSelectedIcons = listOf(
        R.drawable.alarm_outlined,
        R.drawable.schedule_outlined,
        R.drawable.timer_outlined,
        R.drawable.hourglass_empty
    )
    val selectedIcons = listOf(
        R.drawable.alarm_filled,
        R.drawable.schedule_filled,
        R.drawable.timer_filled,
        R.drawable.hourglass_top
    )
    val colorScheme = MaterialTheme.colorScheme

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        labelList.forEachIndexed { index, label ->
            Tooltip(label, preferredPosition = TooltipAnchorPosition.Above, spacing = 10.dp) {
                IconToggleButton(
                    modifier = Modifier.size(48.dp),
                    checked = selectedItem == index,
                    onCheckedChange = { onItemSelected(index) },
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        containerColor = Color.Transparent,
                        checkedContainerColor = colorScheme.surfaceContainer
                    )
                ) {
                    Crossfade(selectedItem == index) {
                        if (it) Symbol(selectedIcons[index], color = colorScheme.onSurface)
                        else Symbol(unSelectedIcons[index], color = colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}
