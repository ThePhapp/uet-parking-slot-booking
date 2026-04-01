package com.uet.parking.data.repository

import com.uet.parking.data.model.ParkingSlot
import com.uet.parking.data.model.Lecture

/**
 * Repository quản lý dữ liệu đỗ xe
 * Lớp này đóng vai trò trung gian giữa UI và Data Layer
 */
class ParkingRepository {

    /**
     * Lấy tất cả các chỗ đỗ xe
     */
    fun getAllParkingSlots(): List<ParkingSlot> {
        return generateMockParkingSlots()
    }

    /**
     * Lấy các chỗ đỗ xe còn trống
     */
    fun getAvailableSlots(): List<ParkingSlot> {
        return getAllParkingSlots().filter { it.isAvailable }
    }

    /**
     * Lấy các chỗ đỗ xe đã được đỗ
     */
    fun getOccupiedSlots(): List<ParkingSlot> {
        return getAllParkingSlots().filter { !it.isAvailable }
    }

    /**
     * Đặt/Giải phóng một chỗ đỗ
     */
    fun toggleParkingSlot(slotId: String, isAvailable: Boolean): Boolean {
        // TODO: Gọi API để cập nhật trạng thái
        return true
    }

    /**
     * Lấy danh sách các giảng đường
     */
    fun getLectures(): List<Lecture> {
        return generateMockLectures()
    }

    /**
     * Dữ liệu giả lập cho phát triển
     */
    private fun generateMockParkingSlots(): List<ParkingSlot> {
        return listOf(
            ParkingSlot(
                id = "1",
                slotNumber = "A01",
                location = "Khu A - Giảng đường 101",
                isAvailable = true,
                type = "regular",
                lastUpdated = System.currentTimeMillis()
            ),
            ParkingSlot(
                id = "2",
                slotNumber = "A02",
                location = "Khu A - Giảng đường 101",
                isAvailable = false,
                type = "regular",
                lastUpdated = System.currentTimeMillis()
            ),
            ParkingSlot(
                id = "3",
                slotNumber = "B01",
                location = "Khu B - Giảng đường 201",
                isAvailable = true,
                type = "reserved",
                lastUpdated = System.currentTimeMillis()
            ),
            ParkingSlot(
                id = "4",
                slotNumber = "B02",
                location = "Khu B - Giảng đường 201",
                isAvailable = true,
                type = "regular",
                lastUpdated = System.currentTimeMillis()
            ),
            ParkingSlot(
                id = "5",
                slotNumber = "B03",
                location = "Khu B - Giảng đường 201",
                isAvailable = false,
                type = "regular",
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    private fun generateMockLectures(): List<Lecture> {
        return listOf(
            Lecture(
                id = "1",
                name = "Giảng đường 101",
                floor = 1,
                capacity = 100,
                totalParkingSlots = 20,
                availableSlots = 15
            ),
            Lecture(
                id = "2",
                name = "Giảng đường 201",
                floor = 2,
                capacity = 80,
                totalParkingSlots = 15,
                availableSlots = 8
            )
        )
    }
}
