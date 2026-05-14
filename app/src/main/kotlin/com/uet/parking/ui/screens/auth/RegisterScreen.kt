package com.uet.parking.ui.screens.auth

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.User
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.OnSurfaceVariant
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
    ) {
        val screenWidth = maxWidth
        val isTablet = screenWidth > 800.dp
        
        // Background Decoration
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .size(400.dp)
                .background(PrimaryBlue.copy(alpha = 0.05f), CircleShape)
                .blur(120.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isTablet) 48.dp else 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader()
            
            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.widthIn(max = 448.dp).fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(if (isTablet) 40.dp else 24.dp)) {
                    Text(
                        text = "Đăng ký tài khoản",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Sử dụng email sinh viên để đăng ký",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    AuthTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorText = "" },
                        label = "HỌ VÀ TÊN",
                        placeholder = "Nguyễn Văn A",
                        icon = Icons.Default.Badge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; errorText = "" },
                        label = "EMAIL",
                        placeholder = "example@vnu.edu.vn",
                        icon = Icons.Default.Person
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; errorText = "" },
                        label = "MẬT KHẨU",
                        placeholder = "••••••••",
                        icon = Icons.Default.Lock,
                        isPass = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorText = "" },
                        label = "NHẬP LẠI MẬT KHẨU",
                        placeholder = "••••••••",
                        icon = Icons.Default.LockReset,
                        isPass = true
                    )
                    
                    if (errorText.isNotEmpty()) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 16.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            val trimmedEmail = email.trim()
                            val trimmedName = fullName.trim()
                            if (trimmedEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || trimmedName.isEmpty()) {
                                errorText = "Vui lòng điền đầy đủ thông tin"
                                return@Button
                            }
                            if (!trimmedEmail.endsWith("@vnu.edu.vn")) {
                                errorText = "Email phải có đuôi @vnu.edu.vn"
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorText = "Mật khẩu nhập lại không khớp"
                                return@Button
                            }
                            scope.launch {
                                try {
                                    val existingUser = database.userDao().getUserByEmail(trimmedEmail)
                                    if (existingUser != null) {
                                        errorText = "Email này đã được đăng ký"
                                    } else {
                                        val newUser = User(
                                            email = trimmedEmail,
                                            password = password,
                                            name = trimmedName,
                                            role = UserRole.USER,
                                            debt = 0.0
                                        )
                                        database.userDao().insertUser(newUser)
                                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                        onRegisterSuccess()
                                    }
                                } catch (e: Exception) {
                                    errorText = "Lỗi CSDL: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đã có tài khoản?", fontSize = 14.sp, color = OnSurfaceVariant)
                Text(
                    text = " Đăng nhập ngay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            FooterLegal()
        }
    }
}
