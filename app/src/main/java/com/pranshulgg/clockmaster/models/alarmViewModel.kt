package com.pranshulgg.clockmaster.models

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranshulgg.clockmaster.helpers.AlarmScheduler
import com.pranshulgg.clockmaster.repository.AlarmRepository
import com.pranshulgg.clockmaster.roomDB.AlarmEntity
import com.pranshulgg.clockmaster.roomDB.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: AlarmRepository
    val alarms: Flow<List<AlarmEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).alarmDao()
        repo = AlarmRepository(dao)
        alarms = repo.allAlarms
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addAlarm(context: Context, alarm: AlarmEntity) = viewModelScope.launch {
        val id = repo.insert(alarm).toInt()
        try {

            AlarmScheduler.scheduleAlarm(
                context,
                id,
                dayOfWeek = alarm.repeatDays,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                soundUri = alarm.sound,
                vibrate = alarm.vibrate,
                snoozeTime = alarm.snoozeTime
            )

        } catch (e: SecurityException) {
            Toast.makeText(context, "Unable to schedule exact alarm", Toast.LENGTH_SHORT).show()
        }
    }


    fun updateAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        repo.update(alarm)
    }

    fun removeAlarm(context: Context, alarm: AlarmEntity) = viewModelScope.launch {
        AlarmScheduler.cancelAlarm(context, alarm.id)
        repo.delete(alarm)
    }
}
