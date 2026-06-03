package com.uet.parking.data.model

import com.uet.parking.data.model.enums.SlotStatus

data class Slot(
    val id: String? = null,
    val parkingLotId: String? = null,
    val userId: String? = null,
    val coordinateX: Float? = null,
    val coordinateY: Float? = null,
    val coordinateLabel: String? = null,
    val status: String? = SlotStatus.AVAILABLE.name,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    constructor() : this(null, null, null, null, null, null, SlotStatus.AVAILABLE.name, null, null)
}
