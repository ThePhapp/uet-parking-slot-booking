package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "userInfo",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class UserInfo(
    @PrimaryKey
    val userId: Int,

    @ColumnInfo(name = "debt", defaultValue = "0.00")
    val debt: Double = 0.0
)
