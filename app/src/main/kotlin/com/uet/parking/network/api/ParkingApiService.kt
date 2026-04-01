package com.uet.parking.network.api

import com.uet.parking.data.model.ParkingSlot
import com.uet.parking.data.model.Lecture
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API Service cho ứng dụng Quản lý Bãi Đỗ Xe
 * 
 * Định nghĩa các endpoint API
 */
interface ParkingApiService {

    /**
     * Lấy danh sách tất cả chỗ đỗ xe
     */
    @GET("/api/parking-slots")
    fun getAllParkingSlots(): Call<List<ParkingSlot>>

    /**
     * Lấy danh sách chỗ đỗ xe còn trống
     */
    @GET("/api/parking-slots/available")
    fun getAvailableSlots(): Call<List<ParkingSlot>>

    /**
     * Lấy thông tin chi tiết một chỗ đỗ
     */
    @GET("/api/parking-slots/{slotId}")
    fun getParkingSlotDetail(@Path("slotId") slotId: String): Call<ParkingSlot>

    /**
     * Cập nhật trạng thái chỗ đỗ (trống/đã đỗ)
     */
    @POST("/api/parking-slots/{slotId}/update")
    fun updateParkingSlotStatus(@Path("slotId") slotId: String): Call<ParkingSlot>

    /**
     * Lấy danh sách all giảng đường
     */
    @GET("/api/lectures")
    fun getAllLectures(): Call<List<Lecture>>

    /**
     * Lấy thống kê bãi đỗ xe
     */
    @GET("/api/statistics")
    fun getStatistics(): Call<Map<String, Any>>
}
