package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_info",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserInfo(
    @PrimaryKey
    @ColumnInfo(name = "userId")
    val userId: Int,

    @ColumnInfo(name = "dept")
    val dept: Double? = 0.0
)
