package com.uet.parking.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.model.UserWithProfile
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thông tin cá nhân", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundGray),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                ProfileHeader(userProfile)
            }

            item {
                val infoItems = mutableListOf<InfoItemData>()
                if (userProfile?.user?.role == com.uet.parking.data.model.enums.UserRole.USER) {
                    infoItems.add(InfoItemData(Icons.Default.Badge, "Mã sinh viên", userProfile?.info?.studentId ?: "Chưa cập nhật"))
                }
                infoItems.addAll(listOf(
                    InfoItemData(Icons.Default.Call, "Số điện thoại", userProfile?.info?.phoneNumber ?: "Chưa cập nhật"),
                    InfoItemData(Icons.Default.Cake, "Ngày sinh", userProfile?.info?.birthday ?: "Chưa cập nhật"),
                    InfoItemData(Icons.Default.Wc, "Giới tính", userProfile?.info?.gender ?: "Chưa cập nhật")
                ))

                InfoCard(
                    title = "THÔNG TIN CHI TIẾT",
                    items = infoItems
                )
            }

            item {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Chỉnh sửa thông tin", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(userProfile: UserWithProfile?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5EEFF))
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userProfile?.user?.name ?: "Người dùng",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B1C30)
        )
        Text(
            text = userProfile?.user?.email ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF434653)
        )
    }
}

@Composable
fun InfoCard(title: String, items: List<InfoItemData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            items.forEachIndexed { index, item ->
                InfoRow(item)
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = Color(0xFFC3C6D5).copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

data class InfoItemData(val icon: ImageVector, val label: String, val value: String)

@Composable
fun InfoRow(data: InfoItemData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFE5EEFF)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(data.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(data.label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF434653))
            Text(data.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0B1C30))
        }
    }
}
