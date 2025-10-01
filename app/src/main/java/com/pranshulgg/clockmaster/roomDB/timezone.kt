package com.pranshulgg.clockmaster.roomDB

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.ZoneId

@Entity(tableName = "clockmaster_timezone_table")
data class Timezone(
    @PrimaryKey val zoneId: String,
    val displayName: String,
    val offset: String,
    val zone: ZoneId
)

class ZoneIdConverter {

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromZoneId(zoneId: ZoneId?): String? {
        return zoneId?.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toZoneId(zoneId: String?): ZoneId? {
        return zoneId?.let { ZoneId.of(it) }
    }
}


