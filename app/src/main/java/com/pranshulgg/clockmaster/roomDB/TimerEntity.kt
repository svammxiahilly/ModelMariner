package com.pranshulgg.clockmaster.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey val id: String,
    val label: String,
    val initialMillis: Long,
    val remainingMillis: Long,
    val state: String,
    val createdAt: Long = System.currentTimeMillis(),
    val originalMillis: Long
)