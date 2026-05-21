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

    // Lấy vé của người dùng hiện tại
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

    fun createBooking(context: android.content.Context, onSuccess: () -> Unit = {}) {
        val currentState = _bookingUiState.value
        val fullSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        val newStartStr = "${currentState.selectedDate} ${currentState.selectedStartTime}"
        val newEndStr = "${currentState.selectedDate} ${currentState.selectedEndTime}"

        val newStart = fullSdf.parse(newStartStr)
        val newEnd = fullSdf.parse(newEndStr)

        if (newStart == null || newEnd == null || !newStart.before(newEnd)) {
            _bookingUiState.update { it.copy(errorMessage = "Giờ bắt đầu phải sớm hơn giờ kết thúc") }
            return
        }

        val now = Date()
        val diffHours = (newStart.time - now.time) / (1000 * 60 * 60.0)
        if (diffHours < 1.0) {
            _bookingUiState.update { it.copy(errorMessage = "Thời gian bắt đầu gửi xe phải cách thời điểm đặt hiện tại ít nhất 1 tiếng") }
            return
        }

        _bookingUiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                // 1. Kiểm tra trùng lịch
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

                // 2. Tìm bãi đỗ xe phù hợp
                val shift = when(currentState.selectedStartTime) {
                    "07:00" -> 1
                    "09:15" -> 2
                    "12:30" -> 3
                    "15:15" -> 4
                    else -> 1
                }

                val allLots = parkingLots.value.ifEmpty { repository.getAllParkingLots().first() }
                val sortedLots = allLots.sortedBy { it.parkingId }
                var selectedLotId: String? = null

                for (lot in sortedLots) {
                    val capacity = lot.capacity ?: 0
                    if (capacity == 0) continue
                    val parkingId = lot.parkingId ?: continue

                    // Ràng buộc sức chứa bãi (90%)
                    val currentLoad = repository.getLoad(parkingId, currentState.selectedDate, shift)
                    val vehicleCount = currentLoad?.vehicleCount ?: 0
                    if (vehicleCount + 1 > capacity * 0.9) continue

                    // Ràng buộc lưu lượng chuyển ca tại giờ bắt đầu
                    val (startIncoming, startOutgoing) = repository.getShiftFlowLoad(parkingId, newStartStr)
                    if ((startIncoming + 1) + startOutgoing > capacity * 0.5) continue

                    // Ràng buộc lưu lượng chuyển ca tại giờ kết thúc
                    val (endIncoming, endOutgoing) = repository.getShiftFlowLoad(parkingId, newEndStr)
                    if (endIncoming + (endOutgoing + 1) > capacity * 0.5) continue

                    selectedLotId = parkingId
                    break
                }

                if (selectedLotId == null) {
                    _bookingUiState.update { it.copy(isLoading = false, errorMessage = "Không còn bãi phù hợp trong khung giờ này") }
                    return@launch
                }

                // 3. Tạo vé mới
                val ticketPrice = 10000.0
                val ticket = Ticket(
                    userId = userId,
                    parkingId = selectedLotId,
                    startTime = newStartStr,
                    endTime = newEndStr,
                    status = TicketStatus.PENDING,
                    price = ticketPrice
                )
                val newTicketId = repository.createTicket(ticket)
                
                val currentLoad = repository.getLoad(selectedLotId, currentState.selectedDate, shift)
                if (currentLoad == null) {
                    repository.updateHourlyLoad(HourlyLoad(null, selectedLotId, currentState.selectedDate, shift, 1))
                } else {
                    repository.incrementVehicleCount(selectedLotId, currentState.selectedDate, shift)
                }

                val fullTicket = ticket.copy(ticketId = newTicketId)
                com.uet.parking.utils.NotificationScheduler.schedulePreBookingNotification(context, fullTicket)
                com.uet.parking.utils.NotificationScheduler.schedulePostBookingNotification(context, fullTicket)

                _bookingUiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _bookingUiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: ${e.message}") }
            }
        }
    }

    fun deleteTicket(context: android.content.Context, ticketId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTicket(ticketId)
                com.uet.parking.utils.NotificationHelper.showTicketCancelled(context, ticketId)
                com.uet.parking.utils.NotificationScheduler.cancelNotifications(context, ticketId)
            } catch (e: Exception) {
                _bookingUiState.update { it.copy(errorMessage = "Lỗi xóa vé: ${e.message}") }
            }
        }
    }
}
