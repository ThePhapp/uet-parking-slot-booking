package com.uet.parking.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalUriHandler
import com.uet.parking.data.model.User
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, (String?) -> Unit) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = if (isProcessing) ({}) else onDismiss,
        title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it; error = null },
                    label = { Text("Mật khẩu cũ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isProcessing
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("Mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isProcessing
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Xác nhận mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isProcessing
                )
                if (error != null) {
                    Text(error!!, color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPassword.isBlank() || newPassword.isBlank()) {
                        error = "Vui lòng nhập đầy đủ thông tin"
                    } else if (newPassword != confirmPassword) {
                        error = "Mật khẩu xác nhận không khớp"
                    } else if (newPassword.length < 6) {
                        error = "Mật khẩu phải ít nhất 6 ký tự"
                    } else {
                        isProcessing = true
                        onConfirm(oldPassword, newPassword) { resultError ->
                            isProcessing = false
                            if (resultError != null) {
                                error = resultError
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Xác nhận")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Hủy")
            }
        }
    )
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SettingsScreen(
    userId: String,
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val repository = remember { ParkingRepository(firestore) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    
    LaunchedEffect(userId) {
        user = repository.getUserByIdSuspend(userId)
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { oldPass, newPass, onResult ->
                if (user == null) {
                    onResult("Dữ liệu người dùng chưa tải xong")
                    return@ChangePasswordDialog
                }
                scope.launch {
                    try {
                        if (user?.password == oldPass) {
                            repository.updatePassword(userId, newPass)
                            // Cập nhật lại user cục bộ để khớp với pass mới trong DB
                            user = user?.copy(password = newPass)
                            Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                            onResult(null)
                            showChangePasswordDialog = false
                        } else {
                            onResult("Mật khẩu cũ không chính xác")
                        }
                    } catch (e: Exception) {
                        onResult("Lỗi: ${e.localizedMessage}")
                    }
                }
            }
        )
    }

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
                ProfileSection(user, onEditProfileClick)
            }

            // Settings Groups
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsGroupCard {
                        SettingsItem(
                            icon = Icons.Default.Person,
                            title = "Thông tin cá nhân",
                            onClick = onEditProfileClick
                        )
                        SettingsItem(
                            icon = Icons.Default.Lock, 
                            title = "Đổi mật khẩu",
                            onClick = { showChangePasswordDialog = true }
                        )
                    }

                    SettingsGroupCard {
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            title = "Thông báo",
                            trailing = { 
                                Switch(
                                    checked = notificationsEnabled, 
                                    onCheckedChange = { 
                                        notificationsEnabled = it
                                        val status = if (it) "đã bật" else "đã tắt"
                                        Toast.makeText(context, "Thông báo $status", Toast.LENGTH_SHORT).show()
                                    }
                                ) 
                            }
                        )
                    }

                    SettingsGroupCard {
                        SettingsItem(
                            icon = Icons.Default.Help,
                            title = "Trung tâm trợ giúp",
                            trailing = { Icon(Icons.Default.OpenInNew, null, tint = Color.Gray) },
                            onClick = { uriHandler.openUri("https://uet.vnu.edu.vn/") }
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
fun ProfileSection(user: User?, onEditClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onEditClick)
            .padding(16.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F2F5)),
                contentAlignment = Alignment.Center
            ) {
                // Hiển thị Avatar (sử dụng ảnh mặc định nếu chưa có URL)
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${user?.name ?: "U"}&background=0D47A1&color=fff&size=128",
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Surface(
                modifier = Modifier.size(36.dp).offset(x = (-4).dp, y = (-4).dp),
                shape = CircleShape,
                color = PrimaryBlue,
                shadowElevation = 4.dp,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp), tint = Color.White)
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
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Unspecified,
    showChevron: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (titleColor == Color(0xFFBA1A1A)) Color(0xFFFFDAD6).copy(alpha = 0.3f) else Color(0xFFE0E7FF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = if (titleColor == Color(0xFFBA1A1A)) Color(0xFFBA1A1A) else PrimaryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor
                )
                if (subtitle != null) Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }
            if (trailing != null) {
                trailing()
            } else if (showChevron) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}
