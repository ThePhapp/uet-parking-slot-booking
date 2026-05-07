package com.uet.parking.ui.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import com.uet.parking.ui.viewmodel.HomeViewModel

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
    onSettingsClick: () -> Unit = {}
) {
    var showPaymentResult by remember { mutableStateOf(false) }
    var isPaymentSuccess by remember { mutableStateOf(false) }

    val mockStudentBalance = 0.0 // Giả lập số dư ví
    val userWithProfile by viewModel.userProfile.collectAsState()

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
            // Nút Đặt xe
            item {
                Button(
                    onClick = onBookNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Đặt xe",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Thẻ Nợ (DebtCard)
            item {
                userWithProfile?.let { data ->
                    val user = data.user
                    val info = data.info
                    Log.d("DEBUG_HOME", "User ID: ${user.userId}")
                    Log.d("DEBUG_HOME", "UserInfo Object: $info")
                    if (info != null) {
                        Log.d("DEBUG_HOME", "Debt value từ DB: ${info.debt}")
                    } else {
                        Log.e("DEBUG_HOME", "UserInfo bị NULL! Kiểm tra lại bảng user_info trong DB")
                    }
                    // SỬA: Đổi dept thành debt và mặc định là 0.0
                    val rawDebt = info?.debt ?: 10000.0

                    val formattedDebt = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                        .format(rawDebt)

                    // Email là non-null nên không cần safe call
                    val studentCode = user.email.substringBefore("@").uppercase()

                    DebtCard(
                        debt = formattedDebt,
                        cardType = "Sinh Viên",
                        studentCode = studentCode,
                        onPaymentClick = {
                            isPaymentSuccess = mockStudentBalance >= rawDebt
                            showPaymentResult = true
                        }
                    )
                }
            }

            // Tiêu đề sự kiện
            item { HomeSectionHeader() }

            // Danh sách sự kiện
            items(events) { event ->
                EventCard(event = event)
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        if (showPaymentResult) {
            val rawDebt = userWithProfile?.info?.debt ?: 0.0
            AlertDialog(
                onDismissRequest = { showPaymentResult = false },
                confirmButton = {
                    TextButton(onClick = { showPaymentResult = false }) {
                        Text("Đóng")
                    }
                },
                title = { Text(if (isPaymentSuccess) "Thanh toán thành công" else "Thanh toán thất bại") },
                text = {
                    Text(
                        if (isPaymentSuccess) "Bạn đã thanh toán khoản nợ ${NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(rawDebt)}."
                        else "Số dư tài khoản không đủ để thanh toán khoản nợ ${NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(rawDebt)}."
                    )
                }
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
            Text(
                text = "THÔNG BÁO MỚI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                letterSpacing = 1.sp
            )
            Text(
                text = "Sự kiện trường học",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        }

        Text(
            text = "Xem tất cả",
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
