package com.uet.parking.ui.screens.booking

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.PrimaryFixed
import kotlinx.coroutines.launch

@Composable
fun SuccessScreen(
    userId: Int,
    onGoHome:          () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    
    val tickets by database.ticketDao().getTicketsByUserId(userId).collectAsState(initial = emptyList())
    val latestTicket = tickets.lastOrNull()
    
    var parkingLot by remember { mutableStateOf<ParkingLot?>(null) }
    
    LaunchedEffect(latestTicket) {
        latestTicket?.parkingId?.let { id ->
            parkingLot = database.parkingLotDao().getParkingLotById(id)
        }
    }

    val fullStartTime = latestTicket?.startTime ?: "--- ---"
    val date = fullStartTime.substringBefore(" ", "---")
    val startTime = fullStartTime.substringAfter(" ", "---")
    val endTime = latestTicket?.endTime?.substringAfter(" ", "---") ?: "---"
    val ticketCode = "PKG-${latestTicket?.ticketId ?: 0}-UET"

    val iconScale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    Box(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SuccessProgressHeader()

            Spacer(modifier = Modifier.weight(0.3f))

            SuccessIcon(iconScale)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Đặt chỗ thành công!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF11131F))
            
            // Thông tin bãi đỗ nổi bật
            if (parkingLot != null) {
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalParking, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Vị trí: ${parkingLot?.name}",
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue,
                                fontSize = 16.sp
                            )
                            Text(
                                "${parkingLot?.address}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("📅 Ngày", date, Modifier.weight(1f))
                InfoCard("⏰ Giờ", "$startTime – $endTime", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            TicketQrCard(ticketCode)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Về trang chủ", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
fun SuccessProgressHeader() {
    Column(modifier = Modifier.fillMaxWidth().background(PrimaryBlue).padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Bước 3 / 3", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Text("Đặt chỗ thành công!", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Text("3 / 3", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(progress = { 1.0f }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = 0.25f))
    }
}

@Composable
fun SuccessIcon(scale: Float) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).scale(scale).clip(CircleShape).background(PrimaryFixed)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(75.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.15f))) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF003D9B), modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun TicketQrCard(ticketCode: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, PrimaryFixed), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Mã vé của bạn", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(ticketCode, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Icon(Icons.Default.QrCode, null, tint = Color.Gray, modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF11131F))
        }
    }
}
