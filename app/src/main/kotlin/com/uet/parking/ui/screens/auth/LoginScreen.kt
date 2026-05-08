package com.uet.parking.ui.screens.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.OnSurfaceVariant
import com.uet.parking.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Int, UserRole) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val errorText by viewModel.errorText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
    ) {
        val screenWidth = this.maxWidth
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
                        text = "Đăng nhập",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Sử dụng tài khoản nội bộ để tiếp tục",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearError() },
                        label = "EMAIL",
                        placeholder = "Nhập email",
                        icon = Icons.Default.Person
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearError() },
                        label = "MẬT KHẨU",
                        placeholder = "••••••••",
                        icon = Icons.Default.Lock,
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
                            viewModel.login(email, password, onLoginSuccess)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản?", fontSize = 14.sp, color = OnSurfaceVariant)
                Text(
                    text = " Đăng ký ngay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { 
                        viewModel.clearError()
                        onNavigateToRegister() 
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            FooterLegal()
        }
    }
}
