package com.pranshulgg.clockmaster.roomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

@Dao
interface TimezoneDAO {
    @Query("SELECT * FROM clockmaster_timezone_table ORDER BY zoneId ASC")
    fun getAllTimezones(): Flow<List<Timezone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timezone: Timezone)

    @Query("DELETE FROM clockmaster_timezone_table WHERE zoneId = :zoneId")
    suspend fun delete(zoneId: String)
}
