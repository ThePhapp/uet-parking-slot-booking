package com.uet.parking.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.ForeignKey

@Entity(
    tableName = "hourlyloads",
    foreignKeys = [
        ForeignKey(
            entity = ParkingLot::class,
            parentColumns = ["parkingId"],
            childColumns = ["parkingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HourlyLoad(
    @PrimaryKey(autoGenerate = true)
    val loadId: Int = 0,
    val parkingId: Int?,
    val date: String?,
    val shift: Int?,
    val vehicleCount: Int?
)
