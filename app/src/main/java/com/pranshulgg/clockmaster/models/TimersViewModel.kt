package com.pranshulgg.clockmaster.models

import androidx.lifecycle.viewModelScope
import com.pranshulgg.clockmaster.repository.TimerRepository
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pranshulgg.clockmaster.helpers.toEntity
import com.pranshulgg.clockmaster.helpers.toTimerItem
import com.pranshulgg.clockmaster.repository.TimersRepository
import com.pranshulgg.clockmaster.roomDB.AppDatabase

class TimersViewModel(application: Application) : AndroidViewModel(application) {

    private val timersDao = AppDatabase.getDatabase(application).timerDao()
    private val timersRepo = TimersRepository(timersDao)

    val timers = TimerRepository.timers

    init {
        viewModelScope.launch {
            timersRepo.getAllTimers().collect { entities ->
                val items = entities.map { it.toTimerItem() }
                TimerRepository.setAll(items)
            }
        }
    }

    fun addTimer(timer: TimerItem) = viewModelScope.launch {
        TimerRepository.addTimer(timer)
        timersRepo.insert(timer.toEntity())
    }

    fun removeTimer(id: String) = viewModelScope.launch {
        TimerRepository.removeTimer(id)
        val entity = timersRepo.get(id)
        if (entity != null) timersRepo.delete(entity)
    }

    fun pauseTimer(id: String) = viewModelScope.launch {
        TimerRepository.pauseTimer(id)
        persistTimerById(id)
    }

    fun resumeTimer(id: String) = viewModelScope.launch {
        TimerRepository.resumeTimer(id)
        persistTimerById(id)
    }

    fun resetTimer(id: String) = viewModelScope.launch {
        TimerRepository.resetTimer(id)
        persistTimerById(id)
    }

    fun resetAll() = viewModelScope.launch {
        TimerRepository.resetAll()
        TimerRepository.timers.value.forEach { timersRepo.insert(it.toEntity()) }
    }

    fun updateLabel(id: String, newLabel: String) = viewModelScope.launch {
        TimerRepository.updateLabel(id, newLabel)
        persistTimerById(id)
    }

    fun updateRemaining(id: String, newRemainingMillis: Long) = viewModelScope.launch {
        TimerRepository.updateRemaining(id, newRemainingMillis)
        persistTimerById(id)
    }

    fun updateInitial(id: String, newInitial: Long) = viewModelScope.launch {
        TimerRepository.updateInitialMillis(id, newInitial)
        persistTimerById(id)
    }

    private suspend fun persistTimerById(id: String) {
        val item = TimerRepository.timers.value.firstOrNull { it.id == id } ?: return
        timersRepo.insert(item.toEntity())
    }
}
