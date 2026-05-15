package com.uet.parking.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

/**
 * Utility class for generating QR codes from booking data
 */
object QrCodeGenerator {

    /**
     * Tạo nội dung JSON cho QR Code từ thông tin booking
     */
    fun generateQrContent(
        bookingId: Int,
        userId: Int,
        fieldId: Int,
        bookingDate: String,
        bookingTime: String,
        slot: Int,
        status: String
    ): String {
        val json = JSONObject().apply {
            put("bookingId", bookingId)
            put("userId", userId)
            put("fieldId", fieldId)
            put("bookingDate", bookingDate)
            put("bookingTime", bookingTime)
            put("slot", slot)
            put("status", status)
            put("appSignature", "UET_PARKING_V1")
        }
        return json.toString()
    }

    /**
     * Tạo QR Code Bitmap từ chuỗi text
     */
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parse QR content JSON thành data map
     */
    fun parseQrContent(content: String): Map<String, Any?>? {
        return try {
            val json = JSONObject(content)
            // Kiểm tra chữ ký ứng dụng
            if (json.optString("appSignature") != "UET_PARKING_V1") {
                return null
            }
            mapOf(
                "bookingId" to json.getInt("bookingId"),
                "userId" to json.getInt("userId"),
                "fieldId" to json.getInt("fieldId"),
                "bookingDate" to json.getString("bookingDate"),
                "bookingTime" to json.getString("bookingTime"),
                "slot" to json.getInt("slot"),
                "status" to json.getString("status")
            )
        } catch (e: Exception) {
            null
        }
    }
}
