package com.pranshulgg.clockmaster.helpers

import com.pranshulgg.clockmaster.models.TimerItem
import com.pranshulgg.clockmaster.models.TimerState
import com.pranshulgg.clockmaster.roomDB.TimerEntity

fun TimerEntity.toTimerItem(): TimerItem {
    val state = when (state) {
        "Running" -> TimerState.Running
        "Paused" -> TimerState.Paused
        else -> TimerState.Finished
    }
    return TimerItem(
        id = id,
        label = label,
        initialMillis = initialMillis,
        remainingMillis = remainingMillis,
        state = state,
        createdAt = createdAt,
        originalMillis = originalMillis
    )
}

fun TimerItem.toEntity(): TimerEntity {
    return TimerEntity(
        id = id,
        label = label,
        initialMillis = initialMillis,
        remainingMillis = remainingMillis,
        state = state.name,
        createdAt = createdAt,
        originalMillis = originalMillis
    )
}
