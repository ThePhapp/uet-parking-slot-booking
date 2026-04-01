package com.uet.parking.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model đại diện cho một giảng đường
 */
data class Lecture(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("floor")
    val floor: Int,
    
    @SerializedName("capacity")
    val capacity: Int,
    
    @SerializedName("totalParkingSlots")
    val totalParkingSlots: Int,
    
    @SerializedName("availableSlots")
    val availableSlots: Int
)
