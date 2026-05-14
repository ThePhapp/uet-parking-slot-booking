package com.uet.parking.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.*
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (Int, UserRole) -> Unit = { _, _ -> }
) {
    var isLoginMode by remember { mutableStateOf(true) }

    AnimatedContent(targetState = isLoginMode, label = "auth_screen_switch") { loginMode ->
        if (loginMode) {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = { isLoginMode = false },
                viewModel = viewModel
            )
        } else {
            RegisterScreen(
                onRegisterSuccess = { isLoginMode = true },
                onNavigateToLogin = { isLoginMode = true },
                viewModel = viewModel
            )
        }
    }
}
