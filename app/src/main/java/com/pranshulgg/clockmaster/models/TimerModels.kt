package com.pranshulgg.clockmaster.models

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import java.util.UUID

enum class TimerState { Running, Paused, Finished }

data class TimerItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Timer",
    val initialMillis: Long,
    val remainingMillis: Long,
    val state: TimerState = TimerState.Paused,
    val createdAt: Long = System.currentTimeMillis(),
    val originalMillis: Long
) {
    fun copyRunning(newRemaining: Long) =
        copy(remainingMillis = newRemaining, state = TimerState.Running)

    fun copyPaused(newRemaining: Long) =
        copy(remainingMillis = newRemaining, state = TimerState.Paused)

    fun copyFinished() = copy(remainingMillis = 0L, state = TimerState.Finished)
}