package com.uet.parking.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model đại diện cho một chỗ đỗ xe
 */
data class ParkingSlot(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("slotNumber")
    val slotNumber: String,
    
    @SerializedName("location")
    val location: String, // Giảng đường hoặc khu vực
    
    @SerializedName("isAvailable")
    val isAvailable: Boolean,
    
    @SerializedName("type")
    val type: String, // "regular" hoặc "reserved"
    
    @SerializedName("lastUpdated")
    val lastUpdated: Long
)
