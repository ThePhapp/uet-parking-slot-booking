package com.uet.parking.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.model.UserInfo
import com.uet.parking.data.model.UserWithProfile
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    // 1. Theo dõi dữ liệu từ ViewModel
    val userProfile by viewModel.userProfile.collectAsState()
    
    
    var studentId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var birthdayError by remember { mutableStateOf<String?>(null) }

    fun validatePhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^[0-9]{10}$"))
    }

    fun validateDate(date: String): Boolean {
        return date.matches(Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d$"))
    }

    // 2. Cập nhật state khi dữ liệu từ DB được tải lên thành công
    LaunchedEffect(userProfile) {
        userProfile?.info?.let {
            studentId = it.studentId ?: ""
            phoneNumber = it.phoneNumber ?: ""
            birthday = it.birthday ?: ""
            gender = it.gender ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chỉnh sửa thông tin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundGray)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditField(
                label = "Mã sinh viên",
                value = studentId,
                onValueChange = { studentId = it },
                icon = Icons.Default.Badge
            )

            EditField(
                label = "Số điện thoại",
                value = phoneNumber,
                onValueChange = { 
                    phoneNumber = it
                    phoneNumberError = if (it.isEmpty() || validatePhoneNumber(it)) null else "Số điện thoại không hợp lệ (10 chữ số)"
                },
                icon = Icons.Default.Call,
                isError = phoneNumberError != null,
                errorMessage = phoneNumberError
            )

            EditField(
                label = "Ngày sinh (DD/MM/YYYY)",
                value = birthday,
                onValueChange = { 
                    birthday = it
                    birthdayError = if (it.isEmpty() || validateDate(it)) null else "Ngày sinh không đúng định dạng DD/MM/YYYY"
                },
                icon = Icons.Default.Cake,
                isError = birthdayError != null,
                errorMessage = birthdayError
            )

            EditField(
                label = "Giới tính",
                value = gender,
                onValueChange = { gender = it },
                icon = Icons.Default.Wc
            )

            Spacer(modifier = Modifier.weight(1f))

            // 3. Nút Lưu với logic cập nhật an toàn
            Button(
                onClick = {
                    val isPhoneValid = phoneNumber.isEmpty() || validatePhoneNumber(phoneNumber)
                    val isDateValid = birthday.isEmpty() || validateDate(birthday)
                    
                    if (isPhoneValid && isDateValid) {
                        val updatedInfo = UserInfo(
                            userId = viewModel.userId,
                            studentId = studentId,
                            phoneNumber = phoneNumber,
                            birthday = birthday,
                            gender = gender,
                            debt = userProfile?.info?.debt ?: 0.0
                        )
                        viewModel.updateProfile(updatedInfo)
                        onBackClick() // Quay lại màn hình Profile sau khi lưu
                    } else {
                        if (!isPhoneValid) phoneNumberError = "Số điện thoại không hợp lệ (10 chữ số)"
                        if (!isDateValid) birthdayError = "Ngày sinh không đúng định dạng DD/MM/YYYY"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF434653),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(12.dp),
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFC3C6D5)
            ),
            singleLine = true
        )
    }
}
