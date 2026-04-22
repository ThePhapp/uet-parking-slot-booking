package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "paymentId")
    // Sửa thành Int? để khớp với notNull=false trong DB thực tế
    val paymentId: Int? = null,

    @ColumnInfo(name = "userId", defaultValue = "NULL")
    val userId: Int? = null,

    @ColumnInfo(name = "amount", defaultValue = "NULL")
    val amount: Double? = null,

    @ColumnInfo(name = "status", defaultValue = "NULL")
    val status: String? = null,

    @ColumnInfo(name = "createdAt", defaultValue = "NULL")
    val createdAt: String? = null
)