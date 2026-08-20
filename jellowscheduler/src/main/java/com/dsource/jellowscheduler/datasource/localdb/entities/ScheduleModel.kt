package com.dsource.jellowscheduler.datasource.localdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ScheduleModel")
data class ScheduleModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val iconUrl: String,
    val activityName: String,
    val startTime: Long,
    val endTime: Long,
    val setReminder: Boolean
)