package com.uet.parking.ui.screens.admin

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.PrimaryContainer
import com.uet.parking.ui.theme.PrimaryFixed
import com.uet.parking.ui.theme.SurfaceVariant
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun ParkingLotDetailPage(lotId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    var lot by remember { mutableStateOf<ParkingLot?>(null) }
    var nextShiftLoad by remember { mutableStateOf(0) }
    
    // Lấy thông tin bãi đỗ
    fun refreshLotData() {
        scope.launch {
            lot = database.parkingLotDao().getParkingLotById(lotId)
        }
    }

    LaunchedEffect(lotId) {
        refreshLotData()
        
        val now = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now.time)
        
        val nextShift = when {
            currentTime < "07:00" -> 1
            currentTime < "09:15" -> 2
            currentTime < "12:30" -> 3
            currentTime < "15:15" -> 4
            else -> 1
        }
        
        val load = database.hourlyLoadDao().getLoad(lotId, today, nextShift)
        nextShiftLoad = load?.vehicleCount ?: 0
    }

    if (lot == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    val primaryGradient = Brush.linearGradient(
        colors = listOf(PrimaryBlue, PrimaryContainer)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            (lot?.address ?: "").uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            lot?.name ?: "",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    Surface(
                        color = PrimaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            lot?.status ?: "",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WorkloadGaugeCard(lot!!, modifier = Modifier.weight(1.4f))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShiftStatsCard(inCount = nextShiftLoad)
                        StatusGradientCard(primaryGradient)
                    }
                }
            }

            item {
                ScanControlCard(
                    onVerifyTicket = { ticketCode ->
                        val ticketId = ticketCode.removePrefix("PKG-").removeSuffix("-UET").toIntOrNull()
                        if (ticketId == null) {
                            Toast.makeText(context, "Mã vé không hợp lệ", Toast.LENGTH_SHORT).show()
                            return@ScanControlCard
                        }

                        scope.launch {
                            val ticket = database.ticketDao().getTicketById(ticketId)
                            if (ticket == null || ticket.parkingId != lotId) {
                                Toast.makeText(context, "Vé không tồn tại hoặc sai bãi đỗ", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            when (ticket.status) {
                                TicketStatus.PENDING -> {
                                    database.ticketDao().updateTicketStatus(ticketId, TicketStatus.IN_PROGRESS.value)
                                    database.parkingLotDao().updateCurrentOccupancy(lotId, (lot?.current ?: 0) + 1)
                                    Toast.makeText(context, "Xe vào bãi thành công!", Toast.LENGTH_SHORT).show()
                                    refreshLotData()
                                }
                                TicketStatus.IN_PROGRESS -> {
                                    // 1. Tính tiền và cộng vào nợ của User
                                    val userId = ticket.userId
                                    if (userId != null) {
                                        val user = database.userDao().getUserByIdSuspend(userId)
                                        if (user != null) {
                                            val currentDebt = user.debt ?: 0.0
                                            val ticketPrice = ticket.price ?: 0.0
                                            database.userDao().updateDebt(userId, currentDebt + ticketPrice)
                                        }
                                    }

                                    // 2. Xóa vé khỏi CSDL
                                    database.ticketDao().deleteTicket(ticket)

                                    // 3. Giảm số lượng xe hiện tại
                                    val newCount = ((lot?.current ?: 0) - 1).coerceAtLeast(0)
                                    database.parkingLotDao().updateCurrentOccupancy(lotId, newCount)

                                    Toast.makeText(context, "Xe ra bãi thành công! Phí đã được cộng vào tài khoản người dùng.", Toast.LENGTH_LONG).show()
                                    refreshLotData()
                                }
                                else -> {
                                    Toast.makeText(context, "Vé không hợp lệ", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun WorkloadGaugeCard(lot: ParkingLot, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TẢI LƯỢNG HIỆN TẠI",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                Canvas(modifier = Modifier.size(130.dp)) {
                    drawArc(
                        color = SurfaceVariant,
                        startAngle = -225f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PrimaryBlue,
                        startAngle = -225f,
                        sweepAngle = 270f * (lot.density / 100f),
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${lot.density}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray
                    )
                    Text("CÔNG SUẤT", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Hiện tại", fontSize = 9.sp, color = Color.Gray)
                    Text("${lot.current ?: 0} xe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Kỳ vọng", fontSize = 9.sp, color = Color.Gray)
                    Text("${lot.capacity ?: 0} xe", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        }
    }
}

@Composable
fun ShiftStatsCard(inCount: Int = 0, outCount: Int = 0) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CA TIẾP THEO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                Text("+$inCount", fontWeight = FontWeight.Black, color = PrimaryBlue)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp), tint = Color(0xFFBA1A1A))
                Text("-$outCount", fontWeight = FontWeight.Black, color = Color(0xFFBA1A1A))
            }
        }
    }
}

@Composable
fun StatusGradientCard(gradient: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        Column {
            Text("TRẠNG THÁI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.7f))
            Text("Ổn định", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ScanControlCard(onVerifyTicket: (String) -> Unit) {
    var ticketCode by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("KIỂM SOÁT VÉ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = ticketCode,
                onValueChange = { ticketCode = it },
                placeholder = { Text("Nhập mã vé thủ công (ví dụ: PKG-1-UET)", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { 
                    if (ticketCode.isNotEmpty()) {
                        onVerifyTicket(ticketCode)
                        ticketCode = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Xác nhận", fontWeight = FontWeight.Bold)
            }
        }
    }
}
