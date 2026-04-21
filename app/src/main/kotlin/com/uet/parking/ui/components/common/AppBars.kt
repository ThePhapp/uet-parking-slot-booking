package com.uet.parking.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.PrimaryContainer
import com.uet.parking.ui.theme.PrimaryFixed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.White.copy(alpha = 0.8f)
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            )
        },
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
    )
}

@Composable
fun AppBottomNavigationBar(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit,
    showScan: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(if (showScan) 100.dp else 70.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(70.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 20.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
                horizontalArrangement = if (showScan) Arrangement.SpaceBetween else Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(Icons.Default.LocalParking, "BOOK", selectedIndex == 0) { onItemSelected(0) }
                NavBarItem(Icons.Default.AccountCircle, "SETTINGS", selectedIndex == 1) { onItemSelected(1) }
            }
        }

        if (showScan) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-15).dp)
            ) {
                FloatingActionButton(
                    onClick = { /* Scan QR */ },
                    containerColor = PrimaryFixed,
                    contentColor = PrimaryBlue,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", modifier = Modifier.size(30.dp))
                }
                Text("SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.wrapContentSize()) {
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
}
