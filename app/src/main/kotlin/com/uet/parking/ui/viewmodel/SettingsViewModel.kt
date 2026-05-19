package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.User
import com.uet.parking.data.model.UserInfo
import com.uet.parking.data.model.UserWithProfile
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ParkingRepository,
    val userId: String
) : ViewModel() {

    // Fetch user profile from Firestore and map it to User object
    // Using StateFlow to ensure the UI has the latest data across recompositions
    val userProfile: StateFlow<UserWithProfile?> = repository.getUserWithProfile(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateProfile(info: UserInfo, newName: String? = null) {
        viewModelScope.launch {
            repository.createUserInfo(info)
            if (newName != null) {
                repository.updateUserName(userId, newName)
            }
        }
    }

    fun logout() {
        // Handle any logout logic if needed
    }
}
