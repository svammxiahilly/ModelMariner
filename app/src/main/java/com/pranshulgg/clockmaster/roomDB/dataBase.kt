package com.pranshulgg.clockmaster.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [Timezone::class, AlarmEntity::class, TimerEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(ZoneIdConverter::class, AlarmConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timezoneDao(): TimezoneDAO
    abstract fun alarmDao(): AlarmDAO
    abstract fun timerDao(): TimerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clockmaster_timezone_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}