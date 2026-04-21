package com.uet.parking.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.ui.theme.*

@Composable
fun AdminHomepage(onNavigateToDetail: (Int) -> Unit) {
    val parkingLots = listOf(
        ParkingLot(1, "Bãi đỗ xe số 1", "Khu vực Phía Bắc - Cổng A", 170, 200, "Busy"),
        ParkingLot(2, "Bãi đỗ xe số 2", "Khu trung tâm - Tòa C1", 42, 100, "Normal"),
        ParkingLot(3, "Bãi đỗ xe số 3", "Khu vực Phía Nam - Ký túc xá", 150, 250, "Normal")
    )

    val totalSlots = parkingLots.sumOf { it.maxCap }
    val availableSlots = totalSlots - parkingLots.sumOf { it.currentLoad }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        val width = maxWidth
        // Quyết định số cột dựa trên độ rộng màn hình
        val columns = if (width < 600.dp) 1 else if (width < 900.dp) 2 else 3
        val horizontalPadding = if (width > 1200.dp) (width - 1200.dp) / 2 + 24.dp else 20.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Hero Stats chiếm toàn bộ chiều ngang
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroMainStatsCard(totalSlots, availableSlots)
            }

            // Tiêu đề danh sách chiếm toàn bộ chiều ngang
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Danh sách Bãi đỗ",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            // Danh sách các Card bãi đỗ (chia theo cột)
            items(parkingLots) { lot ->
                ParkingLotCard(lot = lot, onDetailClick = { onNavigateToDetail(lot.id) })
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun HeroMainStatsCard(totalSlots: Int, availableSlots: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(Icons.Outlined.Security, null, modifier = Modifier.size(150.dp).align(Alignment.TopEnd).offset(30.dp, (-30).dp), tint = Color.White.copy(0.1f))
            Column(modifier = Modifier.padding(24.dp)) {
                Text("TRẠNG THÁI TỔNG THỂ", color = Color.White.copy(0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Hoạt động ổn định", color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatItem("$totalSlots", "TỔNG VỊ TRÍ")
                    Spacer(modifier = Modifier.width(32.dp))
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(0.2f)))
                    Spacer(modifier = Modifier.width(32.dp))
                    StatItem("$availableSlots", "CÒN TRỐNG")
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column {
        Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ParkingLotCard(lot: ParkingLot, onDetailClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(BackgroundGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocalParking, null, tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(lot.name, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(lot.location, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { lot.density / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = PrimaryBlue,
                trackColor = SurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onDetailClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("Chi tiết", fontWeight = FontWeight.Bold)
            }
        }
    }
}
