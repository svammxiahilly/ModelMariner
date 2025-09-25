package com.pranshulgg.clockmaster.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.models.HomeViewModel
import com.pranshulgg.clockmaster.ui.components.BottomNav
import com.pranshulgg.clockmaster.ui.components.DropdownMenu
import com.pranshulgg.clockmaster.ui.components.Symbol

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val selectedItem = viewModel.selectedItem
    val appBarTitles = listOf<String>("Alarm", "World clock", "Stopwatch", "Timer")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(appBarTitles[selectedItem])
                },
                actions = {
                    DropdownMenu(navController)
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedItem != 2,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    targetOffsetX = { it + 16 },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ) + fadeOut()
            ) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    onClick = {},
                ) {
                    Symbol(
                        R.drawable.add,
                        size = 30.dp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

        },
        bottomBar = {
            BottomNav(
                selectedItem = selectedItem,
                onItemSelected = { index -> viewModel.selectedItem = index }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (selectedItem) {
                1 -> WorldClockScreen()
            }

        }
    }
}
