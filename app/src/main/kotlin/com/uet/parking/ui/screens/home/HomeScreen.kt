package com.uet.parking.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.components.DebtCard
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBookNow: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    onStudyScheduleClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val userWithProfile by viewModel.userProfile.collectAsState()
    val paymentUiState by viewModel.paymentUiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()
    
    val schedules by viewModel.studySchedules.collectAsState()
    val globalNotifications by viewModel.globalNotifications.collectAsState()

    val weeklySchedules = schedules
        .filter { schedule ->
            schedule.dayOfWeek in 2..7 &&
                    schedule.startHour in 1..4
        }
        .distinctBy { schedule ->
            "${schedule.dayOfWeek}_${schedule.startHour}_${schedule.subjectName}_${schedule.room}"
        }
        .sortedWith(
            compareBy({ it.dayOfWeek }, { it.startHour })
        )

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshData()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                userWithProfile?.let { data ->
                    val user = data.user
                    val info = data.info
                    
                    val rawDebt = info?.let {
                        if (it.debt != 0.0) it.debt else 0.0
                    } ?: 0.0

                    val formattedDebt = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                        .format(rawDebt)

                    val studentCode = user.email.substringBefore("@").uppercase()

                    DebtCard(
                        debt = formattedDebt,
                        cardType = "Sinh Viên",
                        studentCode = studentCode,
                        onPaymentClick = {
                            if (rawDebt > 0.0) {
                                onPaymentClick()
                            } else {
                                viewModel.payDebt(rawDebt)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "TIỆN ÍCH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clickable { onBookNow() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🚗", fontSize = 24.sp)
                                Text("Đặt xe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clickable { onStudyScheduleClick() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📅", fontSize = 24.sp)
                                Text("Lịch học", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clickable {
                                    if (rawDebt > 0.0) onPaymentClick()
                                    else viewModel.payDebt(rawDebt)
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("💳", fontSize = 24.sp)
                                Text("Thanh toán", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item { HomeSectionHeader() }

            // --- Thông báo từ Admin ---
            if (globalNotifications.isNotEmpty()) {
                items(globalNotifications.take(5)) { notif ->
                    AdminNotificationCard(notification = notif)
                }
            }

            // --- Lịch học ---
            if (weeklySchedules.isEmpty()) {
                item {
                    EmptyScheduleNotificationCard(
                        onStudyScheduleClick = onStudyScheduleClick
                    )
                }
            } else {
                items(weeklySchedules) { schedule ->
                    ScheduleNotificationCard(
                        schedule = schedule,
                        onBookNow = onBookNow
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )

        if (paymentUiState.showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissPaymentDialog() },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissPaymentDialog() }) {
                        Text("Đóng")
                    }
                },
                title = { Text(if (paymentUiState.isSuccess) "Thanh toán thành công" else "Thanh toán thất bại") },
                text = { Text(paymentUiState.message) }
            )
        }
    }
}

@Composable
private fun HomeSectionHeader() {
    Column {
        Text(
            text = "THÔNG BÁO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            letterSpacing = 1.sp
        )

        Text(
            text = "Thông báo",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Các lịch học của bạn. Đặt vé xe trước khi đến trường.",
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun ScheduleNotificationCard(
    schedule: com.uet.parking.data.model.StudySchedule,
    onBookNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Bạn có lịch học",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = schedule.subjectName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Phòng: ${schedule.room}",
                fontSize = 14.sp,
                color = Color(0xFF737685)
            )

            Text(
                text = "${schedule.dayOfWeek.toVietnameseDay()} • Ca ${schedule.startHour} • ${schedule.startHour.toShiftTime()}",
                fontSize = 14.sp,
                color = Color(0xFF737685)
            )

            if (schedule.teacherName.isNotBlank()) {
                Text(
                    text = "Giảng viên: ${schedule.teacherName}",
                    fontSize = 14.sp,
                    color = Color(0xFF737685)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBookNow,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Đặt vé xe",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyScheduleNotificationCard(
    onStudyScheduleClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bạn chưa có lịch học nào.",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hãy tạo lịch học để hệ thống hiển thị thông báo đặt vé xe.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onStudyScheduleClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Tạo lịch học")
            }
        }
    }
}

private fun Int.toVietnameseDay(): String {
    return when (this) {
        2 -> "Thứ 2"
        3 -> "Thứ 3"
        4 -> "Thứ 4"
        5 -> "Thứ 5"
        6 -> "Thứ 6"
        7 -> "Thứ 7"
        8 -> "Chủ nhật"
        else -> "Không rõ ngày"
    }
}

private fun Int.toShiftTime(): String {
    return when (this) {
        1 -> "7h - 9h40"
        2 -> "9h50 - 12h30"
        3 -> "13h30 - 16h10"
        4 -> "16h20 - 19h"
        else -> "Không rõ ca"
    }
}

@Composable
private fun AdminNotificationCard(notification: com.uet.parking.data.model.Notification) {
    val typeColor = when (notification.type.uppercase()) {
        "WARNING" -> Color(0xFFF57C00)
        "ERROR" -> Color(0xFFD32F2F)
        "SUCCESS" -> Color(0xFF388E3C)
        else -> PrimaryBlue
    }
    val typeIcon = when (notification.type.uppercase()) {
        "WARNING" -> "⚠️"
        "ERROR" -> "🚫"
        "SUCCESS" -> "✅"
        else -> "📢"
    }

    val timeText = try {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        sdf.format(notification.timestamp.toDate())
    } catch (_: Exception) { "" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(typeIcon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thông báo từ Quản trị",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
                Text(
                    text = timeText,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = Color(0xFF737685)
            )
        }
    }
}
