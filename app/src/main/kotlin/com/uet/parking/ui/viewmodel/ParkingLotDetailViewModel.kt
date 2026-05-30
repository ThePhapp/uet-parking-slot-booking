package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ParkingLotDetailViewModel(
    private val repository: ParkingRepository,
    private val lotId: String,
    private val adminId: String
) : ViewModel() {

    private val _lot = MutableStateFlow<ParkingLot?>(null)
    val lot = _lot.asStateFlow()

    private val _nextShiftLoad = MutableStateFlow(0)
    val nextShiftLoad = _nextShiftLoad.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        refreshLotData()
        loadNextShiftStats()
    }

    fun refreshLotData() {
        viewModelScope.launch {
            _lot.value = repository.getParkingLotById(lotId)
        }
    }

    private fun loadNextShiftStats() {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now.time)

            val nextShift = when {
                currentTime < "07:00" -> 1
                currentTime < "09:15" -> 2
                currentTime < "12:30" -> 3
                currentTime < "15:15" -> 4
                else -> 1
            }

            val load = repository.getLoad(lotId, today, nextShift)
            _nextShiftLoad.value = load?.vehicleCount ?: 0
        }
    }

    fun processCheckIn(context: android.content.Context, ticketCode: String) {
        val ticketId = extractId(ticketCode)
        if (ticketId == null) {
            _toastMessage.value = "Mã vé không hợp lệ"
            return
        }

        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId)
            val currentLot = _lot.value

            if (ticket == null || ticket.parkingId != lotId) {
                _toastMessage.value = "Vé không tồn tại hoặc không thuộc bãi đỗ này"
                return@launch
            }

            if (ticket.status != TicketStatus.PENDING) {
                _toastMessage.value = "Vé không hợp lệ (Trạng thái hiện tại: ${ticket.status?.value})"
                return@launch
            }

            // Kiểm tra thời gian: Sớm hơn tối đa 30 phút
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val startTime = try { sdf.parse(ticket.startTime ?: "") } catch (e: Exception) { null }
            
            if (startTime != null) {
                val now = Date()
                val diffMillis = startTime.time - now.time
                val diffMinutes = diffMillis / (60 * 1000)

                // Nếu đến sớm hơn 30 phút (diffMinutes > 30)
                if (diffMinutes > 30) {
                    val waitMinutes = diffMinutes - 30
                    if (waitMinutes >= 60) {
                        val hours = waitMinutes / 60
                        val mins = waitMinutes % 60
                        val hourStr = if (hours > 0) "${hours} giờ " else ""
                        val minStr = if (mins > 0) "${mins} phút" else ""
                        _toastMessage.value = "Vé bắt đầu lúc ${ticket.startTime}. Vui lòng quay lại sau $hourStr$minStr".trim()
                    } else {
                        _toastMessage.value = "Vào bãi thất bại: Vui lòng quay lại sau $waitMinutes phút"
                    }
                    return@launch
                }
            }

            repository.updateTicketStatus(ticketId, TicketStatus.IN_PROGRESS.value)
            repository.updateCurrentOccupancy(lotId, (currentLot?.current ?: 0) + 1)
            repository.incrementKPI(adminId)
            android.util.Log.d("QR_SCAN_DEBUG", "👉 thành")
            
            com.uet.parking.utils.NotificationHelper.showCheckInSuccess(context, ticketId, ticket.userId)
            
            _toastMessage.value = "Quét vào thành công! Xe đã vào bãi."
            refreshLotData()
        }
    }

    fun processCheckOut(context: android.content.Context, ticketCode: String) {
        val ticketId = extractId(ticketCode)
        if (ticketId == null) {
            _toastMessage.value = "Mã vé không hợp lệ"
            return
        }

        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId)
            val currentLot = _lot.value

            if (ticket == null || ticket.parkingId != lotId) {
                _toastMessage.value = "Vé không tồn tại hoặc không thuộc bãi đỗ này"
                return@launch
            }

            if (ticket.status != TicketStatus.IN_PROGRESS) {
                _toastMessage.value = "Vé không hợp lệ (Xe chưa check-in)"
                return@launch
            }

            // 1. Tính tiền vào nợ của User
            ticket.userId?.let { userId ->
                val userWithProfile = repository.getUserWithProfile(userId).firstOrNull()
                userWithProfile?.let { profile ->
                    val currentDebt = profile.info?.debt ?: 0.0
                    val ticketPrice = ticket.price ?: 10000.0
                    repository.updateDebt(userId, currentDebt + ticketPrice)
                }
            }

            // 2. Xóa vé và cập nhật bãi
            repository.incrementKPI(adminId)
            repository.deleteTicket(ticketId)
            
            com.uet.parking.utils.NotificationScheduler.cancelNotifications(context, ticketId)
            com.uet.parking.utils.NotificationHelper.showCheckOutSuccess(context, ticketId, ticket.userId)

            val newCount = ((currentLot?.current ?: 0) - 1).coerceAtLeast(0)
            repository.updateCurrentOccupancy(lotId, newCount)

            _toastMessage.value = "Quét ra thành công! Phí đã được tính vào nợ người dùng."
            refreshLotData()
        }
    }

    private fun extractId(code: String): String? {
        return try {
            if (code.startsWith("{")) {
                org.json.JSONObject(code).optString("bookingId", code)
            } else if (code.startsWith("PKG-")) {
                code.removePrefix("PKG-").removeSuffix("-UET")
            } else {
                code
            }
        } catch (e: Exception) {
            code
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
