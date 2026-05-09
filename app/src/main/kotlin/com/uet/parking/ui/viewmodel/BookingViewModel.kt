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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// UI State for Booking Form
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

    // Available Time Slots (định nghĩa các ca làm việc)
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

    // User's Tickets (từ database)
    val userTickets: StateFlow<List<Ticket>> = repository.getAllTickets()
        .map { tickets -> tickets.filter { it.userId == userId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Select parking lot
     */
    fun selectParkingLot(parkingLot: ParkingLot) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedParkingLot = parkingLot,
            errorMessage = ""
        )
    }

    /**
     * Select date (format: dd/MM/yyyy)
     */
    fun selectDate(date: String) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedDate = date,
            errorMessage = ""
        )
    }

    /**
     * Select start time (format: HH:mm)
     */
    fun selectStartTime(time: String) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedStartTime = time,
            errorMessage = ""
        )
    }

    /**
     * Select end time (format: HH:mm)
     */
    fun selectEndTime(time: String) {
        _bookingUiState.value = _bookingUiState.value.copy(
            selectedEndTime = time,
            errorMessage = ""
        )
    }

    /**
     * Create new booking and save to database
     * Returns true if successful, false if there's an error
     */
    fun createBooking(onSuccess: () -> Unit = {}) {
        val currentState = _bookingUiState.value

        // Validation
        val validationError = validateBooking(currentState)
        if (validationError.isNotEmpty()) {
            _bookingUiState.value = currentState.copy(errorMessage = validationError)
            return
        }

        // Create datetime strings
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val startDateTime = "${currentState.selectedDate} ${currentState.selectedStartTime}"
        val endDateTime = "${currentState.selectedDate} ${currentState.selectedEndTime}"

        // Parse dates
        val startDate = try {
            sdf.parse(startDateTime)
        } catch (e: Exception) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Lỗi định dạng thời gian: ${e.message}"
            )
            return
        }

        val endDate = try {
            sdf.parse(endDateTime)
        } catch (e: Exception) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Lỗi định dạng thời gian: ${e.message}"
            )
            return
        }

        if (startDate == null || endDate == null) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Không thể phân tích thời gian"
            )
            return
        }

        // Validate time range
        if (startDate >= endDate) {
            _bookingUiState.value = currentState.copy(
                errorMessage = "Giờ kết thúc phải sau giờ bắt đầu"
            )
            return
        }

        // Set loading state
        _bookingUiState.value = currentState.copy(isLoading = true, errorMessage = "")

        // Insert ticket into database
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

                // Save to database
                repository.insertTicket(ticket)

                // Update UI state on success
                _bookingUiState.value = currentState.copy(
                    isLoading = false,
                    successMessage = "Đặt chỗ thành công!",
                    errorMessage = ""
                )

                // Call success callback
                onSuccess()

            } catch (e: Exception) {
                _bookingUiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Lỗi: ${e.message ?: "Không xác định được lỗi"}"
                )
            }
        }
    }

    /**
     * Validate booking information
     */
    private fun validateBooking(state: BookingUiState): String {
        return when {
            state.selectedParkingLot == null -> "Vui lòng chọn bãi đỗ"
            state.selectedDate.isEmpty() -> "Vui lòng chọn ngày"
            state.selectedStartTime.isEmpty() -> "Vui lòng chọn giờ bắt đầu"
            state.selectedEndTime.isEmpty() -> "Vui lòng chọn giờ kết thúc"
            else -> ""
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
     * Reset booking form
     */
    fun resetBookingForm() {
        _bookingUiState.value = BookingUiState(
            selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        )
    }

    /**
     * Calculate booking price based on duration
     * Current: 10,000 VND per hour
     * Can be extended based on parking lot type, time, etc.
     */
    private fun calculatePrice(startDate: Date, endDate: Date): Double {
        val durationMs = endDate.time - startDate.time
        val durationHours = durationMs / (1000 * 60 * 60).toDouble()
        return durationHours * 10000 // 10,000 VND per hour
    }

    /**
     * Check if time slot overlaps with existing bookings
     */
    fun hasTimeConflict(date: String, startTime: String, endTime: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val newStart = try {
            sdf.parse("$date $startTime")
        } catch (e: Exception) {
            return false
        }
        val newEnd = try {
            sdf.parse("$date $endTime")
        } catch (e: Exception) {
            return false
        }

        if (newStart == null || newEnd == null) return false

        return userTickets.value.any { ticket ->
            val ticketStart = try {
                sdf.parse(ticket.startTime ?: "")
            } catch (e: Exception) {
                null
            }
            val ticketEnd = try {
                sdf.parse(ticket.endTime ?: "")
            } catch (e: Exception) {
                null
            }

            if (ticketStart != null && ticketEnd != null) {
                newStart.before(ticketEnd) && newEnd.after(ticketStart)
            } else false
        }
    }

    /**
     * Get user's bookings for specific date
     */
    fun getBookingsForDate(date: String): List<Ticket> {
        return userTickets.value.filter { ticket ->
            ticket.startTime?.startsWith(date) == true
        }
    }

    /**
     * Get available parking lots with capacity
     */
    fun getAvailableParkingLots(): List<ParkingLot> {
        return parkingLots.value.filter { (it.capacity ?: 0) > (it.current ?: 0) }
    }

    /**
     * Delete a ticket by ID
     */
    fun deleteTicket(ticket: Ticket) {
        viewModelScope.launch {
            try {
                repository.deleteTicket(ticket)
                _bookingUiState.value = _bookingUiState.value.copy(
                    successMessage = "Xóa vé thành công!"
                )
            } catch (e: Exception) {
                _bookingUiState.value = _bookingUiState.value.copy(
                    errorMessage = "Lỗi khi xóa vé: ${e.message}"
                )
            }
        }
    }
}
