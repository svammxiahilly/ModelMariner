package com.pranshulgg.clockmaster.models

import java.time.LocalTime

data class Alarm(
    val id: Int = 0,
    val time: LocalTime,
    val repeatDays: List<Int> = emptyList(),
    val label: String = "",
    val enabled: Boolean = true,
    val vibrate: Boolean = false,
)
