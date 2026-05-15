package com.uet.parking.data.model

import com.google.firebase.firestore.PropertyName

data class UserInfo(
    val userId: String = "",
    
    @get:PropertyName("debt")
    @set:PropertyName("debt")
    var debt: Double = 0.0,
) {
    constructor() : this("", 0.0)

    // Hàm tiện ích để lấy nợ từ bất kỳ trường nào có dữ liệu
    fun getEffectiveDebt(): Double = if (debt != 0.0) debt else 0.0
}
