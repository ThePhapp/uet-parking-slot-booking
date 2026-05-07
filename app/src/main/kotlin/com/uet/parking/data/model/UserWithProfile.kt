package com.uet.parking.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithProfile(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val info: UserInfo?
)

data class AdminWithProfile(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val adminInfo: AdminInfo?
)
