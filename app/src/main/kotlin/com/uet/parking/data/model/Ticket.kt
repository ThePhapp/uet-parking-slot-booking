package com.uet.parking.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.ForeignKey

@Entity(
    tableName = "ticket",
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
            childColumns = ["parkingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Ticket(
    @PrimaryKey(autoGenerate = true)
    val ticketId: Int = 0,
    val userId: Int?,
    val parkingId: Int?,
    val startTime: String?,
    val endTime: String?,
    val status: String?,
    val price: Double = 0.0
)
