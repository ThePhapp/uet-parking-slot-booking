package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "adminInfo",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = ParkingLot::class,
            parentColumns = ["parkingId"],
            childColumns = ["parkingId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class AdminInfo(
    @PrimaryKey
    @ColumnInfo(name = "userId")
    val userId: Int,

    @ColumnInfo(name = "parkingId")
    val parkingId: Int? = null,

    @ColumnInfo(name = "kpi", defaultValue = "0")
    val kpi: Int = 0
)
