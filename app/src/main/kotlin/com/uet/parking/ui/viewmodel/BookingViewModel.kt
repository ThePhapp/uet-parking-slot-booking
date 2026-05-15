package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.HourlyLoad
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    private val userId: String
) : ViewModel() {

    val parkingLots: StateFlow<List<ParkingLot>> = repository.getAllParkingLots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val startTimeSlots = flowOf(
        listOf(
            "Ca 1 — 07:00" to "07:00",
            "Ca 2 — 09:15" to "09:15",
            "Ca 3 — 12:30" to "12:30",
            "Ca 4 — 15:15" to "15:15"
        )
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val endTimeSlots = flowOf(
        listOf(
            "Ca 1 — 09:00" to "09:00",
            "Ca 2 — 11:15" to "11:15",
            "Ca 3 — 14:30" to "14:30",
            "Ca 4 — 17:15" to "17:15"
        )
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _bookingUiState = MutableStateFlow(BookingUiState())
    val bookingUiState: StateFlow<BookingUiState> = _bookingUiState.asStateFlow()

    val userTickets: StateFlow<List<Ticket>> = repository.getAllTickets()
        .map { tickets -> tickets.filter { it.userId == userId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: String) {
        _bookingUiState.update { it.copy(selectedDate = date, errorMessage = "") }
    }

    fun selectStartTime(time: String) {
        _bookingUiState.update { it.copy(selectedStartTime = time, errorMessage = "") }
    }

    fun selectEndTime(time: String) {
        _bookingUiState.update { it.copy(selectedEndTime = time, errorMessage = "") }
    }

    fun createBooking(onSuccess: () -> Unit = {}) {
        val currentState = _bookingUiState.value
        val fullSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        val newStart = fullSdf.parse("${currentState.selectedDate} ${currentState.selectedStartTime}")
        val newEnd = fullSdf.parse("${currentState.selectedDate} ${currentState.selectedEndTime}")

        if (newStart == null || newEnd == null || !newStart.before(newEnd)) {
            _bookingUiState.update { it.copy(errorMessage = "Giờ bắt đầu phải sớm hơn giờ kết thúc") }
            return
        }

        _bookingUiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                val existingTickets = repository.getTicketsByUserIdOnce(userId)
                val isOverlapping = existingTickets.any { ticket ->
                    val ticketStart = fullSdf.parse(ticket.startTime ?: "")
                    val ticketEnd = fullSdf.parse(ticket.endTime ?: "")
                    if (ticketStart != null && ticketEnd != null) {
                        newStart.before(ticketEnd) && newEnd.after(ticketStart)
                    } else false
                }

                if (isOverlapping) {
                    _bookingUiState.update { it.copy(isLoading = false, errorMessage = "Thời gian này trùng với vé hiện có") }
                    return@launch
                }

                val ticketPrice = 10000.0
                val ticket = Ticket(
                    userId = userId,
                    parkingId = "1",
                    startTime = "${currentState.selectedDate} ${currentState.selectedStartTime}",
                    endTime = "${currentState.selectedDate} ${currentState.selectedEndTime}",
                    status = TicketStatus.PENDING,
                    price = ticketPrice
                )
                repository.createTicket(ticket)

                val userInfo = repository.getUserInfoByIdOnce(userId)
                val currentDebt = userInfo?.debt ?: 0.0
                repository.updateDebt(userId, currentDebt + ticketPrice)

                val shift = when(currentState.selectedStartTime) {
                    "07:00" -> 1
                    "09:15" -> 2
                    "12:30" -> 3
                    "15:15" -> 4
                    else -> 1
                }
                
                val currentLoad = repository.getLoad("1", currentState.selectedDate, shift)
                if (currentLoad == null) {
                    repository.updateHourlyLoad(HourlyLoad("1", "1", currentState.selectedDate, shift, 1))
                } else {
                    repository.incrementVehicleCount("1", currentState.selectedDate, shift)
                }

                _bookingUiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _bookingUiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: ${e.message}") }
            }
        }
    }

    fun deleteTicket(ticketId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTicket(ticketId)
            } catch (e: Exception) {
                _bookingUiState.update { it.copy(errorMessage = "Lỗi xóa vé: ${e.message}") }
            }
        }
    }
}
