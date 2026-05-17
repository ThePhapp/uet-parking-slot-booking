package com.uet.parking.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.uet.parking.data.model.Ticket
import org.json.JSONObject

object QrCodeGenerator {

    /**
     * Tạo nội dung JSON cho QR Code từ thông tin ticket.
     * Sử dụng JSON để đảm bảo tính mở rộng và Admin có thể parse dữ liệu dễ dàng.
     */
    fun generateQrContent(ticket: Ticket): String {
        return try {
            JSONObject().apply {
                put("ticketId", ticket.ticketId)
                put("userId", ticket.userId)
                put("parkingId", ticket.parkingId)
                // Thêm chữ ký ứng dụng để xác thực cơ bản tại local (nếu cần)
                put("appSignature", "UET_PARKING_V1")
            }.toString()
        } catch (e: Exception) {
            ticket.ticketId ?: ""
        }
    }

    /**
     * Tạo QR Code Bitmap từ chuỗi văn bản.
     */
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        if (content.isEmpty()) return null
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

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
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
     * Giải mã nội dung QR từ chuỗi JSON.
     */
    fun parseQrContent(content: String): Map<String, String>? {
        return try {
            val json = JSONObject(content)
            if (json.optString("appSignature") != "UET_PARKING_V1") return null
            
            mapOf(
                "ticketId" to json.getString("ticketId"),
                "userId" to json.getString("userId"),
                "parkingId" to json.getString("parkingId")
            )
        } catch (e: Exception) {
            // Fallback nếu QR chỉ chứa ticketId dạng String thuần
            if (content.isNotEmpty()) mapOf("ticketId" to content) else null
        }
    }
}
