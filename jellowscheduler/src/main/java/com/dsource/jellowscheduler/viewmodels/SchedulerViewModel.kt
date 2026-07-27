package com.dsource.jellowscheduler.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dsource.jellowscheduler.datasource.localdb.entities.ScheduleModel
import com.dsource.jellowscheduler.repositories.ISchedulerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulerViewModel @Inject constructor(private val repository: ISchedulerRepository) : ViewModel() {

    val allSchedules: Flow<List<ScheduleModel>> = repository.getAllSchedules()

    init {
        insertDummyData()
    }

    fun insertSchedule(schedule: ScheduleModel) {
        viewModelScope.launch {
            repository.insertSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: ScheduleModel) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: ScheduleModel) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun getScheduleById(id: Int): LiveData<ScheduleModel?> {
        return liveData {
            emit(repository.getScheduleById(id))
        }
    }

    fun insertDummyData() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertDummyData()
        }
    }
}