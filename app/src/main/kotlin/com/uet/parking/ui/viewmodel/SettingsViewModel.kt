package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.User
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.*

class SettingsViewModel(
    private val repository: ParkingRepository,
    val userId: String
) : ViewModel() {

    // Fetch user profile from Firestore and map it to User object
    // Using StateFlow to ensure the UI has the latest data across recompositions
    val user: StateFlow<User?> = repository.getUserWithProfile(userId)
        .map { it?.user }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun logout() {
        // Handle any logout logic if needed
    }
}
