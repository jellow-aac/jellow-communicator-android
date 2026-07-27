package com.dsource.jellowscheduler.repositories

import com.dsource.jellowscheduler.datasource.localdb.entities.ScheduleModel
import kotlinx.coroutines.flow.Flow

interface ISchedulerRepository {
    suspend fun insertSchedule(schedule: ScheduleModel)

    suspend fun updateSchedule(schedule: ScheduleModel)

    suspend fun deleteSchedule(schedule: ScheduleModel)

    fun getAllSchedules(): Flow<List<ScheduleModel>>

    suspend fun getScheduleById(id: Int): ScheduleModel?

    suspend fun insertDummyData()
}