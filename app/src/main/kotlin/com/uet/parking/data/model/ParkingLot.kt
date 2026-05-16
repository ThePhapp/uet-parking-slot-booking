package com.uet.parking.data.model

data class ParkingLot(
    val parkingId: String? = null,
    val name: String? = null,
    val address: String? = null,
    val capacity: Int? = null,
    val current: Int? = null,
    val pricePerHour: Double? = null,
    val status: String? = null
) {
    // Firestore requires a no-argument constructor
    constructor() : this(null, null, null, null, null, null, null)

    val density: Int
        get() = if ((capacity ?: 0) > 0) ((current ?: 0) * 100 / (capacity ?: 1)) else 0
}
