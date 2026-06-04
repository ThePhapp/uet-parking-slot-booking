package com.uet.parking.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.StudySchedule
import com.uet.parking.data.repository.StudyScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
class StudyScheduleViewModel(
    private val userId: String
) : ViewModel() {

    private val repository = StudyScheduleRepository()

    private val _schedules = MutableStateFlow<List<StudySchedule>>(emptyList())
    val schedules: StateFlow<List<StudySchedule>> = _schedules

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Data will be automatically updated via Flow from repository
            delay(1000)
            _isRefreshing.value = false
        }
    }

    init {
        observeSchedules()
    }

    private fun observeSchedules() {
        viewModelScope.launch {
            repository.getSchedulesByUser(userId).collect {
                _schedules.value = it
            }
        }
    }

    fun addSchedule(schedule: StudySchedule) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                repository.addSchedule(schedule)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            repository.deleteSchedule(scheduleId)
        }
    }
}
