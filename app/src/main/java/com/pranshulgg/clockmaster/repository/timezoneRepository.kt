package com.pranshulgg.clockmaster.repository

import com.pranshulgg.clockmaster.roomDB.Timezone
import com.pranshulgg.clockmaster.roomDB.TimezoneDAO
import kotlinx.coroutines.flow.Flow

class TimezoneRepository(private val dao: TimezoneDAO) {

    val allTimezones: Flow<List<Timezone>> = dao.getAllTimezones()

    suspend fun insert(timezone: Timezone) {
        dao.insert(timezone)
    }

    suspend fun delete(zoneId: String) {
        dao.delete(zoneId)
    }
}
