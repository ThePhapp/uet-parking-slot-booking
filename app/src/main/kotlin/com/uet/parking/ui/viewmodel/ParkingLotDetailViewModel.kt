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
    private val lotId: Int,
    private val adminId: Int // Thêm adminId để cập nhật KPI
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

    fun verifyTicket(ticketCode: String) {
        val ticketId = ticketCode.removePrefix("PKG-").removeSuffix("-UET").toIntOrNull()
        if (ticketId == null) {
            _toastMessage.value = "Mã vé không hợp lệ"
            return
        }

        viewModelScope.launch {
            val ticket = repository.getTicketById(ticketId)
            val currentLot = _lot.value

            if (ticket == null || ticket.parkingId != lotId) {
                _toastMessage.value = "Vé không tồn tại hoặc sai bãi đỗ"
                return@launch
            }

            when (ticket.status) {
                TicketStatus.PENDING -> {
                    repository.updateTicketStatus(ticketId, TicketStatus.IN_PROGRESS.value)
                    repository.updateCurrentOccupancy(lotId, (currentLot?.current ?: 0) + 1)
                    
                    // Thăng tiến KPI cho Admin khi quét vé vào thành công
                    repository.incrementKPI(adminId)
                    
                    _toastMessage.value = "Xe vào bãi thành công!"
                    refreshLotData()
                }
                TicketStatus.IN_PROGRESS -> {
                    // 1. Lấy thông tin User kèm Profile (chứa debt trong info)
                    ticket.userId?.let { userId ->
                        val userWithProfile = repository.getUserWithProfile(userId).firstOrNull()

                        userWithProfile?.let { profile ->
                            val currentDebt = profile.info?.debt ?: 0.0
                            val ticketPrice = ticket.price ?: 10000.0

                            repository.updateDebt(userId, currentDebt + ticketPrice)
                        }
                    }

                    // 2. Thăng tiến KPI cho Admin khi quét vé ra thành công
                    repository.incrementKPI(adminId)

                    // 3. Xóa vé khỏi CSDL
                    repository.deleteTicket(ticket)

                    // 4. Giảm số lượng xe hiện tại
                    val newCount = ((currentLot?.current ?: 0) - 1).coerceAtLeast(0)
                    repository.updateCurrentOccupancy(lotId, newCount)

                    _toastMessage.value = "Xe ra bãi thành công! Phí đã được cộng vào tài khoản người dùng."
                    refreshLotData()
                }
                else -> {
                    _toastMessage.value = "Vé không hợp lệ"
                }
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}