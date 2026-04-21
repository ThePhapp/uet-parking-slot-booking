package com.uet.parking.ui.screens.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.OnSurfaceVariant

@Composable
fun AuthScreen(onLoginSuccess: (String) -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
    ) {
        val width = maxWidth
        val isTablet = width > 800.dp
        
        // Floating Background
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(100.dp, (-100).dp)
                .size(400.dp)
                .background(PrimaryBlue.copy(0.05f), CircleShape)
                .blur(120.dp)
        )

        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxSize().padding(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f).padding(end = 64.dp)) {
                    Text("STUDENT & FACULTY PORTAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, letterSpacing = 1.5.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Campus\nParking.", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, lineHeight = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Kiến tạo trải nghiệm đỗ xe thông minh trong khuôn viên học đường.", fontSize = 18.sp, color = OnSurfaceVariant)
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    LoginFormCard(email, { email = it }, password, { password = it }, errorText, onLoginSuccess) { errorText = it }
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
                LoginFormCard(email, { email = it }, password, { password = it }, errorText, onLoginSuccess) { errorText = it }
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
    onLogin: (String) -> Unit,
    onError: (String) -> Unit
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
            if (error.isNotEmpty()) {
                Text(error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
            }
            AuthTextField(email, onEmailChange, "TÀI KHOẢN", "user hoặc admin", Icons.Default.Person)
            Spacer(modifier = Modifier.height(20.dp))
            AuthTextField(password, onPasswordChange, "MẬT KHẨU", "1", Icons.Default.Lock, true)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { handleLogin(email, password, onLogin, onError) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Đăng nhập", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun handleLogin(email: String, pass: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    when {
        email == "user" && pass == "1" -> onSuccess("user")
        email == "admin" && pass == "1" -> onSuccess("admin")
        else -> onError("Thông tin đăng nhập không chính xác")
    }
}

@Composable
fun AuthHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Campus Parking.", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
        Text("Hệ thống quản lý thông minh", fontSize = 14.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun AuthTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, icon: ImageVector, isPass: Boolean = false) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray.copy(0.4f)) },
            leadingIcon = { Icon(icon, null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPass) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF2F4F6),
                focusedBorderColor = PrimaryBlue.copy(0.3f),
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun FooterLegal() {
    Text("© 2024 UNIVERSITY INFRASTRUCTURE\nTERMS • PRIVACY", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center, letterSpacing = 1.sp)
}
