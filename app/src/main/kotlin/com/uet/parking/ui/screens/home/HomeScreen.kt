package com.uet.parking.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.components.DebtCard
import com.uet.parking.ui.components.EventCard
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

data class EventUiModel(
    val title: String,
    val location: String,
    val time: String = "",
    val featured: Boolean = false
)

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

    val events = listOf(
        EventUiModel(
            title = "Hội thảo Công nghệ Blockchain trong Giáo dục 2026",
            location = "Giảng đường A1",
            time = "14:00 - 20/10",
            featured = true
        ),
        EventUiModel(
            title = "Đêm nhạc Acoustic: Giai điệu mùa thu Sinh viên",
            location = "Sân hội trường C"
        ),
        EventUiModel(
            title = "Kỹ năng mềm: Tư duy thiết kế trong khởi nghiệp",
            location = "Phòng 402, Nhà B"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
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

            items(events) { event ->
                EventCard(event = event)
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text("THÔNG BÁO MỚI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, letterSpacing = 1.sp)
            Text("Sự kiện trường học", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        }
        Text("Xem tất cả", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
