package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.BookingEntity
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.BookingStatus
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminBookingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class AdminBookingViewModel(private val repository: ParkingRepository) : ViewModel() {

    val allTickets: StateFlow<List<Ticket>> = repository.getAllTickets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Tất cả bookings
    val allBookings: StateFlow<List<BookingEntity>> = try {
        repository.getAllBookings()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } catch (e: Exception) {
        MutableStateFlow(emptyList<BookingEntity>()).asStateFlow()
    }

    // Bookings đang chờ duyệt
    val pendingBookings: StateFlow<List<BookingEntity>> = try {
        repository.getPendingBookings()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } catch (e: Exception) {
        MutableStateFlow(emptyList<BookingEntity>()).asStateFlow()
    }

    private val _uiState = MutableStateFlow(AdminBookingUiState())
    val uiState: StateFlow<AdminBookingUiState> = _uiState.asStateFlow()

    /**
     * Admin duyệt booking -> chuyển sang APPROVED
     */
    fun approveBooking(bookingId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.updateBookingStatus(bookingId, BookingStatus.APPROVED.value)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Đã duyệt booking #$bookingId thành công!",
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi khi duyệt: ${e.message}"
                )
            }
        }
    }

    /**
     * Admin từ chối booking -> chuyển sang REJECTED
     */
    fun rejectBooking(bookingId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.updateBookingStatus(bookingId, BookingStatus.REJECTED.value)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Đã từ chối booking #$bookingId",
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi khi từ chối: ${e.message}"
                )
            }
        }
    }

    /**
     * Admin xóa booking
     */
    fun deleteBooking(bookingId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteBooking(bookingId)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Đã xóa booking #$bookingId"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Lỗi khi xóa: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }
}
