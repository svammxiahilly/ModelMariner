package com.pranshulgg.clockmaster.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.models.TimezoneViewModel
import com.pranshulgg.clockmaster.roomDB.Timezone
import com.pranshulgg.clockmaster.ui.components.SettingSection
import com.pranshulgg.clockmaster.ui.components.SettingTile
import com.pranshulgg.clockmaster.ui.components.Symbol
import com.pranshulgg.clockmaster.utils.bottomPadding
import kotlinx.coroutines.delay
import kotlinx.serialization.builtins.MapSerializer
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.lastIndex
import kotlin.time.Duration.Companion.seconds

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimezoneSearchPage(navController: NavController, viewModel: TimezoneViewModel) {
    var query by remember { mutableStateOf("") }
    var showLoader by remember { mutableStateOf(true) }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        topBar = {
            Column(
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    modifier = Modifier.height(45.dp + 65.dp),
                    title = {
                        FullScreenSearchPage(
                            query = query,
                            onQueryChange = { query = it }, navController = navController
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Symbol(
                                R.drawable.arrow_back,
                                desc = "Back",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }


    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
            )
        ) {

            LaunchedEffect(Unit) {
                delay(1000L)
                showLoader = false
            }
            if (showLoader) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(modifier = Modifier.size(60.dp))
                }
            } else {
                TimezoneList(query = query, viewModel = viewModel, navController = navController)
            }
        }

    }

}


@Composable
fun FullScreenSearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    navController: NavController
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        }
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimezoneList(query: String, viewModel: TimezoneViewModel, navController: NavController) {
    var use24HourFormat by remember { mutableStateOf(PreferencesHelper.getBool("is24hr") ?: false) }

    val zones = remember {
        ZoneId.getAvailableZoneIds()
            .map { ZoneId.of(it) }
            .sortedBy { it.id }
    }


    val filteredZones = zones.filter {
        it.id.contains(query, ignoreCase = true)
    }

    val formatter =
        remember { DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "hh:mm a") }
    var currentTime by remember { mutableStateOf(ZonedDateTime.now(ZoneId.systemDefault())) }

    val clickGuard = remember { mutableStateOf(false) }

    LazyColumn(
    ) {

        itemsIndexed(filteredZones) { index, zone ->
            val time = currentTime.withZoneSameInstant(zone).format(formatter)
            val isFirst = index == 0
            val isLast = index == filteredZones.lastIndex
            val isOnly = filteredZones.size == 1
            val zonedTime = currentTime.withZoneSameInstant(zone)
            val offset = zonedTime.offset
            val offsetText = offset.id.replace("Z", "+00:00")
            val tz = TimeZone.getTimeZone(zone)
            val displayName = tz.getDisplayName(
                tz.inDaylightTime(Date()),
                TimeZone.LONG,
                Locale.getDefault()
            )

            val city = zone.id.split("/").last().replace("_", " ")

            ListItem(
                modifier = Modifier.clickable {
                    if (clickGuard.value) return@clickable
                    clickGuard.value = true
                    viewModel.addTimezone(
                        Timezone(
                            zoneId = city,
                            displayName = displayName,
                            offset = offsetText,
                            zone = zone
                        )
                    )
                    navController.popBackStack()
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                headlineContent = { Text(displayName) },
                supportingContent = { Text("$city, $offset") },
                trailingContent = {
                    Text(
                        time,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
            )

        }
    }
}
