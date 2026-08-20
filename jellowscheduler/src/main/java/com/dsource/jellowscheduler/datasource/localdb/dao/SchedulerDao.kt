package com.dsource.jellowscheduler.datasource.localdb.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dsource.jellowscheduler.datasource.localdb.entities.ScheduleModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SchedulerDao {
    @Insert
    suspend fun insert(schedule: ScheduleModel)

    @Update
    suspend fun update(schedule: ScheduleModel)

    @Delete
    suspend fun delete(schedule: ScheduleModel)

    @Query("SELECT * FROM ScheduleModel")
    fun getAllSchedules(): Flow<List<ScheduleModel>>

    @Query("SELECT * FROM ScheduleModel WHERE id = :id")
    suspend fun getScheduleById(id: Int): ScheduleModel?
}