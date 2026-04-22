package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parkinglot")
data class ParkingLot(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "parkingId")
    // Sửa thành Int? để khớp với notNull=false trong DB của bạn
    val parkingId: Int? = null,

    @ColumnInfo(name = "name", defaultValue = "NULL")
    val name: String? = null,

    @ColumnInfo(name = "address", defaultValue = "NULL")
    val address: String? = null,

    @ColumnInfo(name = "capacity", defaultValue = "NULL")
    val capacity: Int? = null,

    @ColumnInfo(name = "current", defaultValue = "NULL")
    val current: Int? = null,

    @ColumnInfo(name = "pricePerHour", defaultValue = "NULL")
    val pricePerHour: Double? = null,

    @ColumnInfo(name = "status", defaultValue = "NULL")
    val status: String? = null
) {
    // Đảm bảo density nằm TRONG class để không bị lỗi "Unresolved reference"
    val density: Int
        get() = if ((capacity ?: 0) > 0) ((current ?: 0) * 100 / (capacity ?: 1)) else 0
}