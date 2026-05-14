package com.uet.parking.ui.screens.admin

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.theme.*
import com.uet.parking.ui.viewmodel.AdminViewModel
import com.uet.parking.ui.viewmodel.ViewModelFactory

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AdminHomepage(
    userId: Int,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        ParkingRepository(
            database.userDao(),
            database.ticketDao(),
            database.parkingLotDao(),
            database.hourlyLoadDao(),
            database.userInfoDao(),
            database.adminInfoDao()
        )
    }

    val viewModel: AdminViewModel = viewModel(
        factory = ViewModelFactory(repository, userId)
    )

    val parkingLots by viewModel.parkingLots.collectAsState()
    val adminProfile by viewModel.adminProfile.collectAsState()
    val totalSlots by viewModel.totalSlots.collectAsState()
    val availableSlots by viewModel.availableSlots.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        val width = maxWidth
        val columns = 3
        val horizontalPadding = if (width > 1200.dp) (width - 1200.dp) / 2 + 16.dp else 12.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroMainStatsCard(totalSlots, availableSlots)
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickActionsCard(
                    onManageLotClick = {
                        adminProfile?.adminInfo?.parkingId?.let { id ->
                            onNavigateToDetail(id)
                        }
                    }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Danh sách Bãi đỗ",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            items(parkingLots) { lot ->
                ParkingLotCard(lot = lot, onDetailClick = { onNavigateToDetail(lot.parkingId ?: 0) })
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun QuickActionsCard(onManageLotClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "TIỆN ÍCH QUẢN TRỊ",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Settings,
                    label = "Quản lý bãi đỗ",
                    modifier = Modifier.weight(1f),
                    onClick = onManageLotClick
                )
                QuickActionButton(
                    icon = Icons.Default.ConfirmationNumber,
                    label = "Đặt vé ngoài",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.BarChart,
                    label = "Thống kê",
                    modifier = Modifier.weight(1f),
                    isPlaceholder = true
                )
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    label = "Báo cáo",
                    modifier = Modifier.weight(1f),
                    isPlaceholder = true
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isPlaceholder: Boolean = false,
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isPlaceholder) Color.LightGray else PrimaryBlue
        ),
        border = BorderStroke(
            1.dp,
            if (isPlaceholder) Color.LightGray.copy(alpha = 0.3f) else PrimaryBlue.copy(alpha = 0.15f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Text(
                label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                lineHeight = 10.sp
            )
        }
    }
}

@Composable
fun HeroMainStatsCard(totalSlots: Int, availableSlots: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(Icons.Outlined.Security, null, modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(20.dp, (-20).dp), tint = Color.White.copy(0.1f))
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TRẠNG THÁI TỔNG THỂ", color = Color.White.copy(0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Hoạt động ổn định", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatItem("$totalSlots", "TỔNG VỊ TRÍ")
                    Spacer(modifier = Modifier.width(24.dp))
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(0.2f)))
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
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ParkingLotCard(lot: ParkingLot, onDetailClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(BackgroundGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocalParking, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(lot.name ?: "", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (lot.density / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = PrimaryBlue,
                trackColor = SurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { onDetailClick(lot.parkingId ?: 0) },
                modifier = Modifier.fillMaxWidth().height(30.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Xem", fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
}