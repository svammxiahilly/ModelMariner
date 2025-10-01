package com.pranshulgg.clockmaster.repository

import com.pranshulgg.clockmaster.roomDB.AlarmDAO
import com.pranshulgg.clockmaster.roomDB.AlarmEntity
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val alarmDao: AlarmDAO) {
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    suspend fun insert(alarm: AlarmEntity): Long = alarmDao.insert(alarm)
    suspend fun update(alarm: AlarmEntity) = alarmDao.update(alarm)
    suspend fun delete(alarm: AlarmEntity) = alarmDao.delete(alarm)
}
