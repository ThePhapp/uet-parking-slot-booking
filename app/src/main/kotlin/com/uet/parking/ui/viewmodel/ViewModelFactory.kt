package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uet.parking.data.repository.ParkingRepository

class ViewModelFactory(
    private val repository: ParkingRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AdminViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(AdminBookingViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AdminBookingViewModel(repository) as T
            }
            modelClass.isAssignableFrom(BookingViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                BookingViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SettingsViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PaymentViewModel(repository, userId) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class ParkingLotDetailViewModelFactory(
    private val repository: ParkingRepository,
    private val lotId: Int,
    private val adminId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingLotDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParkingLotDetailViewModel(repository, lotId, adminId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}