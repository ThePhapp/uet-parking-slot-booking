package com.uet.parking.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.User
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue

@Composable
fun SettingsScreen(userId: Int, onBackClick: () -> Unit = {}, onLogoutClick: () -> Unit = {}) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val user by database.userDao().getUserById(userId).collectAsState(initial = null)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        val maxWidth = maxWidth
        val horizontalPadding = if (maxWidth > 800.dp) (maxWidth - 800.dp) / 2 + 24.dp else 24.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Profile Section
            item {
                ProfileSection(user)
            }

            // Settings Groups
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsGroupCard {
                        SettingsItem(icon = Icons.Default.Person, title = "Thông tin cá nhân")
                        SettingsItem(icon = Icons.Default.Lock, title = "Đổi mật khẩu")
                        SettingsItem(icon = Icons.Default.CreditCard, title = "Phương thức thanh toán")
                    }

                    SettingsGroupCard {
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            title = "Thông báo",
                            trailing = { Switch(checked = true, onCheckedChange = {}) }
                        )
                        SettingsItem(
                            icon = Icons.Default.Language,
                            title = "Ngôn ngữ",
                            subtitle = "Tiếng Việt",
                            trailing = { Icon(Icons.Default.ExpandMore, null, tint = Color.Gray) }
                        )
                    }

                    SettingsGroupCard {
                        SettingsItem(
                            icon = Icons.Default.Help,
                            title = "Trung tâm trợ giúp",
                            trailing = { Icon(Icons.Default.OpenInNew, null, tint = Color.Gray) }
                        )
                        SettingsItem(
                            icon = Icons.Default.Logout,
                            title = "Đăng xuất",
                            titleColor = Color(0xFFBA1A1A),
                            showChevron = false,
                            onClick = onLogoutClick
                        )
                    }
                }
            }

            // Footer
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PHIÊN BẢN 2.4.0 (BUILD 89)", style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 2.sp)
                    Text("CAMPUS PARKING MANAGEMENT SYSTEM", style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.5f), letterSpacing = 1.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ProfileSection(user: User?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize().padding(16.dp), tint = Color.Gray)
            }
            Surface(
                modifier = Modifier.size(32.dp).offset(x = (-4).dp, y = (-4).dp),
                shape = CircleShape,
                color = PrimaryBlue,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(user?.name ?: "Người dùng", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
        Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        content = content
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null, titleColor: Color = Color.Unspecified, showChevron: Boolean = true, trailing: @Composable (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = if (titleColor == Color(0xFFBA1A1A)) Color(0xFFFFDAD6).copy(alpha = 0.3f) else Color(0xFFE0E7FF)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (titleColor == Color(0xFFBA1A1A)) Color(0xFFBA1A1A) else PrimaryBlue)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = titleColor)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = PrimaryBlue, fontWeight = FontWeight.Medium)
        }
        if (trailing != null) {
            trailing()
        } else if (showChevron) {
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
