package com.uet.parking.data.local.db

import androidx.room.TypeConverter
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.model.enums.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole?): String? = role?.value

    @TypeConverter
    fun toUserRole(value: String?): UserRole? = value?.let { UserRole.fromString(it) }

    @TypeConverter
    fun fromTicketStatus(status: TicketStatus?): String? = status?.value

    @TypeConverter
    fun toTicketStatus(value: String?): TicketStatus? = value?.let { TicketStatus.fromString(it) }
}
