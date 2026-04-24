package com.uet.parking.ui.screens.booking

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay

@Composable
fun SearchingScreen(
    date: String = "",
    startTime: String = "",
    endTime: String = "",
    onNavigateToSuccess: () -> Unit,
    onHistoryClick: () -> Unit = {},
    onNavItemSelected: (Int) -> Unit = {}
) {
    // Tự động chuyển sang màn hình thành công sau 6 giây
    LaunchedEffect(Unit) {
        delay(6000L)
        onNavigateToSuccess()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "searching")
    val rotation by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 360f,
        animationSpec  = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label          = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bước 2 / 3", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("Đang tìm chỗ đậu xe…", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Text("2 / 3", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.66f },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = androidx.compose.ui.graphics.Color.White,
                    trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f)
                )
            }

            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(200.dp).scale(pulseScale).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.06f)))
                Box(modifier = Modifier.size(150.dp).scale(pulseScale).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.10f)))
                CircularProgressIndicator(
                    modifier = Modifier.size(90.dp).graphicsLayer { rotationZ = rotation },
                    color       = PrimaryBlue,
                    strokeWidth = 6.dp
                )
            }

            Spacer(Modifier.height(32.dp))

            Text("Đang phân tích dữ liệu bãi đỗ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            if (date.isNotEmpty()) {
                Text("Ngày đặt: $date ($startTime - $endTime)", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray, modifier = Modifier.padding(top = 8.dp))
            } else {
                Text("Vui lòng đợi trong giây lát...", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.weight(1.5f))
        }
    }
}
