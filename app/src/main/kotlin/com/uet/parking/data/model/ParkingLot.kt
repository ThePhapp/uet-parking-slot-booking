package com.uet.parking.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "parkinglot")
data class ParkingLot(
    @PrimaryKey(autoGenerate = true)
    val parkingId: Int = 0,
    val name: String?,
    val address: String?,
    val capacity: Int?,
    val current: Int?,
    val pricePerHour: Double?,
    val status: String?
) {
    val density: Int get() = if ((capacity ?: 0) > 0) ((current ?: 0) * 100 / capacity!!) else 0
}
