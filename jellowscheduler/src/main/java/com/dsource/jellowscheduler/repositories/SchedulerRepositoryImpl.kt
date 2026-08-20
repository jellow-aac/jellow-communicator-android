package com.dsource.jellowscheduler.repositories

import com.dsource.jellowscheduler.datasource.localdb.dao.SchedulerDao
import com.dsource.jellowscheduler.datasource.localdb.entities.ScheduleModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SchedulerRepositoryImpl @Inject constructor(private val schedulerDao: SchedulerDao) : ISchedulerRepository {

    override suspend fun insertSchedule(schedule: ScheduleModel) {
        schedulerDao.insert(schedule)
    }

    override suspend fun updateSchedule(schedule: ScheduleModel) {
        schedulerDao.update(schedule)
    }

    override suspend fun deleteSchedule(schedule: ScheduleModel) {
        schedulerDao.delete(schedule)
    }

    override fun getAllSchedules(): Flow<List<ScheduleModel>> {
        return schedulerDao.getAllSchedules()
    }

    override suspend fun getScheduleById(id: Int): ScheduleModel? {
        return schedulerDao.getScheduleById(id)
    }

    override suspend fun insertDummyData() {
        val dummySchedules = listOf(
            DummyScheduleData("icon_url_1", "Meeting with John", 1278987600000, 1278991200000, true), //Example data
            DummyScheduleData("icon_url_2", "Meeting with John1", 1378987600000, 1378991200000, true), //Example data
            DummyScheduleData("icon_url_3", "Meeting with John2", 1478987600000, 1478991200000, true), //Example data
            DummyScheduleData("icon_url_4", "Meeting with John3", 1578987600000, 1578991200000, true), //Example data
            DummyScheduleData("icon_url_15", "Meeting with John4", 1678984300000, 1678991200000, true), //Example data
            // ... add more dummy data here ...
        )

        dummySchedules.forEach { dummy ->
            schedulerDao.insert(
                ScheduleModel(
                    iconUrl = dummy.iconUrl,
                    activityName = dummy.activityName,
                    startTime = dummy.startTime,
                    endTime = dummy.endTime,
                    setReminder = dummy.setReminder
                )
            )
        }
    }

    data class DummyScheduleData(
        val iconUrl: String,
        val activityName: String,
        val startTime: Long,
        val endTime: Long,
        val setReminder: Boolean
    )
}