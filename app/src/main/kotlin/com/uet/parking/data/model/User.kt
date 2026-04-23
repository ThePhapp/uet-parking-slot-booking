package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uet.parking.data.model.enums.UserRole

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    val userId: Int? = null,

    @ColumnInfo(name = "name", defaultValue = "NULL")
    val name: String? = null,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password", defaultValue = "NULL")
    val password: String? = null,

    @ColumnInfo(name = "debt", defaultValue = "'0.00'")
    val debt: Double? = 0.0,

    @ColumnInfo(name = "role", defaultValue = "'user'")
    val role: UserRole = UserRole.USER
)
