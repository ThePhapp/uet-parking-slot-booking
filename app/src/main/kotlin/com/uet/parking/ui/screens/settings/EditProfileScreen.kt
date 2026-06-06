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
import com.uet.parking.utils.ValidationUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    // 1. Theo dõi dữ liệu từ ViewModel
    val userProfile by viewModel.userProfile.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var birthdayError by remember { mutableStateOf<String?>(null) }
    var studentIdError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedGender by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Không cho phép chọn ngày sau ngày hiện tại
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // 2. Cập nhật state khi dữ liệu từ DB được tải lên thành công
    LaunchedEffect(userProfile) {
        userProfile?.user?.let {
            name = it.name ?: ""
        }
        userProfile?.info?.let {
            studentId = it.studentId ?: ""
            phoneNumber = it.phoneNumber ?: ""
            birthday = it.birthday ?: ""
            gender = it.gender ?: ""
        }
    }

    Scaffold { innerPadding ->
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
                label = "Họ và tên",
                value = name,
                onValueChange = { 
                    name = it
                    nameError = if (it.isBlank()) "Không được để trống họ tên" else null
                },
                icon = Icons.Default.Person,
                isError = nameError != null,
                errorMessage = nameError
            )

            if (userProfile?.user?.role == com.uet.parking.data.model.enums.UserRole.USER) {
                EditField(
                    label = "Mã sinh viên",
                    value = studentId,
                    onValueChange = { 
                        if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                            studentId = it
                            studentIdError = if (it.isEmpty()) null else ValidationUtils.validateStudentId(it)
                        }
                    },
                    icon = Icons.Default.Badge,
                    isError = studentIdError != null,
                    errorMessage = studentIdError
                )
            }

            EditField(
                label = "Số điện thoại",
                value = phoneNumber,
                onValueChange = { 
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        phoneNumber = it
                        phoneNumberError = if (it.isEmpty()) null else if (ValidationUtils.validatePhoneNumber(it)) null else "Số điện thoại không hợp lệ (10 chữ số)"
                    }
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
                    birthdayError = if (it.isEmpty()) null else ValidationUtils.validateDate(it)
                },
                icon = Icons.Default.Cake,
                isError = birthdayError != null,
                errorMessage = birthdayError,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Chọn ngày", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                birthday = sdf.format(Date(millis))
                                birthdayError = null
                            }
                            showDatePicker = false
                        }) {
                            Text("Chọn")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Hủy")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Giới tính Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Giới tính",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF434653),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expandedGender,
                    onExpandedChange = { expandedGender = !expandedGender },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                        shape = RoundedCornerShape(12.dp),
                        isError = genderError != null,
                        supportingText = if (genderError != null) { { Text(genderError!!) } } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFC3C6D5)
                        ),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false }
                    ) {
                        listOf("Nam", "Nữ").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    genderError = null
                                    expandedGender = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Nút Lưu với logic cập nhật an toàn
            Button(
                onClick = {
                    val isNameValid = name.isNotBlank()
                    val isPhoneValid = phoneNumber.isNotEmpty() && ValidationUtils.validatePhoneNumber(phoneNumber)
                    val dateError = if (birthday.isEmpty()) null else ValidationUtils.validateDate(birthday)
                    val isDateValid = dateError == null
                    
                    val sIdError = if (userProfile?.user?.role == com.uet.parking.data.model.enums.UserRole.USER) ValidationUtils.validateStudentId(studentId) else null
                    val isSIdValid = sIdError == null
                    
                    val gError = ValidationUtils.validateGender(gender)
                    val isGenderValid = gError == null
                    
                    if (isNameValid && isPhoneValid && isDateValid && isSIdValid && isGenderValid) {
                        val updatedInfo = UserInfo(
                            userId = viewModel.userId,
                            studentId = studentId,
                            phoneNumber = phoneNumber,
                            birthday = birthday,
                            gender = gender,
                            debt = userProfile?.info?.debt ?: 0.0
                        )
                        viewModel.updateProfile(updatedInfo, name)
                        onBackClick() // Quay lại màn hình Profile sau khi lưu
                    } else {
                        if (!isNameValid) nameError = "Không được để trống họ tên"
                        if (!isPhoneValid) phoneNumberError = if (phoneNumber.isEmpty()) "Số điện thoại không được để trống" else "Số điện thoại không hợp lệ (10 chữ số)"
                        if (!isDateValid) birthdayError = dateError
                        if (!isSIdValid) studentIdError = sIdError
                        if (!isGenderValid) genderError = gError
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
    errorMessage: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
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
            trailingIcon = trailingIcon,
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
