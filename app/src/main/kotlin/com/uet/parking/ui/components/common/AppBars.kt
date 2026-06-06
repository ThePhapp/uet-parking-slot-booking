package com.uet.parking.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.uet.parking.R
import com.uet.parking.data.model.Notification
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onHomeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    containerColor: Color = Color.White.copy(alpha = 0.9f)
) {
    var userMenuExpanded by remember { mutableStateOf(false) }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // User Profile Icon
                Box {
                    IconButton(onClick = { userMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = userMenuExpanded,
                        onDismissRequest = { userMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cài đặt") },
                            onClick = {
                                userMenuExpanded = false
                                onSettingsClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Đăng xuất") },
                            onClick = {
                                userMenuExpanded = false
                                onLogoutClick()
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                        )
                    }
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
    enabled: Boolean = true,
    onAdminQrClick: (String) -> Unit = {}
) {
    var showQrOptions by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp), 
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                NavBarItem(
                    icon = Icons.Default.Home,
                    label = "HOME",
                    isSelected = selectedIndex == 0,
                    enabled = enabled
                ) { onItemSelected(0) }
            }

            if (isAdmin) {
                // Admin: Nút QR SCAN nổi bật ở giữa với hình vuông bo góc
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ProminentNavBarItem(
                        icon = Icons.Default.QrCodeScanner,
                        label = "QR SCAN",
                        isSelected = selectedIndex == 1,
                        enabled = enabled
                    ) { showQrOptions = true }

                    DropdownMenu(
                        expanded = showQrOptions,
                        onDismissRequest = { showQrOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Check-in") },
                            onClick = {
                                showQrOptions = false
                                onAdminQrClick("checkin")
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Check-out") },
                            onClick = {
                                showQrOptions = false
                                onAdminQrClick("checkout")
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                        )
                    }
                }
            } else {
                // User: Booking
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    NavBarItem(
                        icon = Icons.Default.LocalParking,
                        label = "BOOKING",
                        isSelected = selectedIndex == 1,
                        enabled = enabled
                    ) { onItemSelected(1) }
                }
                
                // User: Tickets
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    NavBarItem(
                        icon = Icons.Default.ConfirmationNumber,
                        label = "TICKETS",
                        isSelected = selectedIndex == 2,
                        enabled = enabled
                    ) { onItemSelected(2) }
                }
            }
            
            // Settings
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                NavBarItem(
                    icon = Icons.Default.Settings,
                    label = "SETTINGS",
                    isSelected = (isAdmin && selectedIndex == 2) || (!isAdmin && selectedIndex == 3),
                    enabled = enabled
                ) { onItemSelected(if (isAdmin) 2 else 3) }
            }
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) PrimaryBlue else if (!enabled) Color.LightGray.copy(alpha = 0.5f) else Color.LightGray,
            modifier = Modifier.size(26.dp)
        )
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) PrimaryBlue else Color.LightGray
        )
    }
}

@Composable
private fun ProminentNavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp) // Hình vuông bo góc
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Surface(
            shape = shape,
            color = if (isSelected) PrimaryBlue else PrimaryBlue.copy(alpha = 0.12f),
            shadowElevation = if (isSelected) 8.dp else 0.dp,
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryBlue
        )
    }
}
