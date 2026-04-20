package com.uet.parking.ui.screens.booking

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuccessScreen(
    date:              String,
    startTime:         String,
    endTime:           String,
    ticketCode:        String,
    onGoHome:          () -> Unit,
    onHistoryClick:    () -> Unit,
    onNavItemSelected: (Int) -> Unit
) {
    // Pop-in animation for success icon
    val iconScale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    Scaffold(
        topBar    = { BookingTopBar(onHistoryClick = onHistoryClick) },
        bottomBar = { BookingBottomNav(selectedIndex = 1, onItemSelected = onNavItemSelected) },
        containerColor = BkColor.Surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressSection(
                stepLabel   = "Bước 3 / 3",
                stepTitle   = "Đặt chỗ thành công!",
                stepCounter = "3 / 3",
                progress    = 1.0f
            )

            Spacer(Modifier.weight(0.5f))

            // Success icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(iconScale)
                    .clip(CircleShape)
                    .background(BkColor.PrimaryFixed)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(BkColor.Primary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Thành công",
                        tint    = Color(0xFFC4D2FF),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Đặt chỗ thành công!",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = BkColor.TextMain
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Chỗ đậu xe của bạn đã được xác nhận",
                fontSize  = 14.sp,
                color     = BkColor.TextHint,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Info cards
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard("📅 Ngày", date, Modifier.weight(1f))
                InfoCard("⏰ Giờ", "$startTime – $endTime", Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Ticket card
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = BkColor.Card),
                border    = BorderStroke(1.dp, BkColor.PrimaryFixed),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mã vé của bạn", fontSize = 12.sp, color = BkColor.TextHint)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        ticketCode,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = BkColor.Primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = "QR",
                        tint     = BkColor.TextHint,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Home button
            Button(
                onClick   = onGoHome,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BkColor.Primary)
            ) {
                Text(
                    "Về trang chủ",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = BkColor.OnPrimary
                )
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = BkColor.Card),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = BkColor.TextHint)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BkColor.TextMain)
        }
    }
}
