package com.pranshulgg.clockmaster.roomDB

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime
import androidx.room.TypeConverter

@Entity(tableName = "alarm_table")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val repeatDays: List<Int> = emptyList(),
    val label: String = "",
    val enabled: Boolean = true,
    val sound: String? = null,
    val vibrate: Boolean = false,
    val snoozeTime: Int? = null

)


class AlarmConverters {

    @TypeConverter
    fun fromRepeatDays(days: List<Int>): String = days.joinToString(",")

    @TypeConverter
    fun toRepeatDays(data: String): List<Int> =
        if (data.isEmpty()) emptyList() else data.split(",").map { it.toInt() }

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalTime(data: String): LocalTime = LocalTime.parse(data)
}
