package com.pranshulgg.clockmaster.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pranshulgg.clockmaster.helpers.PreferencesHelper

class HomeViewModel : ViewModel() {

    var currentDefaultSelectedTab = PreferencesHelper.getString("DefaultSelectedTab") ?: "Alarm"
    var selectedTab = when (currentDefaultSelectedTab) {
        "Alarm" -> 0
        "World clock" -> 1
        "Stopwatch" -> 2
        "Timer" -> 3
        else -> 0
    }
    var selectedItem by mutableIntStateOf(selectedTab)
}