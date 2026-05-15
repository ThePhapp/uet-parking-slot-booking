package com.uet.parking.ui.screens.booking

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.model.BookingEntity
import com.uet.parking.data.model.enums.BookingStatus
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.util.QrCodeGenerator

/**
 * Màn hình hiển thị QR Code cho user
 * Hiển thị full-screen QR để admin quét
 */
@Composable
fun QrCodeScreen(
    booking: BookingEntity,
    parkingLotName: String = "Sân #${booking.fieldId}",
    onDismiss: () -> Unit
) {
    val qrBitmap = remember(booking.id) {
        val content = QrCodeGenerator.generateQrContent(
            bookingId = booking.id,
            userId = booking.userId,
            fieldId = booking.fieldId,
            bookingDate = booking.bookingDate,
            bookingTime = booking.bookingTime,
            slot = booking.slot,
            status = booking.status.value
        )
        QrCodeGenerator.generateQrBitmap(content, 600)
    }

    val slotLabel = when (booking.slot) {
        1 -> "Ca 1 (07:00 - 09:00)"
        2 -> "Ca 2 (09:15 - 11:15)"
        3 -> "Ca 3 (12:30 - 14:30)"
        4 -> "Ca 4 (15:15 - 17:15)"
        else -> "Ca ${booking.slot}"
    }

    val statusColor = when (booking.status) {
        BookingStatus.PENDING -> Color(0xFFFFA726)
        BookingStatus.APPROVED -> Color(0xFF66BB6A)
        BookingStatus.CHECKED_IN -> Color(0xFF42A5F5)
        BookingStatus.REJECTED -> Color(0xFFEF5350)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "VÉ ĐIỆN TỬ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Booking #${booking.id}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // QR Code
        Card(
            modifier = Modifier.padding(horizontal = 32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Đưa mã QR này cho Admin quét",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // QR Image
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code Booking #${booking.id}",
                        modifier = Modifier.size(260.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .background(Color.LightGray.copy(0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không thể tạo QR", color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        booking.status.value,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                if (booking.isCheckedIn) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF66BB6A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Đã check-in: ${booking.checkedInAt ?: ""}",
                            fontSize = 12.sp,
                            color = Color(0xFF66BB6A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Booking Details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FD))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "THÔNG TIN ĐẶT SÂN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(16.dp))

                QrDetailRow(Icons.Default.CalendarToday, "Ngày", booking.bookingDate)
                Spacer(Modifier.height(10.dp))
                QrDetailRow(Icons.Default.AccessTime, "Ca chơi", slotLabel)
                Spacer(Modifier.height(10.dp))
                QrDetailRow(Icons.Default.Schedule, "Khung giờ", booking.bookingTime)
                Spacer(Modifier.height(10.dp))
                QrDetailRow(Icons.Default.LocationOn, "Sân", parkingLotName)
                Spacer(Modifier.height(10.dp))
                QrDetailRow(Icons.Default.Person, "User ID", "#${booking.userId}")
            }
        }

        Spacer(Modifier.height(24.dp))

        // Warning text
        if (!booking.isCheckedIn && booking.status == BookingStatus.APPROVED) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Vui lòng đưa mã QR này cho admin quét khi đến sân để check-in.",
                        fontSize = 13.sp,
                        color = Color(0xFF795548)
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun QrDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF11131F))
        }
    }
}
