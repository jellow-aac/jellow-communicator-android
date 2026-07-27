package com.dsource.jellowscheduler.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dsource.jellowscheduler.repositories.ISchedulerRepository

class SchedulerViewModelFactory (private val repository: ISchedulerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchedulerViewModel::class.java)) {
            return SchedulerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}