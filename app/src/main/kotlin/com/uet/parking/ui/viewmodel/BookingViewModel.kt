package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// UI State untuk Booking Form
data class BookingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val selectedParkingLot: ParkingLot? = null,
    val selectedDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val selectedStartTime: String = "07:00",
    val selectedEndTime: String = "09:00"
)

class BookingViewModel(
    private val repository: ParkingRepository,
    private val userId: Int
) : ViewModel() {

    // Available Parking Lots
    val parkingLots: StateFlow<List<ParkingLot>> = repository.getAllParkingLots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available Time Slots (mock data - có thể mở rộng từ database sau)
    val startTimeSlots: StateFlow<List<Pair<String, String>>> = MutableStateFlow(
        listOf(
            "Ca 1 — 07:00" to "07:00",
            "Ca 2 — 09:15" to "09:15",
            "Ca 3 — 12:30" to "12:30",
            "Ca 4 — 15:15" to "15:15"
        )
    ).asStateFlow()

    val endTimeSlots: StateFlow<List<Pair<String, String>>> = MutableStateFlow(
        listOf(
            "Ca 1 — 09:00" to "09:00",
            "Ca 2 — 11:15" to "11:15",
            "Ca 3 — 14:30" to "14:30",
            "Ca 4 — 17:15" to "17:15"
        )
    ).asStateFlow()

    // Booking UI State
    private val _bookingUiState = MutableStateFlow(BookingUiState())
    val bookingUiState: StateFlow<BookingUiState> = _bookingUiState.asStateFlow()

    // User's Tickets
    val userTickets: StateFlow<List<Ticket>> = repository.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Update selected parking lot
     */
    fun selectParkingLot(parkingLot: ParkingLot) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedParkingLot = parkingLot,
            errorMessage = ""
        )
    }

    /**
     * Update selected date
     */
    fun selectDate(date: String) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedDate = date,
            errorMessage = ""
        )
    }

    /**
     * Update selected start time
     */
    fun selectStartTime(time: String) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedStartTime = time,
            errorMessage = ""
        )
    }

    /**
     * Update selected end time
     */
    fun selectEndTime(time: String) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedEndTime = time,
            errorMessage = ""
        )
    }

    /**
     * Create a new booking
     */
    fun createBooking(): Boolean {
        val currentState = _bookingUiState.value

        // Validation
        if (currentState.selectedParkingLot == null) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Vui lòng chọn bãi đỗ"
            )
            return false
        }

        if (currentState.selectedDate.isEmpty()) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Vui lòng chọn ngày"
            )
            return false
        }

        if (currentState.selectedStartTime.isEmpty()) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Vui lòng chọn giờ bắt đầu"
            )
            return false
        }

        if (currentState.selectedEndTime.isEmpty()) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Vui lòng chọn giờ kết thúc"
            )
            return false
        }

        // Create datetime strings
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val startDateTime = "${currentState.selectedDate} ${currentState.selectedStartTime}"
        val endDateTime = "${currentState.selectedDate} ${currentState.selectedEndTime}"

        // Validate time range
        return try {
            val startDate = sdf.parse(startDateTime)
            val endDate = sdf.parse(endDateTime)

            if (startDate == null || endDate == null) {
                _bookingUiState.value = currentState.copy(
                    errorMessage = "Định dạng thời gian không hợp lệ"
                )
                return false
            }

            if (startDate >= endDate) {
                _bookingUiState.value = currentState.copy(
                    errorMessage = "Giờ kết thúc phải sau giờ bắt đầu"
                )
                return false
            }

            // Insert ticket into database
            _bookingUiState.value = currentState.copy(isLoading = true)
            viewModelScope.launch {
                try {
                    val ticket = Ticket(
                        userId = userId,
                        parkingId = currentState.selectedParkingLot?.parkingId,
                        startTime = startDateTime,
                        endTime = endDateTime,
                        status = TicketStatus.PENDING,
                        price = calculatePrice(startDate, endDate)
                    )
                    repository.insertTicket(ticket)
                    _bookingUiState.value = currentState.copy(
                        isLoading = false,
                        successMessage = "Đặt chỗ thành công!"
                    )
                } catch (e: Exception) {
                    _bookingUiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Lỗi: ${e.message}"
                    )
                }
            }
            true
        } catch (e: Exception) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Lỗi: ${e.message}"
            )
            false
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _bookingUiState.value = _bookingUiState.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }

    /**
     * Calculate booking price based on duration
     * Mục tiêu: sau này có thể tùy chỉnh dựa trên loại bãi đỗ, thời gian, v.v.
     */
    private fun calculatePrice(startDate: Date, endDate: Date): Double {
        val durationMs = endDate.time - startDate.time
        val durationHours = durationMs / (1000 * 60 * 60).toDouble()
        return durationHours * 10000 // 10,000 VND per hour
    }

    /**
     * Get parking lot availability for specific date
     * TODO: Implement based on HourlyLoad data
     */
    fun getParkingAvailability(parkingId: Int, date: String): StateFlow<Int?> {
        // This can be extended to check HourlyLoad from database
        return MutableStateFlow<Int?>(null).asStateFlow()
    }

    /**
     * Get user's bookings filtered by status
     */
    fun getUserBookingsByStatus(status: String): StateFlow<List<Ticket>> {
        // This would require updating repository methods
        return MutableStateFlow<List<Ticket>>(emptyList()).asStateFlow()
    }
}
