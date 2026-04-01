package com.uet.parking.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class cho xử lý ngày giờ
 */
object DateUtils {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("vi", "VN"))

    fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        return dateFormat.format(calendar.time)
    }

    fun getCurrentTimestamp(): Long = System.currentTimeMillis()

    fun getTimeDifference(startTime: Long, endTime: Long): Long {
        return endTime - startTime
    }
}
