package com.uet.parking.data.model

data class UserInfo(
    val userId: String = "",
    var debt: Double = 0.0
) {
    constructor() : this("", 0.0)
}
