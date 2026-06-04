package com.uet.parking.data.model

import com.uet.parking.data.model.enums.SlotStatus
import java.util.UUID

data class Slot(
    val id: String = UUID.randomUUID().toString(),
    val parkingSlotId: String = "",
    val parkingLotId: String = "",
    val userId: String? = null,
    val coordinateX: Double = 0.0,
    val coordinateY: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coordinateLabel: String = "",
    val status: String = SlotStatus.AVAILABLE.value,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        parkingSlotId = "",
        parkingLotId = "",
        userId = null,
        coordinateX = 0.0,
        coordinateY = 0.0,
        latitude = null,
        longitude = null,
        coordinateLabel = "",
        status = SlotStatus.AVAILABLE.value,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
