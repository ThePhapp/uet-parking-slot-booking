package com.uet.parking.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Bảng màu hệ thống ---
val PrimaryBlue = Color(0xFF003D9B)
val PrimaryContainer = Color(0xFF0052CC)
val BackgroundGray = Color(0xFFF7F9FB)
val SurfaceVariant = Color(0xFFE0E3E5)

@Composable
fun BottomNavigationBar() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(70.dp),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(Icons.Default.LocalParking, "BOOK", true)
            NavBarItem(Icons.Default.AccountCircle, "SETTINGS", false)
        }
    }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, isSelected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon, contentDescription = label,
            tint = if (isSelected) PrimaryBlue else Color.LightGray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = if (isSelected) PrimaryBlue else Color.LightGray
        )
    }
}