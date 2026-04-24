package com.uet.parking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.ui.components.common.AppBottomNavigationBar
import com.uet.parking.ui.components.common.AppTopBar
import com.uet.parking.ui.screens.admin.AdminHomepage
import com.uet.parking.ui.screens.admin.ParkingLotDetailPage
import com.uet.parking.ui.screens.auth.AuthScreen
import com.uet.parking.ui.screens.home.HomeScreen
import com.uet.parking.ui.screens.settings.SettingsScreen
import com.uet.parking.ui.screens.booking.BookingFormScreen
import com.uet.parking.ui.screens.booking.SearchingScreen
import com.uet.parking.ui.screens.booking.SuccessScreen
import com.uet.parking.ui.screens.booking.TicketScreen
import com.uet.parking.ui.theme.ParkingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParkingTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Biến cờ (flag) xác định vai trò người dùng sau khi đăng nhập
    var userRole by remember { mutableStateOf<UserRole?>(null) }
    var currentUserId by remember { mutableStateOf<Int?>(null) }
    
    // Logic nhận diện role dựa trên màn hình hiện tại (để giữ trạng thái khi xoay màn hình...)
    LaunchedEffect(currentRoute) {
        if (currentRoute == "auth") {
            userRole = null
            currentUserId = null
        }
        else if (currentRoute?.startsWith("admin") == true) userRole = UserRole.ADMIN
        else if (currentRoute in listOf("home", "booking", "tickets", "settings")) userRole = UserRole.USER
    }

    val isAdmin = userRole == UserRole.ADMIN
    val isUser = userRole == UserRole.USER

    Scaffold(
        topBar = {
            if (currentRoute != "auth") {
                AppTopBar(
                    title = when {
                        currentRoute == "home" -> "Trang chủ"
                        currentRoute == "booking" -> "Đặt chỗ"
                        currentRoute == "searching" -> "Đang tìm kiếm"
                        currentRoute == "success" -> "Thành công"
                        currentRoute == "tickets" -> "Vé của tôi"
                        currentRoute == "settings" || currentRoute == "admin_settings" -> "Cài đặt"
                        currentRoute == "admin_home" -> "Quản trị bãi đỗ"
                        currentRoute?.startsWith("admin_detail") == true -> "Chi tiết bãi đỗ"
                        currentRoute == "admin_booking" -> "Lịch trình đặt chỗ"
                        else -> "Campus Parking"
                    },
                    showBack = currentRoute?.startsWith("admin_detail") == true || 
                              currentRoute in listOf("tickets", "booking", "searching", "success"),
                    onBackClick = { navController.popBackStack() },
                    onHomeClick = { 
                        val homeRoute = if (isAdmin) "admin_home" else "home"
                        navController.navigate(homeRoute) {
                            popUpTo(homeRoute) { inclusive = true }
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Refresh logic */ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Gray)
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isUser || isAdmin) {
                AppBottomNavigationBar(
                    isAdmin = isAdmin,
                    selectedIndex = when (currentRoute) {
                        "home", "admin_home" -> 0
                        "booking", "admin_booking" -> 1
                        "tickets" -> 2
                        "settings", "admin_settings" -> if (isAdmin) 2 else 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        val target = if (isAdmin) {
                            when (index) {
                                0 -> "admin_home"
                                1 -> "admin_booking"
                                2 -> "admin_settings"
                                else -> "admin_home"
                            }
                        } else {
                            when (index) {
                                0 -> "home"
                                1 -> "booking"
                                2 -> "tickets"
                                3 -> "settings"
                                else -> "home"
                            }
                        }
                        
                        navController.navigate(target) {
                            val popUpTarget = if (isAdmin) "admin_home" else "home"
                            popUpTo(popUpTarget) { inclusive = (target == popUpTarget) }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(onLoginSuccess = { userId, role ->
                    currentUserId = userId
                    userRole = role
                    val startRoute = if (role == UserRole.ADMIN) "admin_home" else "home"
                    navController.navigate(startRoute) {
                        popUpTo("auth") { inclusive = true }
                    }
                })
            }
            
            // --- User Routes ---
            composable("home") {
                HomeScreen(
                    userId = currentUserId ?: 0,

                    onBookNow = {
                        navController.navigate("booking") {
                            launchSingleTop = true
                        }
                    },

                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("booking") { 
                BookingFormScreen(
                    userId = currentUserId ?: 0,
                    onContinue = { _, _, _ -> navController.navigate("searching") }
                ) 
            }
            composable("searching") { SearchingScreen(onNavigateToSuccess = { navController.navigate("success") { popUpTo("booking") { inclusive = true } } }) }
            composable("success") { 
                SuccessScreen(
                    userId = currentUserId ?: 0,
                    onGoHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                ) 
            }
            composable("tickets") { 
                TicketScreen(userId = currentUserId ?: 0) 
            }
            composable("settings") {
                SettingsScreen(
                    userId = currentUserId ?: 0,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigate("auth") { popUpTo(0) } }
                )
            }

            // --- Admin Routes ---
            composable("admin_home") {
                AdminHomepage(onNavigateToDetail = { id ->
                    navController.navigate("admin_detail/$id")
                })
            }
            composable("admin_detail/{lotId}") { backStackEntry ->
                val lotId = backStackEntry.arguments?.getString("lotId")?.toIntOrNull() ?: 0
                ParkingLotDetailPage(lotId = lotId, onBack = { navController.popBackStack() })
            }
            composable("admin_booking") { PlaceholderScreen("Lịch trình đặt chỗ (Trống)") }
            composable("admin_settings") {
                SettingsScreen(
                    userId = currentUserId ?: 0,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigate("auth") { popUpTo(0) } }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
    }
}
