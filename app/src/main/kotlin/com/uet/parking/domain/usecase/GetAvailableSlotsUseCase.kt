package com.uet.parking.domain.usecase

import com.uet.parking.data.model.ParkingSlot
import com.uet.parking.data.repository.ParkingRepository

/**
 * Use Case: Lấy danh sách các chỗ đỗ xe còn trống
 * 
 * Chức năng: Chứa logic nghiệp vụ riêng biệt
 * - Được gọi từ ViewModel
 * - Sử dụng Repository để lấy dữ liệu
 * - Có thể xử lý logic phức tạp trước khi trả về UI
 */
class GetAvailableSlotsUseCase(
    private val repository: ParkingRepository
) {

    fun execute(): List<ParkingSlot> {
        val slots = repository.getAvailableSlots()
        
        // TODO: Có thể thêm logic xử lý, filter, sort ở đây
        // Ví dụ: sắp xếp theo vị trí gần nhất
        
        return slots
    }
}
