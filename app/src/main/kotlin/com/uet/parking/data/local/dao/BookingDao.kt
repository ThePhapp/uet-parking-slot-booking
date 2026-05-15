package com.uet.parking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uet.parking.data.model.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Query("SELECT * FROM booking ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM booking WHERE userId = :userId ORDER BY createdAt DESC")
    fun getBookingsByUserId(userId: Int): Flow<List<BookingEntity>>

    @Query("SELECT * FROM booking WHERE id = :bookingId LIMIT 1")
    suspend fun getBookingById(bookingId: Int): BookingEntity?

    /**
     * Đếm số booking đã tồn tại cho sân, ngày và ca cụ thể (chỉ tính PENDING + APPROVED)
     */
    @Query("""
        SELECT COUNT(*) FROM booking 
        WHERE fieldId = :fieldId 
        AND bookingDate = :date 
        AND slot = :slot 
        AND status IN ('Pending', 'Approved')
    """)
    suspend fun countBookingsForSlot(fieldId: Int, date: String, slot: Int): Int

    /**
     * Kiểm tra user đã đặt sân cùng ngày cùng ca chưa
     */
    @Query("""
        SELECT COUNT(*) FROM booking 
        WHERE userId = :userId 
        AND bookingDate = :date 
        AND slot = :slot 
        AND status IN ('Pending', 'Approved')
    """)
    suspend fun countUserBookingsForSlot(userId: Int, date: String, slot: Int): Int

    /**
     * Đếm tổng booking cho một ngày và ca (trên tất cả sân)
     */
    @Query("""
        SELECT COUNT(*) FROM booking 
        WHERE bookingDate = :date 
        AND slot = :slot 
        AND status IN ('Pending', 'Approved')
    """)
    suspend fun countAllBookingsForSlot(date: String, slot: Int): Int

    /**
     * Lấy danh sách fieldId đã đầy cho ngày và ca cụ thể
     */
    @Query("""
        SELECT b.fieldId FROM booking b 
        INNER JOIN parkinglot p ON b.fieldId = p.parkingId 
        WHERE b.bookingDate = :date 
        AND b.slot = :slot 
        AND b.status IN ('Pending', 'Approved') 
        GROUP BY b.fieldId 
        HAVING COUNT(*) >= p.capacity
    """)
    suspend fun getFullFieldIds(date: String, slot: Int): List<Int>

    @Query("UPDATE booking SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: Int, status: String)

    @Query("DELETE FROM booking WHERE id = :bookingId")
    suspend fun deleteBooking(bookingId: Int)

    /**
     * Lấy tất cả booking PENDING cho admin duyệt
     */
    @Query("SELECT * FROM booking WHERE status = 'Pending' ORDER BY createdAt DESC")
    fun getPendingBookings(): Flow<List<BookingEntity>>
}
