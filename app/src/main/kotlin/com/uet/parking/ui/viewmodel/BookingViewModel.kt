package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.HourlyLoad
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.data.repository.StudyScheduleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AutoBookingResult(
    val successCount: Int = 0,
    val failCount: Int = 0,
    val results: List<TicketResult> = emptyList()
)

data class TicketResult(
    val scheduleName: String,
    val time: String,
    val success: Boolean,
    val message: String
)

data class BookingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val selectedParkingLot: ParkingLot? = null,
    val selectedDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val selectedStartTime: String = getClosestShift(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())).first,
    val selectedEndTime: String = getClosestShift(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())).second,
    val autoBookingResult: AutoBookingResult? = null
) {
    companion object {
        fun getClosestShift(dateString: String? = null): Pair<String, String> {
            val now = Calendar.getInstance()
            val currentTimeInMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            
            val shifts = listOf(
                Pair("07:00", "09:40"),
                Pair("09:50", "12:30"),
                Pair("13:30", "16:10"),
                Pair("16:20", "19:00")
            )
            
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayStr = sdf.format(Date())
            val isToday = dateString == null || dateString == todayStr

            if (isToday) {
                val availableShifts = shifts.filter { shift ->
                    val parts = shift.first.split(":")
                    val shiftTimeInMinutes = parts[0].toInt() * 60 + parts[1].toInt()
                    (shiftTimeInMinutes - currentTimeInMinutes) >= 60
                }
                if (availableShifts.isEmpty()) {
                    return Pair("", "")
                }
                return availableShifts.minByOrNull { shift ->
                    val parts = shift.first.split(":")
                    val shiftTimeInMinutes = parts[0].toInt() * 60 + parts[1].toInt()
                    Math.abs(shiftTimeInMinutes - currentTimeInMinutes)
                } ?: availableShifts[0]
            }
            
            return shifts.minByOrNull { shift ->
                val parts = shift.first.split(":")
                val shiftTimeInMinutes = parts[0].toInt() * 60 + parts[1].toInt()
                Math.abs(shiftTimeInMinutes - currentTimeInMinutes)
            } ?: shifts[0]
        }
    }
}

class BookingViewModel(
    private val repository: ParkingRepository,
    private val userId: String
) : ViewModel() {

    private val studyScheduleRepository = StudyScheduleRepository()

    val parkingLots: StateFlow<List<ParkingLot>> = repository.getAllParkingLots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val startTimeSlots = flowOf(
        listOf(
            "Ca 1 — 07:00" to "07:00",
            "Ca 2 — 09:50" to "09:50",
            "Ca 3 — 13:30" to "13:30",
            "Ca 4 — 16:20" to "16:20"
        )
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val endTimeSlots = flowOf(
        listOf(
            "Ca 1 — 09:40" to "09:40",
            "Ca 2 — 12:30" to "12:30",
            "Ca 3 — 16:10" to "16:10",
            "Ca 4 — 19:00" to "19:00"
        )
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _bookingUiState = MutableStateFlow(BookingUiState())
    val bookingUiState: StateFlow<BookingUiState> = _bookingUiState.asStateFlow()

    // Lấy vé của người dùng hiện tại
    val userTickets: StateFlow<List<Ticket>> = repository.getAllTickets()
        .map { tickets -> tickets.filter { it.userId == userId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: String) {
        val closest = BookingUiState.getClosestShift(date)
        _bookingUiState.update { 
            it.copy(
                selectedDate = date, 
                selectedStartTime = closest.first,
                selectedEndTime = closest.second,
                errorMessage = ""
            ) 
        }
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
        
        if (currentState.selectedStartTime.isEmpty() || currentState.selectedEndTime.isEmpty()) {
            _bookingUiState.update { it.copy(errorMessage = "Vui lòng chọn giờ bắt đầu và giờ kết thúc") }
            return
        }

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
                com.uet.parking.utils.NotificationHelper.showTicketCancelled(context, ticketId, userId)
                com.uet.parking.utils.NotificationScheduler.cancelNotifications(context, ticketId)
            } catch (e: Exception) {
                _bookingUiState.update { it.copy(errorMessage = "Lỗi xóa vé: ${e.message}") }
            }
        }
    }

    fun clearAutoBookingResult() {
        _bookingUiState.update { it.copy(autoBookingResult = null) }
    }

    fun bookBySchedule(context: android.content.Context) {
        _bookingUiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                val schedules = studyScheduleRepository.getSchedulesByUser(userId).first()

                val now = Calendar.getInstance()
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                now.set(Calendar.MILLISECOND, 0)

                val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fullSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val currentTime = Date()

                val results = mutableListOf<TicketResult>()
                var successCount = 0
                var failCount = 0

                val existingTickets = repository.getTicketsByUserIdOnce(userId)
                val allLots = parkingLots.value.ifEmpty { repository.getAllParkingLots().first() }
                val sortedLots = allLots.sortedBy { it.parkingId }

                for (schedule in schedules) {
                    val scheduleDayOfWeek = schedule.dayOfWeek
                    val targetAndroidDay = if (scheduleDayOfWeek == 8) Calendar.SUNDAY else scheduleDayOfWeek

                    val scheduleDateCal = now.clone() as Calendar
                    var daysDiff = targetAndroidDay - currentDayOfWeek
                    if (daysDiff < 0) {
                        // Already passed this week, skip
                        continue
                    }
                    scheduleDateCal.add(Calendar.DAY_OF_YEAR, daysDiff)
                    
                    val scheduleDateStr = sdf.format(scheduleDateCal.time)
                    val shift = schedule.startHour
                    
                    val startParkingTime = when (shift) {
                        1 -> "06:45"
                        2 -> "09:35"
                        3 -> "13:15"
                        4 -> "16:05"
                        else -> "06:45"
                    }
                    val endParkingTime = when (shift) {
                        1 -> "09:55"
                        2 -> "12:45"
                        3 -> "16:25"
                        4 -> "19:15"
                        else -> "09:55"
                    }

                    val newStartStr = "$scheduleDateStr $startParkingTime"
                    val newEndStr = "$scheduleDateStr $endParkingTime"
                    val newStart = fullSdf.parse(newStartStr)
                    val newEnd = fullSdf.parse(newEndStr)
                    
                    if (newStart == null || newEnd == null) continue

                    val diffHours = (newStart.time - currentTime.time) / (1000 * 60 * 60.0)
                    if (diffHours < 1.0) continue

                    val dayName = if (scheduleDayOfWeek == 8) "CN" else "Thứ $scheduleDayOfWeek"
                    val label = "${schedule.subjectName} ($dayName, Ca $shift)"

                    val isOverlapping = existingTickets.any { ticket ->
                        val ticketStart = fullSdf.parse(ticket.startTime ?: "")
                        val ticketEnd = fullSdf.parse(ticket.endTime ?: "")
                        if (ticketStart != null && ticketEnd != null) {
                            newStart.before(ticketEnd) && newEnd.after(ticketStart)
                        } else false
                    }

                    if (isOverlapping) {
                        failCount++
                        results.add(TicketResult(label, "$startParkingTime - $endParkingTime", false, "Trùng thời gian với vé đã có"))
                        continue
                    }

                    var selectedLotId: String? = null
                    for (lot in sortedLots) {
                        val capacity = lot.capacity ?: 0
                        if (capacity == 0) continue
                        val parkingId = lot.parkingId ?: continue

                        val currentLoad = repository.getLoad(parkingId, scheduleDateStr, shift)
                        val vehicleCount = currentLoad?.vehicleCount ?: 0
                        if (vehicleCount + 1 > capacity * 0.9) continue

                        val (startIncoming, startOutgoing) = repository.getShiftFlowLoad(parkingId, newStartStr)
                        if ((startIncoming + 1) + startOutgoing > capacity * 0.5) continue

                        val (endIncoming, endOutgoing) = repository.getShiftFlowLoad(parkingId, newEndStr)
                        if (endIncoming + (endOutgoing + 1) > capacity * 0.5) continue

                        selectedLotId = parkingId
                        break
                    }

                    if (selectedLotId == null) {
                        failCount++
                        results.add(TicketResult(label, "$startParkingTime - $endParkingTime", false, "Hết bãi đỗ xe phù hợp"))
                        continue
                    }

                    val ticket = Ticket(
                        userId = userId,
                        parkingId = selectedLotId,
                        startTime = newStartStr,
                        endTime = newEndStr,
                        status = TicketStatus.PENDING,
                        price = 10000.0
                    )
                    
                    val newTicketId = repository.createTicket(ticket)
                    val fullTicket = ticket.copy(ticketId = newTicketId)
                    
                    val currentLoad = repository.getLoad(selectedLotId, scheduleDateStr, shift)
                    if (currentLoad == null) {
                        repository.updateHourlyLoad(HourlyLoad(null, selectedLotId, scheduleDateStr, shift, 1))
                    } else {
                        repository.incrementVehicleCount(selectedLotId, scheduleDateStr, shift)
                    }

                    com.uet.parking.utils.NotificationScheduler.schedulePreBookingNotification(context, fullTicket)
                    com.uet.parking.utils.NotificationScheduler.schedulePostBookingNotification(context, fullTicket)

                    successCount++
                    results.add(TicketResult(label, "$startParkingTime - $endParkingTime", true, "Đặt vé thành công tại $selectedLotId"))
                }

                _bookingUiState.update { 
                    it.copy(
                        isLoading = false, 
                        autoBookingResult = AutoBookingResult(successCount, failCount, results)
                    ) 
                }

            } catch (e: Exception) {
                _bookingUiState.update { it.copy(isLoading = false, errorMessage = "Lỗi tự động đặt vé: ${e.message}") }
            }
        }
    }
}
