package com.uet.parking.ui.screens.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.viewmodel.PaymentState
import com.uet.parking.ui.viewmodel.PaymentViewModel
import com.uet.parking.ui.viewmodel.toVndText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VnpayBlue = Color(0xFF0A1F44)
private val VnpayRed = Color(0xFFEF1724)
private val VnpayGreen = Color(0xFF00A651)

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    onBackHome: () -> Unit
) {
    val userWithProfile by viewModel.userProfile.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val transactionId by viewModel.transactionId.collectAsState()
    val paidAmount by viewModel.paidAmount.collectAsState()

    val debt = userWithProfile?.info?.debt ?: 0.0
    val qrUrl = remember(debt) {
        if (debt > 0) viewModel.buildVnpayQrData(debt) else ""
    }

    when (paymentState) {
        PaymentState.PROCESSING -> ProcessingScreen()
        PaymentState.SUCCESS -> SuccessScreen(
            amount = paidAmount,
            transactionId = transactionId ?: "",
            onDone = {
                viewModel.resetPayment()
                onBackHome()
            }
        )
        PaymentState.ERROR -> ErrorScreen(onRetry = { viewModel.resetPayment() })
        PaymentState.IDLE -> {
            if (debt <= 0.0) {
                NoDebtScreen(onBackHome = onBackHome)
            } else {
                PaymentQrScreen(
                    debt = debt,
                    qrUrl = qrUrl,
                    userName = userWithProfile?.user?.name ?: "Sinh viên",
                    userId = userWithProfile?.user?.userId ?: "",
                    onConfirm = { viewModel.confirmPayment(debt) }
                )
            }
        }
    }
}

@Composable
private fun PaymentQrScreen(
    debt: Double,
    qrUrl: String,
    userName: String,
    userId: String,
    onConfirm: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header VNPAY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(VnpayBlue, Color(0xFF1A3A6C))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = VnpayRed
                    )
                    Text(
                        text = "PAY",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "SANDBOX",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cổng thanh toán trực tuyến",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Chi tiết giao dịch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CHI TIẾT GIAO DỊCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                TransactionRow("Đơn vị thụ hưởng", "ĐH Công nghệ - ĐHQGHN")
                TransactionRow("Dịch vụ", "Phí gửi xe Campus Parking")
                TransactionRow("Người thanh toán", userName)
                TransactionRow("Mã sinh viên", userId.takeLast(8).uppercase())

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = debt.toVndText(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = VnpayRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // QR Code
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QUÉT MÃ QR ĐỂ THANH TOÁN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(2.dp, PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "VNPAY Payment QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Security,
                        null,
                        tint = VnpayGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Được bảo mật bởi VNPAY",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút xác nhận
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VnpayBlue)
        ) {
            Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Xác nhận đã thanh toán",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Môi trường Sandbox: Giao dịch không trừ tiền thật",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun TransactionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
    }
}

@Composable
private fun ProcessingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VnpayBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.scale(pulse)) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(rotation),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Đang xử lý giao dịch...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vui lòng không tắt ứng dụng",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("VN", fontSize = 18.sp, fontWeight = FontWeight.Black, color = VnpayRed)
                Text("PAY", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

@Composable
private fun SuccessScreen(
    amount: Double,
    transactionId: String,
    onDone: () -> Unit
) {
    val scaleAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    val timeString = remember {
        SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(VnpayGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = VnpayGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Thanh toán thành công!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = VnpayGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Giao dịch đã được xác nhận",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Thông tin giao dịch
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8F9FC)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ReceiptRow("Số tiền", amount.toVndText())
                        ReceiptRow("Mã giao dịch", transactionId)
                        ReceiptRow("Thời gian", timeString)
                        ReceiptRow("Phương thức", "VNPAY QR")
                        ReceiptRow("Trạng thái", "✅ Thành công")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Số dư nợ của bạn đã được cập nhật về 0₫",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        "Về trang chủ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
    }
}

@Composable
private fun ErrorScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("❌", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Giao dịch thất bại",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Đã có lỗi xảy ra. Vui lòng thử lại.",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Thử lại", color = Color.White)
            }
        }
    }
}

@Composable
private fun NoDebtScreen(onBackHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bạn không có khoản nợ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tài khoản của bạn hiện không có dư nợ cần thanh toán",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackHome,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Về trang chủ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}