package com.uet.parking.data.model

import com.uet.parking.data.model.enums.UserRole

data class User(
    val userId: String? = null,
    val name: String? = null,
    val email: String = "",
    val password: String? = null,
    val role: UserRole = UserRole.USER
) {
    constructor() : this(null, null, "", null, UserRole.USER)
}
