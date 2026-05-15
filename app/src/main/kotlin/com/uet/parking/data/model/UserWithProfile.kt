package com.uet.parking.data.model

data class UserWithProfile(
    val user: User,
    val info: UserInfo?
)

data class AdminWithProfile(
    val user: User,
    val adminInfo: AdminInfo?
)
