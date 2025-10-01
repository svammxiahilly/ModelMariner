package com.pranshulgg.clockmaster.repository

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.os.SystemClock

object StopwatchRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var tickerJob: Job? = null

    private var accumulatedMs = 0L
    private var startClockMs = 0L

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs

    private val _laps = MutableStateFlow<List<Long>>(emptyList())
    val laps: StateFlow<List<Long>> = _laps

    private const val TICK_MS = 50L

    fun start() {
        if (_isRunning.value) return
        startClockMs = SystemClock.elapsedRealtime()
        _isRunning.value = true
        tickerJob = scope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                _elapsedMs.value = accumulatedMs + (now - startClockMs)
                delay(TICK_MS)
            }
        }
    }

    fun pause() {
        if (!_isRunning.value) return
        val now = SystemClock.elapsedRealtime()
        accumulatedMs += (now - startClockMs)
        tickerJob?.cancel()
        tickerJob = null
        _isRunning.value = false
        _elapsedMs.value = accumulatedMs
    }

    fun reset() {
        tickerJob?.cancel()
        tickerJob = null
        accumulatedMs = 0L
        startClockMs = 0L
        _isRunning.value = false
        _elapsedMs.value = 0L
        _laps.value = emptyList()
    }

    fun lap() {
        val lapTime = _elapsedMs.value
        _laps.value = listOf(lapTime) + _laps.value
    }
}
