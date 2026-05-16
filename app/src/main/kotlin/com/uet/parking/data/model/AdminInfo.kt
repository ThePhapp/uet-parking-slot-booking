package com.uet.parking.data.model

data class AdminInfo(
    val userId: String = "",
    val parkingId: String? = null,
    val kpi: Int = 0
) {
    constructor() : this("", null, 0)
}
