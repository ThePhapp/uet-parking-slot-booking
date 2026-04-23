package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.uet.parking.data.model.enums.TicketStatus

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
    @ColumnInfo(name = "ticketId")
    val ticketId: Int? = null,

    @ColumnInfo(name = "userId", defaultValue = "NULL")
    val userId: Int? = null,

    @ColumnInfo(name = "parkingId", defaultValue = "NULL")
    val parkingId: Int? = null,

    @ColumnInfo(name = "startTime", defaultValue = "NULL")
    val startTime: String? = null,

    @ColumnInfo(name = "endTime", defaultValue = "NULL")
    val endTime: String? = null,

    @ColumnInfo(name = "status", defaultValue = "NULL")
    val status: TicketStatus? = TicketStatus.PENDING,

    @ColumnInfo(name = "price", defaultValue = "0.00")
    val price: Double? = 0.0
)
