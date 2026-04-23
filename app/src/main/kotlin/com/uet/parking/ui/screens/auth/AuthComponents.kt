package com.uet.parking.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.ui.theme.OnSurfaceVariant
import com.uet.parking.ui.theme.PrimaryBlue

@Composable
fun AuthHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = PrimaryBlue.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = Icons.Default.LocalParking,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.padding(8.dp).size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Campus Parking",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            letterSpacing = (-1).sp
        )
        Text(
            text = "Kiến tạo trải nghiệm đỗ xe thông minh",
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
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
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            letterSpacing = 1.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
            trailingIcon = if (isPass) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (isPass && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF1F3F9),
                focusedBorderColor = PrimaryBlue.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
            ),
            singleLine = true
        )
    }
}

@Composable
fun FooterLegal() {
    Text(
        text = "© VNU University of Engineering and Technology \nTERMS • PRIVACY",
        fontSize = 10.sp,
        color = Color.Gray.copy(alpha = 0.6f),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        lineHeight = 16.sp,
        textAlign = TextAlign.Center
    )
}
