package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    @ColumnInfo(name = "loadId")
    // Sửa thành Int? để khớp với thực tế DB (notNull=false)
    val loadId: Int? = null,

    @ColumnInfo(name = "parkingId", defaultValue = "NULL")
    val parkingId: Int? = null,

    @ColumnInfo(name = "date", defaultValue = "NULL")
    val date: String? = null,

    @ColumnInfo(name = "shift", defaultValue = "NULL")
    val shift: Int? = null,

    @ColumnInfo(name = "vehicleCount", defaultValue = "NULL")
    val vehicleCount: Int? = null
)