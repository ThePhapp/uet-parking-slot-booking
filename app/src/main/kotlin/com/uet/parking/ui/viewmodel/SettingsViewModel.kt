package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.uet.parking.data.model.User
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.Flow

class SettingsViewModel(
    private val repository: ParkingRepository,
    val userId: Int
) : ViewModel() {

    val user: Flow<User?> = repository.getUserById(userId)

    fun logout() {
        // Handle any logout logic if needed (e.g., clearing preferences)
    }
}
