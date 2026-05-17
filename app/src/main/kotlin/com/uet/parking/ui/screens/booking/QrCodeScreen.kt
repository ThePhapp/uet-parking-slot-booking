package com.uet.parking.ui.screens.booking

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.utils.QrCodeGenerator

/**
 * Màn hình hiển thị mã QR phóng to, thiết kế theo phong cách vé điện tử chuyên nghiệp.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScreen(
    ticket: Ticket,
    parkingLotName: String = "Bãi đỗ xe UET - GĐ4",
    onDismiss: () -> Unit
) {
    val ticketCode = "PKG-${ticket.ticketId ?: "N/A"}-UET"
    
    // Polish: Tạo nội dung QR dạng JSON đồng bộ với QrCodeGenerator mới nhất
    val qrContent = remember(ticket.ticketId) {
        QrCodeGenerator.generateQrContent(ticket)
    }

    val qrBitmap = remember(qrContent) {
        QrCodeGenerator.generateQrBitmap(qrContent, 800)
    }

    // Xử lý tách chuỗi thời gian an toàn
    val fullStartTime = ticket.startTime ?: "--- ---"
    val date = fullStartTime.substringBefore(" ")
    val startTime = fullStartTime.substringAfter(" ")
    val endTime = ticket.endTime?.substringAfter(" ") ?: "---"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vé gửi xe điện tử", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VNU - UET CAMPUS PARKING",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // QR Code Section với viền bo góc tinh tế
                    if (qrBitmap != null) {
                        Surface(
                            modifier = Modifier
                                .size(260.dp)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.padding(16.dp).fillMaxSize()
                            )
                        }
                    } else {
                        Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = ticketCode,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Hiệu ứng dập lỗ vé chuyên nghiệp
                    TicketVisualDivider()

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Thông tin vé chi tiết với Icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TicketDetailItem(Icons.Default.CalendarToday, "NGÀY ĐẶT", date, Modifier.weight(1f))
                        TicketDetailItem(Icons.Default.AccessTime, "GIỜ GỬI", "$startTime - $endTime", Modifier.weight(1.3f), Alignment.End)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalParking, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parkingLotName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Badge Trạng thái sống động
                    val statusColor = when (ticket.status) {
                        TicketStatus.PENDING -> Color(0xFFFFA000)
                        TicketStatus.IN_PROGRESS -> PrimaryBlue
                        TicketStatus.CONFIRMED -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = ticket.status?.value?.uppercase() ?: "UNKNOWN",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Hướng dẫn sử dụng cho người dùng
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        color = PrimaryBlue.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Vui lòng giữ độ sáng màn hình ở mức tối đa để nhân viên bãi xe quét mã dễ dàng hơn.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBlue,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Quay lại", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TicketVisualDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vết cắt trái hình bán nguyệt
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 28.dp)
                .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                .background(BackgroundGray)
        )

        Canvas(
            Modifier
                .weight(1f)
                .height(1.dp)
                .padding(horizontal = 10.dp)
        ) {
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        }

        // Vết cắt phải hình bán nguyệt
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 28.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .background(BackgroundGray)
        )
    }
}

@Composable
private fun TicketDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, 
    value: String, 
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
    }
}
