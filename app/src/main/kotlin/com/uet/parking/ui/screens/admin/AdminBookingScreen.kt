package com.uet.parking.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.BookingEntity
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.enums.BookingStatus
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.viewmodel.AdminBookingViewModel
import com.uet.parking.ui.viewmodel.ViewModelFactory

@Composable
fun AdminBookingScreen(userId: Int, onNavigateToQrScan: () -> Unit = {}) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        ParkingRepository(
            database.userDao(),
            database.ticketDao(),
            database.parkingLotDao(),
            database.hourlyLoadDao(),
            database.userInfoDao(),
            database.adminInfoDao(),
            database.bookingDao()
        )
    }

    val viewModel: AdminBookingViewModel = viewModel(
        factory = ViewModelFactory(repository, userId)
    )

    val allBookings by viewModel.allBookings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tabs: Tất cả, Chờ duyệt, Đã duyệt, Đã check-in, Đã từ chối
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tất cả", "Chờ duyệt", "Đã duyệt", "Checked In", "Đã từ chối")

    val filteredBookings = when (selectedTab) {
        1 -> allBookings.filter { it.status == BookingStatus.PENDING }
        2 -> allBookings.filter { it.status == BookingStatus.APPROVED }
        3 -> allBookings.filter { it.status == BookingStatus.CHECKED_IN }
        4 -> allBookings.filter { it.status == BookingStatus.REJECTED }
        else -> allBookings
    }

    // Show messages
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.successMessage)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header stats
            AdminBookingHeader(
                total = allBookings.size,
                pending = allBookings.count { it.status == BookingStatus.PENDING },
                approved = allBookings.count { it.status == BookingStatus.APPROVED },
                rejected = allBookings.count { it.status == BookingStatus.REJECTED },
                checkedIn = allBookings.count { it.status == BookingStatus.CHECKED_IN }
            )

            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = when (index) {
                        1 -> allBookings.count { it.status == BookingStatus.PENDING }
                        2 -> allBookings.count { it.status == BookingStatus.APPROVED }
                        3 -> allBookings.count { it.status == BookingStatus.CHECKED_IN }
                        4 -> allBookings.count { it.status == BookingStatus.REJECTED }
                        else -> allBookings.size
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (count > 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = if (selectedTab == index) PrimaryBlue else Color.Gray.copy(0.2f),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            "$count",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == index) Color.White else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Booking list
            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Không có booking nào",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings, key = { it.id }) { booking ->
                        AdminBookingCard(
                            booking = booking,
                            database = database,
                            onApprove = { viewModel.approveBooking(booking.id) },
                            onReject = { viewModel.rejectBooking(booking.id) },
                            onDelete = { viewModel.deleteBooking(booking.id) }
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Floating Action Button - Scan QR
        FloatingActionButton(
            onClick = onNavigateToQrScan,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = PrimaryBlue,
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Quét QR Code",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AdminBookingHeader(total: Int, pending: Int, approved: Int, rejected: Int, checkedIn: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "QUẢN LÝ ĐẶT SÂN",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "$total Booking",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("Chờ duyệt", pending, Color(0xFFFFA726))
                StatChip("Đã duyệt", approved, Color(0xFF66BB6A))
                StatChip("Đã nhận", checkedIn, Color(0xFF42A5F5))
                StatChip("Từ chối", rejected, Color(0xFFEF5350))
            }
        }
    }
}

@Composable
private fun StatChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$count",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = Color.White.copy(0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun AdminBookingCard(
    booking: BookingEntity,
    database: AppDatabase,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var parkingLot by remember { mutableStateOf<ParkingLot?>(null) }
    var userName by remember { mutableStateOf("Đang tải...") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(booking.fieldId) {
        parkingLot = database.parkingLotDao().getParkingLotById(booking.fieldId)
    }

    LaunchedEffect(booking.userId) {
        val user = database.userDao().getUserByIdSuspend(booking.userId)
        userName = user?.name ?: user?.email ?: "User #${booking.userId}"
    }

    val statusColor = when (booking.status) {
        BookingStatus.PENDING -> Color(0xFFFFA726)
        BookingStatus.APPROVED -> Color(0xFF66BB6A)
        BookingStatus.CHECKED_IN -> Color(0xFF42A5F5)
        BookingStatus.REJECTED -> Color(0xFFEF5350)
    }

    val statusBg = when (booking.status) {
        BookingStatus.PENDING -> Color(0xFFFFF3E0)
        BookingStatus.APPROVED -> Color(0xFFE8F5E9)
        BookingStatus.CHECKED_IN -> Color(0xFFE3F2FD)
        BookingStatus.REJECTED -> Color(0xFFFDECEA)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Booking #${booking.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryBlue
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        userName,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // Status badge
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        booking.status.value,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Quick info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(Icons.Default.CalendarToday, booking.bookingDate)
                InfoChip(Icons.Default.AccessTime, "Ca ${booking.slot}")
                InfoChip(Icons.Default.Schedule, booking.bookingTime)
            }

            // Expandable detail
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Color.LightGray.copy(0.5f))
                    Spacer(Modifier.height(12.dp))

                    DetailInfoRow("Sân", parkingLot?.name ?: "Sân #${booking.fieldId}")
                    DetailInfoRow("Địa chỉ", parkingLot?.address ?: "---")
                    DetailInfoRow("Ngày tạo", booking.createdAt)
                    DetailInfoRow("Mã booking", "#${booking.id}")

                    // Action buttons (chỉ hiện khi PENDING)
                    if (booking.status == BookingStatus.PENDING) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Approve button
                            Button(
                                onClick = onApprove,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Duyệt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Reject button
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF5350)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350))
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Từ chối", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Delete button (luôn hiện)
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Xóa booking", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa booking #${booking.id}? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
                ) {
                    Text("Xóa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = PrimaryBlue)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF434654))
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF11131F))
    }
}
