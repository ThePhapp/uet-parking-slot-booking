package com.uet.parking.ui.screens.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.OnSurfaceVariant
import kotlinx.coroutines.launch
import android.util.Log

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AuthScreen(onLoginSuccess: (Int, String) -> Unit = { _, _ -> }) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
        
        // Floating Background
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .size(400.dp)
                .background(PrimaryBlue.copy(alpha = 0.05f), CircleShape)
                .blur(120.dp)
        )

        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxSize().padding(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f).padding(end = 64.dp)) {
                    Text(
                        "STUDENT & FACULTY PORTAL", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = PrimaryBlue, 
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Campus\nParking.", 
                        fontSize = 64.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = PrimaryBlue, 
                        lineHeight = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Kiến tạo trải nghiệm đỗ xe thông minh trong khuôn viên học đường. Tiết kiệm thời gian, tối ưu không gian.", 
                        fontSize = 18.sp, 
                        color = OnSurfaceVariant,
                        lineHeight = 28.sp
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    LoginFormCard(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        error = errorText,
                        onLoginClick = {
                            if (email.isEmpty() || password.isEmpty()) {
                                errorText = "Vui lòng nhập đầy đủ thông tin"
                                return@LoginFormCard
                            }
                            scope.launch {
                                try {
                                    val user = database.userDao().getUserByEmail(email)
                                    if (user != null && user.password == password) {
                                        onLoginSuccess(user.userId ?: 0, user.role ?: "user")
                                    } else {
                                        errorText = "Tài khoản hoặc mật khẩu không chính xác"
                                    }
                                } catch (e: Exception) {
                                    errorText = "Lỗi kết nối cơ sở dữ liệu: ${e.message}"
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AuthHeader()
                Spacer(modifier = Modifier.height(32.dp))
                LoginFormCard(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    error = errorText,
                    onLoginClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            errorText = "Vui lòng nhập đầy đủ thông tin"
                            return@LoginFormCard
                        }
                        scope.launch {
                            try {
                                val user = database.userDao().getUserByEmail(email)
                                if (user != null && user.password == password) {
                                    onLoginSuccess(user.userId ?: 0, user.role ?: "user")
                                } else {
                                    errorText = "Tài khoản hoặc mật khẩu không chính xác"
                                }
                            } catch (e: Exception) {
                                errorText = "Lỗi kết nối cơ sở dữ liệu: ${e.message}"
                                e.printStackTrace()
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                FooterLegal()
            }
        }
    }
}

@Composable
fun LoginFormCard(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String,
    onLoginClick: () -> Unit
) {
    Surface(
        modifier = Modifier.widthIn(max = 448.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text("Chào mừng trở lại", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
            Text("Sử dụng tài khoản nội bộ để tiếp tục", fontSize = 14.sp, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            
            AuthTextField(email, onEmailChange, "TÀI KHOẢN", "Nhập email của bạn", Icons.Default.Person)
            Spacer(modifier = Modifier.height(20.dp))
            AuthTextField(password, onPasswordChange, "MẬT KHẨU", "••••••••", Icons.Default.Lock, true)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Đăng nhập", fontWeight = FontWeight.Bold, color = Color.White)
            }

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AuthHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Campus Parking.", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
        Text("Kiến tạo trải nghiệm đỗ xe thông minh", fontSize = 14.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun AuthTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    placeholder: String, 
    icon: ImageVector, 
    isPass: Boolean = false
) {
    Column {
        Text(
            label, 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Bold, 
            color = OnSurfaceVariant, 
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(icon, null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPass) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF2F4F6),
                focusedBorderColor = PrimaryBlue.copy(alpha = 0.3f),
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun FooterLegal() {
    Text(
        "© VNU University of Engineering and Technology \nTERMS • PRIVACY", 
        fontSize = 10.sp, 
        color = Color.Gray, 
        textAlign = TextAlign.Center, 
        letterSpacing = 1.sp
    )
}
