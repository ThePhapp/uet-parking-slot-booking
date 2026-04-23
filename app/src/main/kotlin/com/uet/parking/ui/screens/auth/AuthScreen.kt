package com.uet.parking.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.*
import com.uet.parking.data.model.enums.UserRole

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(onLoginSuccess: (Int, UserRole) -> Unit = { _, _ -> }) {
    var isLoginMode by remember { mutableStateOf(true) }

    AnimatedContent(targetState = isLoginMode, label = "auth_screen_switch") { loginMode ->
        if (loginMode) {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = { isLoginMode = false }
            )
        } else {
            RegisterScreen(
                onRegisterSuccess = { isLoginMode = true },
                onNavigateToLogin = { isLoginMode = true }
            )
        }
    }
}
