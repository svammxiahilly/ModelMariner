package com.pranshulgg.clockmaster.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.pranshulgg.clockmaster.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNav(selectedItem: Int, onItemSelected: (Int) -> Unit) {
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


    NavigationBar(
    ) {
        labelList.forEachIndexed { index, labelList ->
            NavigationBarItem(
                onClick = {
                    onItemSelected(index)
                },
                selected = selectedItem == index,
                label = { Text(labelList, fontSize = 14.sp) },
                icon = {
                    Icon(
                        painter = painterResource(if (selectedItem == index) selectedIcons[index] else unSelectedIcons[index]),
                        contentDescription = null
                    )
                },
            )
        }
    }

}