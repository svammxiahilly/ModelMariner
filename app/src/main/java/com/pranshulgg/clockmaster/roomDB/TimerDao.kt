package com.pranshulgg.clockmaster.roomDB

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers ORDER BY createdAt ASC")
    fun getAllTimers(): Flow<List<TimerEntity>>


    @Query("SELECT * FROM timers WHERE id = :id LIMIT 1")
    suspend fun getTimer(id: String): TimerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: TimerEntity)

    @Update
    suspend fun update(timer: TimerEntity)

    @Delete
    suspend fun delete(timer: TimerEntity)

    @Query("DELETE FROM timers WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM timers")
    suspend fun deleteAll()
}
