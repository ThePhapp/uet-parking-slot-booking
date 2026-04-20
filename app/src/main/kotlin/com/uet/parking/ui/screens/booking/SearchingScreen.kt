package com.uet.parking.ui.screens.booking

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

@Composable
fun SearchingScreen(
    date:                String,
    startTime:           String,
    endTime:             String,
    onNavigateToSuccess: () -> Unit,
    onHistoryClick:      () -> Unit,
    onNavItemSelected:   (Int) -> Unit
) {
    // Auto navigate after 3s
    LaunchedEffect(Unit) {
        delay(3000L)
        onNavigateToSuccess()
    }

    // Rotation animation for spinner
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
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue  = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1200
                0.2f at 0; 1f at 300; 0.2f at 600; 0.2f at 1200
            },
            RepeatMode.Restart
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue  = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1200
                0.2f at 0; 0.2f at 200; 1f at 500; 0.2f at 800; 0.2f at 1200
            },
            RepeatMode.Restart
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue  = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1200
                0.2f at 0; 0.2f at 400; 1f at 700; 0.2f at 1000; 0.2f at 1200
            },
            RepeatMode.Restart
        ),
        label = "dot3"
    )

    Scaffold(
        topBar    = { BookingTopBar(onHistoryClick = onHistoryClick) },
        bottomBar = { BookingBottomNav(selectedIndex = 0, onItemSelected = onNavItemSelected) },
        containerColor = BkColor.Surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressSection(
                stepLabel   = "Bước 2 / 3",
                stepTitle   = "Đang tìm chỗ đậu xe…",
                stepCounter = "2 / 3",
                progress    = 0.66f
            )

            Spacer(Modifier.weight(1f))

            // Pulse rings + spinner
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(BkColor.Primary.copy(alpha = 0.06f))
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(BkColor.Primary.copy(alpha = 0.10f))
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(90.dp)
                        .graphicsLayer { rotationZ = rotation },
                    color       = BkColor.Primary,
                    strokeWidth = 6.dp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Đang tìm chỗ đậu xe",
                fontSize   = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color      = BkColor.TextMain
            )
            Spacer(Modifier.height(6.dp))

            // Animated dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BkColor.Primary.copy(alpha = alpha))
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Info bento cards
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoBentoCard("📍 Khu vực", "Khu A & B", Modifier.weight(1f))
                InfoBentoCard("⏱ Thời gian", "< 2 phút", Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun InfoBentoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = BkColor.Card),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = BkColor.TextHint)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BkColor.TextMain)
        }
    }
}
