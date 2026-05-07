package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "admin_info",
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
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class AdminInfo(
    @PrimaryKey
    @ColumnInfo(name = "userId")
    val userId: Int,

    @ColumnInfo(name = "parkingId")
    val parkingId: Int? = null,

    @ColumnInfo(name = "kpi")
    val kpi: Double? = 0.0
)
