package com.dsource.jellowscheduler.datasource.localdb.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dsource.jellowscheduler.datasource.localdb.dao.SchedulerDao
import com.dsource.jellowscheduler.datasource.localdb.entities.ScheduleModel

@Database(entities = [ScheduleModel::class], version = 1)
abstract class SchedulerDb : RoomDatabase() {
    abstract fun scheduleDao(): SchedulerDao

    companion object {
        @Volatile
        private var INSTANCE: SchedulerDb? = null

        fun getDatabase(context: Context): SchedulerDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchedulerDb::class.java,
                    "scheduler_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}