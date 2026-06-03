package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uet.parking.data.repository.ParkingRepository

class ViewModelFactory(
    private val repository: ParkingRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AdminViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(BookingViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                BookingViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PaymentViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SettingsViewModel(repository, userId) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                NotificationViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

class ParkingLotDetailViewModelFactory(
    private val repository: ParkingRepository,
    private val slotRepository: com.uet.parking.data.repository.SlotRepository,
    private val lotId: String,
    private val adminId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingLotDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParkingLotDetailViewModel(repository, slotRepository, lotId, adminId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
