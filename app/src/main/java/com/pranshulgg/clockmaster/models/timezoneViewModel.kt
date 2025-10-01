package com.pranshulgg.clockmaster.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pranshulgg.clockmaster.repository.TimezoneRepository
import com.pranshulgg.clockmaster.roomDB.Timezone
import com.pranshulgg.clockmaster.roomDB.TimezoneDAO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimezoneViewModel(private val repository: TimezoneRepository) : ViewModel() {

    val timezones: StateFlow<List<Timezone>> =
        repository.allTimezones.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addTimezone(timezone: Timezone) = viewModelScope.launch {
        repository.insert(timezone)
    }

    fun removeTimezone(zoneId: String) = viewModelScope.launch {
        repository.delete(zoneId)
    }
}

class TimezoneViewModelFactory(private val repository: TimezoneRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimezoneViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimezoneViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
