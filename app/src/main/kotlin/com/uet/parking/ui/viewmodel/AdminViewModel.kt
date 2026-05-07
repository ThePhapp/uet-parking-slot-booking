package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.AdminWithProfile
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AdminViewModel(
    private val repository: ParkingRepository,
    private val userId: Int
) : ViewModel() {

    val adminProfile: StateFlow<AdminWithProfile?> = repository.getAdminWithProfile(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val parkingLots: StateFlow<List<ParkingLot>> = repository.getAllParkingLots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSlots: StateFlow<Int> = parkingLots.map { lots ->
        lots.sumOf { it.capacity ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val availableSlots: StateFlow<Int> = parkingLots.map { lots ->
        val total = lots.sumOf { it.capacity ?: 0 }
        val occupied = lots.sumOf { it.current ?: 0 }
        total - occupied
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
