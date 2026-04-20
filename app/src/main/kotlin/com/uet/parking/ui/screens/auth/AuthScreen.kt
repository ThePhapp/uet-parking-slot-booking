package com.uet.parking.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
fun AuthScreen(onLoginSuccess: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
    ) {
        // Floating Background Shapes
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .size(400.dp)
                .background(Color(0xFF003D9B).copy(alpha = 0.05f), RoundedCornerShape(200.dp))
                .blur(120.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .size(300.dp)
                .background(Color(0xFF4C5D8D).copy(alpha = 0.05f), RoundedCornerShape(150.dp))
                .blur(100.dp)
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val isMobile = this.maxWidth < 800.dp
            
            if (isMobile) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Text(
                            "Campus Parking.",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Kiến tạo trải nghiệm đỗ xe thông minh",
                            fontSize = 16.sp,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color(0xFFC3C6D6).copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            AuthFormContent(onLoginSuccess)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    FooterLegal()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(end = 64.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "STUDENT & FACULTY PORTAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Campus\nParking.",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue,
                            lineHeight = 64.sp,
                            letterSpacing = (-2).sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Kiến tạo trải nghiệm đỗ xe thông minh trong khuôn viên học đường. Tiết kiệm thời gian, tối ưu không gian.",
                            fontSize = 18.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 28.sp,
                            modifier = Modifier.widthIn(max = 400.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            BentoElement(
                                icon = Icons.Default.LocalParking,
                                text = "5,000+ Chỗ trống mỗi ngày",
                                containerColor = PrimaryBlue.copy(alpha = 0.05f),
                                contentColor = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )
                            BentoElement(
                                icon = Icons.Default.LocalParking,
                                text = "Đặt chỗ trong 30 giây",
                                containerColor = Color(0xFF4C5D8D).copy(alpha = 0.1f),
                                contentColor = Color(0xFF4C5D8D),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .widthIn(max = 448.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Color(0xFFC3C6D6).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(40.dp)) {
                                AuthFormContent(onLoginSuccess)
                            }
                        }
                        Spacer(modifier = Modifier.height(48.dp))
                        FooterLegal()
                    }
                }
            }
        }
    }
}

@Composable
fun AuthFormContent(onLoginSuccess: () -> Unit) {
    Text(
        "Chào mừng trở lại",
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        color = PrimaryBlue,
        letterSpacing = (-0.5).sp
    )
    Text(
        "Sử dụng tài khoản nội bộ để tiếp tục",
        fontSize = 14.sp,
        color = OnSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
    
    Spacer(modifier = Modifier.height(40.dp))
    
    AuthTextField(
        label = "EMAIL / MÃ SINH VIÊN",
        placeholder = "username@university.edu",
        icon = Icons.Default.Person
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "MẬT KHẨU",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.sp
            )
            Text(
                "Quên mật khẩu?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.clickable { }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        AuthTextField(
            placeholder = "••••••••",
            icon = Icons.Default.Lock,
            isPassword = true
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = false, 
            onCheckedChange = {},
            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
        )
        Text(
            "Ghi nhớ đăng nhập",
            fontSize = 14.sp,
            color = OnSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onLoginSuccess,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = PrimaryBlue, spotColor = PrimaryBlue),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF003D9B), Color(0xFF0052CC))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Đăng nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
    
    Spacer(modifier = Modifier.height(40.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFC3C6D6).copy(alpha = 0.3f))
        Text(
            "HOẶC",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFC3C6D6).copy(alpha = 0.3f))
    }

    Spacer(modifier = Modifier.height(40.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Chưa có tài khoản? ", fontSize = 14.sp, color = OnSurfaceVariant)
        Text(
            "Đăng ký ngay",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun FooterLegal() {
    Text(
        "© 2024 UNIVERSITY SMART INFRASTRUCTURE\nTERMS OF SERVICE • PRIVACY POLICY",
        fontSize = 10.sp,
        color = Color.Gray.copy(alpha = 0.6f),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        lineHeight = 16.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun BentoElement(icon: ImageVector, text: String, containerColor: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(192.dp)
            .background(containerColor, RoundedCornerShape(12.dp))
            .border(1.dp, contentColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(36.dp))
        Text(text, fontWeight = FontWeight.Bold, color = contentColor, fontSize = 16.sp)
    }
}

@Composable
fun AuthTextField(label: String? = null, placeholder: String, icon: ImageVector, isPassword: Boolean = false) {
    Column {
        if (label != null) {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
            trailingIcon = if (isPassword) {
                { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Gray) }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF2F4F6),
                focusedBorderColor = PrimaryBlue.copy(alpha = 0.2f),
                unfocusedBorderColor = Color.Transparent,
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
        )
    }
}
