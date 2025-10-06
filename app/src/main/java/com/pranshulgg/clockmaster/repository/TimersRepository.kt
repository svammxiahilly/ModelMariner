package com.pranshulgg.clockmaster.repository

import com.pranshulgg.clockmaster.roomDB.TimerDao
import com.pranshulgg.clockmaster.roomDB.TimerEntity
import kotlinx.coroutines.flow.Flow

class TimersRepository(private val dao: TimerDao) {
    fun getAllTimers(): Flow<List<TimerEntity>> = dao.getAllTimers()

    suspend fun insert(entity: TimerEntity) = dao.insert(entity)
    suspend fun update(entity: TimerEntity) = dao.update(entity)
    suspend fun delete(entity: TimerEntity) = dao.delete(entity)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun get(id: String) = dao.getTimer(id)
}
