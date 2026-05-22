package com.uet.parking.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.R
import com.uet.parking.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onHomeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    containerColor: Color = Color.White.copy(alpha = 0.9f)
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onHomeClick() }
            )
        },
        actions = {
            Box(Modifier.padding(end = 8.dp)) {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Avatar",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cài đặt") },
                        onClick = {
                            expanded = false
                            onSettingsClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Settings, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Đăng xuất") },
                        onClick = {
                            expanded = false
                            onLogoutClick()
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
    )
}

@Composable
fun AppBottomNavigationBar(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit,
    isAdmin: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trường Home chung cho cả 2
            NavBarItem(
                icon = Icons.Default.Home,
                label = "HOME",
                isSelected = selectedIndex == 0,
                enabled = enabled
            ) { onItemSelected(0) }

            // Trường Booking chung cho cả 2
            NavBarItem(
                icon = Icons.Default.LocalParking,
                label = "BOOKING",
                isSelected = selectedIndex == 1,
                enabled = enabled
            ) { onItemSelected(1) }
            
            if (!isAdmin) {
                // Chỉ User mới có Tickets
                NavBarItem(
                    icon = Icons.Default.ConfirmationNumber,
                    label = "TICKETS",
                    isSelected = selectedIndex == 2,
                    enabled = enabled
                ) { onItemSelected(2) }
            }
            
            // Trường Settings chung cho cả 2
            NavBarItem(
                icon = Icons.Default.Settings,
                label = "SETTINGS",
                isSelected = isAdmin && selectedIndex == 2 || !isAdmin && selectedIndex == 3,
                enabled = enabled
            ) { onItemSelected(if (isAdmin) 2 else 3) }
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryBlue else if (!enabled) Color.LightGray.copy(alpha = 0.5f) else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) PrimaryBlue else if (!enabled) Color.LightGray.copy(alpha = 0.5f) else Color.LightGray
            )
        }
    }
}
