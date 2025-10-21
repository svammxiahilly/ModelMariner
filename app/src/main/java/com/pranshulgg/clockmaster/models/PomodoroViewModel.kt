package com.pranshulgg.clockmaster.models

import android.app.Application
import android.content.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pranshulgg.clockmaster.helpers.PomodoroMode
import com.pranshulgg.clockmaster.services.PomodoroService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx = application.applicationContext

    // App-side state
    private val _mode = MutableStateFlow(PomodoroMode.FOCUS)
    val mode: StateFlow<PomodoroMode> = _mode

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _focusMinutes = MutableStateFlow(25)
    val focusMinutes: StateFlow<Int> = _focusMinutes

    private val _shortMinutes = MutableStateFlow(5)
    val shortMinutes: StateFlow<Int> = _shortMinutes

    private val _longMinutes = MutableStateFlow(15)
    val longMinutes: StateFlow<Int> = _longMinutes

    private val _autoStartNext = MutableStateFlow(false)
    val autoStartNext: StateFlow<Boolean> = _autoStartNext

    private val _completedCycles = MutableStateFlow(0)
    val completedCycles: StateFlow<Int> = _completedCycles

    private val _cyclesBeforeLong = MutableStateFlow(4)
    val cyclesBeforeLong: StateFlow<Int> = _cyclesBeforeLong


    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (intent.action == PomodoroService.BROADCAST_ACTION_STATE) {
                val m: PomodoroMode? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(
                        PomodoroService.EXTRA_MODE,
                        PomodoroMode::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(PomodoroService.EXTRA_MODE) as? PomodoroMode
                }
                val rem = intent.getLongExtra(PomodoroService.EXTRA_REMAIN_MS, 0L)
                val running = intent.getBooleanExtra(PomodoroService.EXTRA_RUNNING, false)
                val focus = intent.getIntExtra(PomodoroService.EXTRA_FOCUS_MIN, _focusMinutes.value)
                val short = intent.getIntExtra(PomodoroService.EXTRA_SHORT_MIN, _shortMinutes.value)
                val long = intent.getIntExtra(PomodoroService.EXTRA_LONG_MIN, _longMinutes.value)
                val auto =
                    intent.getBooleanExtra(PomodoroService.EXTRA_AUTO_NEXT, _autoStartNext.value)
                val completed = intent.getIntExtra(
                    PomodoroService.EXTRA_COMPLETED_CYCLES,
                    _completedCycles.value
                )
                val beforeLong = intent.getIntExtra(
                    PomodoroService.EXTRA_CYCLES_BEFORE_LONG,
                    _cyclesBeforeLong.value
                )

                viewModelScope.launch {
                    m?.let { _mode.emit(it) }
                    _remainingMs.emit(rem)
                    _isRunning.emit(running)
                    _focusMinutes.emit(focus)
                    _shortMinutes.emit(short)
                    _longMinutes.emit(long)
                    _autoStartNext.emit(auto)
                    _completedCycles.emit(completed)
                    _cyclesBeforeLong.emit(beforeLong)
                }
            }
        }
    }

    init {
        val filter = IntentFilter(PomodoroService.BROADCAST_ACTION_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ctx.registerReceiver(stateReceiver, filter)
        }
        PomodoroService.enqueueUpdateFromApp(ctx)
    }


    override fun onCleared() {
        super.onCleared()
        try {
            ctx.unregisterReceiver(stateReceiver)
        } catch (e: Exception) {
        }
    }

    fun startPauseToggle() {
        viewModelScope.launch {
            if (_isRunning.value) PomodoroService.enqueueAction(ctx, PomodoroService.ACTION_PAUSE)
            else PomodoroService.enqueueAction(ctx, PomodoroService.ACTION_RESUME)
        }
    }

    fun skip() {
        PomodoroService.enqueueAction(ctx, PomodoroService.ACTION_SKIP)
    }

    fun reset() {
        PomodoroService.enqueueAction(ctx, PomodoroService.ACTION_RESET)
    }

    fun changeMode(newMode: PomodoroMode) {
        viewModelScope.launch {
            _mode.emit(newMode)
            PomodoroService.enqueueSetMode(ctx, newMode)
        }
    }

    fun setAutoNext(enabled: Boolean) {
        viewModelScope.launch {
            _autoStartNext.emit(enabled)
            PomodoroService.enqueueSetAutoNext(ctx, enabled)
        }
    }

    fun setFocusMinutes(mins: Int) {
        viewModelScope.launch {
            _focusMinutes.emit(mins)
            PomodoroService.enqueueSetDurations(ctx, mins, _shortMinutes.value, _longMinutes.value)
        }
    }

    fun setShortMinutes(mins: Int) {
        viewModelScope.launch {
            _shortMinutes.emit(mins)
            PomodoroService.enqueueSetDurations(ctx, _focusMinutes.value, mins, _longMinutes.value)
        }
    }

    fun setLongMinutes(mins: Int) {
        viewModelScope.launch {
            _longMinutes.emit(mins)
            PomodoroService.enqueueSetDurations(ctx, _focusMinutes.value, _shortMinutes.value, mins)
        }
    }

    fun getUpcomingSessionInfo(): Pair<String, String> {
        val nextMode: PomodoroMode
        when (_mode.value) {
            PomodoroMode.FOCUS -> {
                nextMode = if (_completedCycles.value + 1 >= _cyclesBeforeLong.value)
                    PomodoroMode.LONG_BREAK
                else
                    PomodoroMode.SHORT_BREAK
            }

            PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                nextMode = PomodoroMode.FOCUS
            }
        }

        val nextLabel = when (nextMode) {
            PomodoroMode.FOCUS -> "Focus"
            PomodoroMode.SHORT_BREAK -> "Short Break"
            PomodoroMode.LONG_BREAK -> "Long Break"
        }

        val nextMinutes = when (nextMode) {
            PomodoroMode.FOCUS -> _focusMinutes.value
            PomodoroMode.SHORT_BREAK -> _shortMinutes.value
            PomodoroMode.LONG_BREAK -> _longMinutes.value
        }

        val formattedTime = String.format("%02d:%02d", nextMinutes, 0)
        return nextLabel to formattedTime
    }

}
