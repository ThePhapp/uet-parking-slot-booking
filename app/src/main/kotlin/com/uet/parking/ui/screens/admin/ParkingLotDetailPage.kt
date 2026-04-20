package com.uet.parking.ui.screens.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uet.parking.data.model.ParkingLot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotDetailPage(lotId: Int, onBack: () -> Unit) {
    // Mock data dựa trên lotId
    val lot = ParkingLot(
        id = lotId,
        name = "Bãi đỗ xe số $lotId",
        location = "Khu vực A - Tầng 1",
        currentLoad = 150,
        maxCap = 200,
        status = "Đang hoạt động"
    )

    val primaryGradient = Brush.linearGradient(
        colors = listOf(PrimaryBlue, PrimaryContainer)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết Bãi đỗ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PrimaryBlue)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Outlined.History, contentDescription = null, tint = Color.Gray) }
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBL9lbov-PFCudf7FTEO1LNkyqUvoFG7Aur41suSTd0GjJToR5P1s6RzzzSeU28G4jzyRGOxC1c41CgfORuvA4g5m1aCuKrhGl7WG_NkBGMwX3Acy0UEHvqzqFhtw2iqJh8saKayV0BOcYak1pN5cZTDnCox1ZFYYfAXxMDg2g__xBCOFOqQaql0SaIOiizESbkiqwd_pHetDg9SlniqvcCcpZj1zKVAMWFHn9nG71E2PF3axb6zhku_tDcn-NsMZh73EZnaiOi3F4",
                        contentDescription = "User profile",
                        modifier = Modifier.size(32.dp).clip(CircleShape).border(2.dp, Color(0xFFE1F5FE), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.8f))
            )
        },
        bottomBar = { BottomNavigationBar() },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Hero: Parking Lot Identity
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(lot.location.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, letterSpacing = 1.sp)
                        Text(lot.name, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                    }
                    Surface(color = PrimaryContainer.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp)) {
                        Text(lot.status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                    }
                }
            }

            // Bento Grid: Workload & Stats
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Workload Ring Gauge
                    WorkloadGaugeCard(lot, modifier = Modifier.weight(1.4f))
                    
                    // Right Column: Shift Stats & Status
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShiftStatsCard()
                        StatusGradientCard(primaryGradient)
                    }
                }
            }

            // Scan Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                        Text("KIỂM SOÁT VÉ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 2.sp)
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                    }
                    ScanControlCard(primaryGradient)
                }
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
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Icon(Icons.Outlined.Analytics, null, modifier = Modifier.size(60.dp).align(Alignment.TopEnd), tint = Color.LightGray.copy(alpha = 0.3f))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TẢI LƯỢNG HIỆN TẠI", modifier = Modifier.fillMaxWidth(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        drawArc(color = SurfaceVariant, startAngle = -225f, sweepAngle = 270f, useCenter = false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(color = PrimaryBlue, startAngle = -225f, sweepAngle = 270f * (lot.density / 100f), useCenter = false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${lot.density}%", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.DarkGray)
                        Text("CÔNG SUẤT", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth().background(BackgroundGray, RoundedCornerShape(12.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Hiện tại", fontSize = 10.sp, color = Color.Gray)
                        Text("${lot.currentLoad} xe", fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Kỳ vọng", fontSize = 10.sp, color = Color.Gray)
                        Text("${lot.maxCap} xe", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun ShiftStatsCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(32.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
                }
                Text("CA TIẾP THEO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            DetailShiftRow(Icons.AutoMirrored.Filled.Login, "Dự kiến vào", "+42", PrimaryBlue)
            Spacer(modifier = Modifier.height(12.dp))
            DetailShiftRow(Icons.AutoMirrored.Filled.Logout, "Dự kiến ra", "-18", Color(0xFF7B2600))
        }
    }
}

@Composable
fun DetailShiftRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
            Text(label, fontSize = 13.sp, color = Color.DarkGray)
        }
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun StatusGradientCard(gradient: Brush) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(gradient).padding(20.dp)) {
        Column {
            Text("TRẠNG THÁI BÃI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.8f))
            Text("Mật độ trung bình", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color.White.copy(0.2f), shape = CircleShape) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Text("Lưu thông ổn định", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ScanControlCard(gradient: Brush) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.5f).background(Color.Black)) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDqarKqcmaYmd95unawo_LfmseagiuoagmdCkjmNbPBpizgavKrMDhNcBwNYX4fQ5nUkYFlgBk4Pad5P5B_Ca_kQbLPY6ngKV6GEPwYJxJ9V03BGhKQJiCb8g-ND0R2wkprD5GLYzTFYJAWznWPufkUB6IS9D0kEuicGbHfLzvTNdqc1xzGiT3LGaQxvXYznxvIjeOqdBLfoxPJ7V8kOb_hh0Y5P13PnvXFDJCkWhNltQ3mPQKpW7Sq7_5d0wFGBSRzWpIVKqwFLQM",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.6f),
                    contentScale = ContentScale.Crop
                )
                
                // QR Scanner Overlay
                Box(modifier = Modifier.align(Alignment.Center).size(200.dp).border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(32.dp))) {
                    ScannerCorners()
                    val infiniteTransition = rememberInfiniteTransition(label = "")
                    val translateY by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 200f, animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "")
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).offset(y = translateY.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, PrimaryBlue, Color.Transparent))))
                }
                
                Text("Quét mã QR để check-in/out", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).background(Color.Black.copy(0.4f), CircleShape).padding(horizontal = 16.dp, vertical = 8.dp), color = Color.White, fontSize = 12.sp)
                
                Row(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingActionButton(onClick = {}, containerColor = Color.White.copy(0.2f), contentColor = Color.White, modifier = Modifier.size(40.dp), shape = CircleShape) { Icon(Icons.Default.FlashlightOn, null) }
                    FloatingActionButton(onClick = {}, containerColor = Color.White.copy(0.2f), contentColor = Color.White, modifier = Modifier.size(40.dp), shape = CircleShape) { Icon(Icons.Default.FlipCameraIos, null) }
                }
            }
            
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("NHẬP MÃ VÉ THỦ CÔNG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = "", onValueChange = {},
                        placeholder = { Text("Ví dụ: PKG-2023-889", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null, tint = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = BackgroundGray, focusedContainerColor = BackgroundGray, unfocusedBorderColor = Color.Transparent, focusedBorderColor = PrimaryBlue.copy(0.2f))
                    )
                    Button(onClick = {}, modifier = Modifier.height(56.dp).fillMaxWidth(0.3f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                        Text("Xác nhận", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE3F2FD).copy(0.3f), RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Thay thế ShieldPerson bằng Security nếu không tìm thấy
                    Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                    Text("Chỉ dành cho nhân viên ủy quyền. Mọi hoạt động đều được ghi lại.", fontSize = 10.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun ScannerCorners() {
    Box(modifier = Modifier.fillMaxSize()) {
        val cornerColor = PrimaryBlue
        val thickness = 4.dp
        val size = 30.dp
        Box(modifier = Modifier.size(size).align(Alignment.TopStart).border(thickness, cornerColor, RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp)))
        Box(modifier = Modifier.size(size).align(Alignment.TopEnd).border(thickness, cornerColor, RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)))
        Box(modifier = Modifier.size(size).align(Alignment.BottomStart).border(thickness, cornerColor, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 0.dp)))
        Box(modifier = Modifier.size(size).align(Alignment.BottomEnd).border(thickness, cornerColor, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 16.dp)))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailPreview() { ParkingLotDetailPage(3, {}) }
