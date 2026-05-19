package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StudyScheduleViewModelFactory(
    private val userId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyScheduleViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
