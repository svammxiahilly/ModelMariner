package com.pranshulgg.clockmaster.repository

import com.pranshulgg.clockmaster.models.TimerItem
import com.pranshulgg.clockmaster.models.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object TimerRepository {
    private val _timers = MutableStateFlow<List<TimerItem>>(emptyList())
    val timers: StateFlow<List<TimerItem>> = _timers.asStateFlow()

    private val mutex = Mutex()

    suspend fun addTimer(timer: TimerItem) = mutex.withLock {
        _timers.value = _timers.value + timer
    }

    suspend fun updateTimer(updated: TimerItem) = mutex.withLock {
        _timers.value = _timers.value.map { if (it.id == updated.id) updated else it }
    }

    suspend fun removeTimer(id: String) = mutex.withLock {
        _timers.value = _timers.value.filterNot { it.id == id }
    }

    suspend fun resetTimer(id: String) = mutex.withLock {
        _timers.value = _timers.value.map {
            if (it.id == id) it.copyPaused(it.originalMillis) else it
        }
    }

    suspend fun resetAll() = mutex.withLock {
        _timers.value = _timers.value.map { it.copyPaused(it.originalMillis) }
    }

    suspend fun pauseTimer(id: String) = mutex.withLock {
        _timers.value = _timers.value.map {
            if (it.id == id && it.state == TimerState.Running) it.copyPaused(it.remainingMillis) else it
        }
    }

    suspend fun resumeTimer(id: String) = mutex.withLock {
        _timers.value = _timers.value.map {
            if (it.id == id && it.state != TimerState.Running && it.remainingMillis > 0L) it.copyRunning(
                it.remainingMillis
            ) else it
        }
    }

    suspend fun tick(elapsedMillis: Long): List<String> = mutex.withLock {
        val updated = _timers.value.map { timer ->
            if (timer.state == TimerState.Running) {
                val newRemaining = max(0L, timer.remainingMillis - elapsedMillis)
                if (newRemaining == 0L) timer.copyFinished() else timer.copyRunning(newRemaining)
            } else timer
        }
        _timers.value = updated
        return updated.filter { it.state == TimerState.Finished && it.remainingMillis == 0L }
            .map { it.id }
    }

    suspend fun setAll(timers: List<TimerItem>) = mutex.withLock {
        _timers.value = timers
    }

    suspend fun updateLabel(id: String, newLabel: String) = mutex.withLock {
        _timers.value = _timers.value.map { if (it.id == id) it.copy(label = newLabel) else it }
    }

    suspend fun updateRemaining(id: String, newRemainingMillis: Long) = mutex.withLock {
        _timers.value = _timers.value.map {
            if (it.id == id) it.copy(remainingMillis = newRemainingMillis) else it
        }
    }

    suspend fun updateInitialMillis(id: String, newInitial: Long) = mutex.withLock {
        _timers.value = _timers.value.map {
            if (it.id == id) it.copy(initialMillis = newInitial) else it
        }
    }
}
