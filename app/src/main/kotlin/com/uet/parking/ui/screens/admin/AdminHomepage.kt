package com.uet.parking.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.model.ParkingLot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomepage(onNavigateToDetail: (Int) -> Unit) {
    val parkingLots = listOf(
        ParkingLot(1, "Bãi đỗ xe số 1", "Khu vực Phía Bắc - Cổng A", 170, 200, "Busy"),
        ParkingLot(2, "Bãi đỗ xe số 2", "Khu trung tâm - Tòa C1", 42, 100, "Normal"),
        ParkingLot(3, "Bãi đỗ xe số 3", "Khu vực Phía Nam - Ký túc xá", 150, 250, "Normal")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Quản lý Bãi đỗ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = PrimaryBlue))
                    }
                },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { BottomNavigationBar() },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item { HeroStatsCard() }
            item {
                Text("Danh sách Bãi đỗ", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
            }
            items(parkingLots) { lot ->
                ParkingLotCard(lot = lot, onDetailClick = { onNavigateToDetail(lot.id) })
            }
            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun HeroStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("TRẠNG THÁI TỔNG THỂ", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Hoạt động ổn định", color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Column { Text("1,248", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("TỔNG VỊ TRÍ", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp) }
                Spacer(modifier = Modifier.width(40.dp))
                Column { Text("156", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("CÒN TRỐNG", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp) }
            }
        }
    }
}

@Composable
fun ParkingLotCard(lot: ParkingLot, onDetailClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).background(BackgroundGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocalParking, contentDescription = null, tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(lot.name, fontWeight = FontWeight.Bold)
                    Text(lot.location, color = Color.Gray, fontSize = 12.sp)
                }
                // Hiển thị workload x / Maxcap ở góc phải card
                Column(horizontalAlignment = Alignment.End) {
                    Text("${lot.currentLoad}/${lot.maxCap}", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 14.sp)
                    Text("Workload", color = Color.Gray, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = lot.density / 100f, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = PrimaryBlue, trackColor = SurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onDetailClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Chi tiết")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminPreview() { AdminHomepage({}) }
