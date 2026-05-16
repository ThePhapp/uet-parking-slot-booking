package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.uet.parking.data.model.enums.BookingStatus

@Entity(
    tableName = "booking",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParkingLot::class,
            parentColumns = ["parkingId"],
            childColumns = ["fieldId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "userId")
    val userId: Int,

    @ColumnInfo(name = "fieldId")
    val fieldId: Int,

    @ColumnInfo(name = "bookingDate")
    val bookingDate: String, // format: dd/MM/yyyy

    @ColumnInfo(name = "bookingTime")
    val bookingTime: String, // format: HH:mm - HH:mm

    @ColumnInfo(name = "slot")
    val slot: Int, // Ca: 1, 2, 3, 4

    @ColumnInfo(name = "status", defaultValue = "Pending")
    val status: BookingStatus = BookingStatus.PENDING,

    @ColumnInfo(name = "createdAt")
    val createdAt: String, // format: yyyy-MM-dd HH:mm:ss

    @ColumnInfo(name = "qrCode", defaultValue = "")
    val qrCode: String = "", // JSON encoded QR content

    @ColumnInfo(name = "checkedInAt", defaultValue = "NULL")
    val checkedInAt: String? = null, // Thời gian check-in

    @ColumnInfo(name = "isCheckedIn", defaultValue = "0")
    val isCheckedIn: Boolean = false // Đã check-in chưa
)
