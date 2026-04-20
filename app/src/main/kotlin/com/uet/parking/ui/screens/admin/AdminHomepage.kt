package com.uet.parking.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uet.parking.data.model.ParkingLot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomepage(onNavigateToDetail: (Int) -> Unit) {
    // Mock data 3 bãi đỗ xe
    val parkingLots = listOf(
        ParkingLot(
            id = 1,
            name = "Bãi đỗ xe số 1",
            location = "Khu vực Phía Bắc - Cổng A",
            currentLoad = 170,
            maxCap = 200,
            status = "Busy",
            hourlyLoads = listOf(150, 155, 160, 165, 170)
        ),
        ParkingLot(
            id = 2,
            name = "Bãi đỗ xe số 2",
            location = "Khu trung tâm - Tòa C1",
            currentLoad = 42,
            maxCap = 100,
            status = "Normal",
            hourlyLoads = listOf(30, 35, 40, 42, 42)
        ),
        ParkingLot(
            id = 3,
            name = "Bãi đỗ xe số 3",
            location = "Khu vực Phía Nam - Ký túc xá",
            currentLoad = 150,
            maxCap = 250,
            status = "Normal",
            hourlyLoads = listOf(140, 145, 148, 150, 150)
        )
    )

    // Tính toán thống kê từ danh sách bãi đỗ
    val totalSlots = parkingLots.sumOf { it.maxCap }
    val currentTotalLoad = parkingLots.sumOf { it.currentLoad }
    val availableSlots = totalSlots - currentTotalLoad

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDrovaXBF_R-_oy6fGiqVjYApxiKFHyixKmJMOpPOGFpmX-CA_huKh9Wg5FgVDEG3UPERAXZx66VDr4rjObSiOGKouKcLcX4BlmuQc4rea5vrdeQSupJLj0RohX6-26rSQhmWo_IIilyBaN7wJOOOCB_05EoCFfTUe9V0auXGQRAuT9X3ZKMVuMQEy_Q5ecloZXB3_7s_siFfCSqaC4pPPZ3_SyN9qza18nOttij2SljpHcseBtCPFPIFqkiF0SCfQYDJlVDyRg5TI",
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Quản lý Bãi đỗ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.History, contentDescription = "History", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { BottomNavigationBar() },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Hero Stats Bento Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeroMainStatsCard(totalSlots, availableSlots, modifier = Modifier.weight(2f))
                    HeroPeakHourCard(modifier = Modifier.weight(1.2f))
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "KHU VỰC KIỂM SOÁT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Danh sách Bãi đỗ",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    Text(
                        "Tất cả >",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { }
                    )
                }
            }

            // Parking Lot Cards
            items(parkingLots) { lot ->
                ParkingLotCard(lot = lot, onDetailClick = { onNavigateToDetail(lot.id) })
            }

            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun HeroMainStatsCard(totalSlots: Int, availableSlots: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(180.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                Icons.Outlined.Security,
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp),
                tint = Color.White.copy(alpha = 0.1f)
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "TRẠNG THÁI TỔNG THỂ",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "Hoạt động ổn định",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(totalSlots.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("TỔNG VỊ TRÍ", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(availableSlots.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("CÒN TRỐNG", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HeroPeakHourCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    "GIỜ CAO ĐIỂM",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "16:30 - 18:30",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Column {
                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = PrimaryBlue,
                    trackColor = SurfaceVariant
                )
                Text(
                    "Dự kiến tăng 15% trong 30p tới",
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ParkingLotCard(lot: ParkingLot, onDetailClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon bãi đỗ
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(BackgroundGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalParking, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            }

            // Thông tin bãi đỗ
            Column(modifier = Modifier.weight(1f)) {
                Text(lot.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(lot.location, color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress Bar & Status
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Mật độ: ${lot.density}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Surface(
                        color = if (lot.status == "Busy") Color(0xFFFFDBCF) else Color(0xFFDAE2FF),
                        shape = CircleShape
                    ) {
                        Text(
                            lot.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (lot.status == "Busy") Color(0xFF812800) else Color(0xFF0040A2)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { lot.density / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = PrimaryBlue,
                    trackColor = SurfaceVariant
                )
            }

            // Nút bấm
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.background(BackgroundGray, CircleShape)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color.Gray)
                }
                Button(
                    onClick = onDetailClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Chi tiết", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminPreview() {
    AdminHomepage({})
}
