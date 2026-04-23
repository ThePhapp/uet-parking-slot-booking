package com.uet.parking.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object BkColor {
    val Primary        = Color(0xFF003D9B)
    val PrimaryVariant = Color(0xFF0052CC)
    val PrimaryFixed   = Color(0xFFDAE2FF)
    val Surface        = Color(0xFFF7F9FB)
    val Card           = Color(0xFFFFFFFF)
    val OnPrimary      = Color(0xFFFFFFFF)
    val TextMain       = Color(0xFF11131F)
    val TextSub        = Color(0xFF434654)
    val TextHint       = Color(0xFF8B90A7)
    val Stroke         = Color(0xFFC3C6D6)
    val SlotNormalBg   = Color(0xFFF2F4F6)
    val SlotSelectedBg = Color(0xFFEEF2FF)
    val Success        = Color(0xFF1DB954)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTopBar(
    onHistoryClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BkColor.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("P", color = BkColor.OnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Campus Parking",
                    color = BkColor.OnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onHistoryClick) {
                Icon(Icons.Default.History, contentDescription = "Lịch sử", tint = BkColor.OnPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BkColor.Primary)
    )
}

@Composable
fun BookingBottomNav(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        Triple("Đặt chỗ",    Icons.Default.LocalParking,            "book"),
        Triple("Vé của tôi", Icons.Outlined.ConfirmationNumber,     "tickets"),
        Triple("Cài đặt",    Icons.Default.Settings,                "settings")
    )
    NavigationBar(containerColor = BkColor.Card) {
        items.forEachIndexed { index, (label, icon, _) ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick  = { onItemSelected(index) },
                icon     = { Icon(icon, contentDescription = label) },
                label    = { Text(label, fontSize = 11.sp) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = BkColor.Primary,
                    selectedTextColor   = BkColor.Primary,
                    unselectedIconColor = BkColor.TextHint,
                    unselectedTextColor = BkColor.TextHint,
                    indicatorColor      = BkColor.PrimaryFixed
                )
            )
        }
    }
}

@Composable
fun ProgressSection(
    stepLabel:   String,
    stepTitle:   String,
    stepCounter: String,
    progress:    Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BkColor.Primary)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stepLabel, color = BkColor.OnPrimary.copy(alpha = 0.7f), fontSize = 11.sp)
                Text(stepTitle, color = BkColor.OnPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Text(stepCounter, color = BkColor.OnPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color            = BkColor.OnPrimary,
            trackColor       = BkColor.OnPrimary.copy(alpha = 0.25f)
        )
    }
}
