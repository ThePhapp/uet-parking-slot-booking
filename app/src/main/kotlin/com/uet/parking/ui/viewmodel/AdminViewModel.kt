package com.uet.parking.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.uet.parking.data.model.AdminWithProfile
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.worker.TicketCleanupWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AdminViewModel(
    private val repository: ParkingRepository,
    private val userId: String
) : ViewModel() {

    val adminProfile: StateFlow<AdminWithProfile?> = repository.getAdminWithProfile(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val parkingLots: StateFlow<List<ParkingLot>> = repository.getAllParkingLots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSlots: StateFlow<Int> = parkingLots.map { lots ->
        lots.sumOf { it.capacity ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val availableSlots: StateFlow<Int> = parkingLots.map { lots ->
        val total = lots.sumOf { it.capacity ?: 0 }
        val occupied = lots.sumOf { it.current ?: 0 }
        total - occupied
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val kpi: StateFlow<Int> = adminProfile.map { it?.adminInfo?.kpi ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val externalTickets: StateFlow<List<Ticket>> = combine(repository.getAllTickets(), adminProfile) { tickets, profile ->
        val lotId = profile?.adminInfo?.parkingLotId
        if (lotId == null) emptyList()
        else tickets.filter { it.parkingId == lotId && it.userId?.startsWith("EXTERNAL_GUEST_") == true }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun clearToast() { _toastMessage.value = null }

    fun createExternalTicket(context: Context, endTimeStr: String) {
        val admin = adminProfile.value
        val lotId = admin?.adminInfo?.parkingLotId
        
        if (lotId == null) {
            _toastMessage.value = "Tài khoản admin chưa được gán bãi đỗ"
            return
        }

        viewModelScope.launch {
            try {
                val lot = repository.getParkingLotById(lotId) ?: return@launch
                val now = Calendar.getInstance()
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dateSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                val startTimeStr = sdf.format(now.time)
                val todayStr = dateSdf.format(now.time)
                val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)

                val shift = when {
                    currentTimeStr < "09:15" -> 1
                    currentTimeStr < "12:30" -> 2
                    currentTimeStr < "15:15" -> 3
                    else -> 4
                }

                // 1. Ràng buộc sức chứa (90%)
                val capacity = lot.capacity ?: 0
                val currentLoad = repository.getLoad(lotId, todayStr, shift)
                val vehicleCount = currentLoad?.vehicleCount ?: 0
                if (vehicleCount + 1 > capacity * 0.9) {
                    _toastMessage.value = "Không thể đặt: Bãi đã đạt giới hạn an toàn (90%)"
                    return@launch
                }

                // 2. Kiểm tra lưu lượng chuyển ca (50%)
                val (startIncoming, startOutgoing) = repository.getShiftFlowLoad(lotId, startTimeStr)
                if ((startIncoming + 1) + startOutgoing > capacity * 0.5) {
                    _toastMessage.value = "Không thể đặt: Lưu lượng chuyển ca hiện tại quá tải"
                    return@launch
                }

                // Ràng buộc tại mốc kết thúc
                val fullEndTimeStr = "$todayStr $endTimeStr"
                val (endIncoming, endOutgoing) = repository.getShiftFlowLoad(lotId, fullEndTimeStr)
                if (endIncoming + (endOutgoing + 1) > capacity * 0.5) {
                    _toastMessage.value = "Không thể đặt: Lưu lượng tại giờ kết thúc dự kiến quá tải"
                    return@launch
                }

                val ticket = Ticket(
                    userId = "EXTERNAL_GUEST_${System.currentTimeMillis()}",
                    parkingId = lotId,
                    startTime = startTimeStr,
                    endTime = fullEndTimeStr,
                    status = TicketStatus.IN_PROGRESS,
                    price = 0.0
                )
                
                val ticketId = repository.createTicket(ticket)
                repository.updateCurrentOccupancy(lotId, (lot.current ?: 0) + 1)
                repository.incrementVehicleCount(lotId, todayStr, shift)
                scheduleTicketCleanup(context, ticketId, lotId, fullEndTimeStr)

                _toastMessage.value = "Đặt vé ngoài thành công! Kết thúc lúc $endTimeStr"
            } catch (e: Exception) {
                _toastMessage.value = "Lỗi hệ thống: ${e.message}"
            }
        }
    }

    fun deleteExternalTicket(ticket: Ticket) {
        val ticketId = ticket.ticketId ?: return
        val lotId = ticket.parkingId ?: return

        viewModelScope.launch {
            try {
                val lot = repository.getParkingLotById(lotId)
                if (lot != null) {
                    val newCount = ((lot.current ?: 0) - 1).coerceAtLeast(0)
                    repository.updateCurrentOccupancy(lotId, newCount)
                }

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val startTime = try { sdf.parse(ticket.startTime ?: "") } catch (_: Exception) { null }
                if (startTime != null) {
                    val dateSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateSdf.format(startTime)
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTime)
                    val shift = when {
                        time < "09:15" -> 1
                        time < "12:30" -> 2
                        time < "15:15" -> 3
                        else -> 4
                    }
                    repository.decrementVehicleCount(lotId, date, shift)
                }

                repository.deleteTicket(ticketId)
                _toastMessage.value = "Đã xóa vé ngoài và cập nhật lại bãi đỗ"
            } catch (e: Exception) {
                _toastMessage.value = "Lỗi khi xóa vé: ${e.message}"
            }
        }
    }

    private fun scheduleTicketCleanup(context: Context, ticketId: String, lotId: String, endTimeStr: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val endTime = try { sdf.parse(endTimeStr) } catch (_: Exception) { null } ?: return
        val delay = endTime.time - System.currentTimeMillis()

        if (delay > 0) {
            val data = Data.Builder()
                .putString("ticketId", ticketId)
                .putString("lotId", lotId)
                .build()

            val cleanupRequest = OneTimeWorkRequestBuilder<TicketCleanupWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("CLEANUP_$ticketId")
                .build()

            WorkManager.getInstance(context).enqueue(cleanupRequest)
        }
    }
}
