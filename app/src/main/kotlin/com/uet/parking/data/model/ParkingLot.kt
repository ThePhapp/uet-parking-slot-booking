package com.uet.parking.data.model

data class ParkingLot(
    val id: Int,
    val name: String,
    val location: String,
    val currentLoad: Int,
    val maxCap: Int,
    val status: String,
    val hourlyLoads: List<Int> = emptyList() // Số lượng xe ở các thời điểm
) {
    val density: Int get() = if (maxCap > 0) (currentLoad * 100 / maxCap) else 0
}
