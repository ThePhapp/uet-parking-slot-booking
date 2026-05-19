package com.uet.parking.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.StudySchedule
import com.uet.parking.data.repository.StudyScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudyScheduleViewModel(
    private val userId: String
) : ViewModel() {

    private val repository = StudyScheduleRepository()

    private val _schedules = MutableStateFlow<List<StudySchedule>>(emptyList())
    val schedules: StateFlow<List<StudySchedule>> = _schedules

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

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
}
