package com.uet.parking.data.model

data class HourlyLoad(
    val loadId: String? = null,
    val parkingId: String? = null,
    val date: String? = null,
    val shift: Int? = null,
    val vehicleCount: Int? = null
) {
    constructor() : this(null, null, null, null, null)
}
