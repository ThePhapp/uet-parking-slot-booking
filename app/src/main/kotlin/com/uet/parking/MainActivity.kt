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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.ui.components.common.AppBottomNavigationBar
import com.uet.parking.ui.components.common.AppTopBar
import com.uet.parking.ui.navigation.Route
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
    val currentDestination = navBackStackEntry?.destination

    // Biến cờ (flag) xác định vai trò người dùng sau khi đăng nhập
    var userRole by remember { mutableStateOf<UserRole?>(null) }
    var currentUserId by remember { mutableStateOf<Int?>(null) }
    
    // Logic nhận diện role dựa trên màn hình hiện tại
    LaunchedEffect(currentDestination) {
        if (currentDestination?.hasRoute<Route.Auth>() == true) {
            userRole = null
            currentUserId = null
        }
        else if (currentDestination?.hasRoute<Route.AdminHome>() == true || 
                 currentDestination?.hasRoute<Route.AdminDetail>() == true ||
                 currentDestination?.hasRoute<Route.AdminBooking>() == true ||
                 currentDestination?.hasRoute<Route.AdminSettings>() == true) {
            userRole = UserRole.ADMIN
        }
        else if (currentDestination?.hasRoute<Route.Home>() == true || 
                 currentDestination?.hasRoute<Route.Booking>() == true || 
                 currentDestination?.hasRoute<Route.Tickets>() == true || 
                 currentDestination?.hasRoute<Route.Settings>() == true) {
            userRole = UserRole.USER
        }
    }

    val isAdmin = userRole == UserRole.ADMIN
    val isUser = userRole == UserRole.USER

    Scaffold(
        topBar = {
            if (currentDestination?.hasRoute<Route.Auth>() == false) {
                AppTopBar(
                    title = when {
                        currentDestination.hasRoute<Route.Home>() -> "Trang chủ"
                        currentDestination.hasRoute<Route.Booking>() -> "Đặt chỗ"
                        currentDestination.hasRoute<Route.Searching>() -> "Đang tìm kiếm"
                        currentDestination.hasRoute<Route.Success>() -> "Thành công"
                        currentDestination.hasRoute<Route.Tickets>() -> "Vé của tôi"
                        currentDestination.hasRoute<Route.Settings>() || currentDestination.hasRoute<Route.AdminSettings>() -> "Cài đặt"
                        currentDestination.hasRoute<Route.AdminHome>() -> "Quản trị bãi đỗ"
                        currentDestination.hasRoute<Route.AdminDetail>() -> "Chi tiết bãi đỗ"
                        currentDestination.hasRoute<Route.AdminBooking>() -> "Lịch trình đặt chỗ"
                        else -> "Campus Parking"
                    },
                    showBack = currentDestination.hasRoute<Route.AdminDetail>() || 
                              currentDestination.hasRoute<Route.Tickets>() || 
                              currentDestination.hasRoute<Route.Booking>() || 
                              currentDestination.hasRoute<Route.Searching>() || 
                              currentDestination.hasRoute<Route.Success>(),
                    onBackClick = { navController.popBackStack() },
                    onHomeClick = { 
                        val homeRoute = if (isAdmin) Route.AdminHome else Route.Home
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
                    selectedIndex = when {
                        currentDestination?.hasRoute<Route.Home>() == true || currentDestination?.hasRoute<Route.AdminHome>() == true -> 0
                        currentDestination?.hasRoute<Route.Booking>() == true || currentDestination?.hasRoute<Route.AdminBooking>() == true -> 1
                        currentDestination?.hasRoute<Route.Tickets>() == true -> 2
                        currentDestination?.hasRoute<Route.Settings>() == true || currentDestination?.hasRoute<Route.AdminSettings>() == true -> if (isAdmin) 2 else 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        val target: Route = if (isAdmin) {
                            when (index) {
                                0 -> Route.AdminHome
                                1 -> Route.AdminBooking
                                2 -> Route.AdminSettings
                                else -> Route.AdminHome
                            }
                        } else {
                            when (index) {
                                0 -> Route.Home
                                1 -> Route.Booking
                                2 -> Route.Tickets
                                3 -> Route.Settings
                                else -> Route.Home
                            }
                        }
                        
                        navController.navigate(target) {
                            val popUpTarget = if (isAdmin) Route.AdminHome else Route.Home
                            popUpTo(popUpTarget) { inclusive = (target == popUpTarget) }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Auth,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Auth> {
                AuthScreen(onLoginSuccess = { userId, role ->
                    currentUserId = userId
                    userRole = role
                    val startRoute = if (role == UserRole.ADMIN) Route.AdminHome else Route.Home
                    navController.navigate(startRoute) {
                        popUpTo(Route.Auth) { inclusive = true }
                    }
                })
            }
            
            // --- User Routes ---
            composable<Route.Home> { 
                HomeScreen(
                    userId = currentUserId ?: 0,
                    onSettingsClick = { navController.navigate(Route.Settings) }
                ) 
            }
            composable<Route.Booking> { 
                BookingFormScreen(
                    userId = currentUserId ?: 0,
                    onContinue = { _, _, _ -> navController.navigate(Route.Searching) }
                ) 
            }
            composable<Route.Searching> { 
                SearchingScreen(onNavigateToSuccess = { 
                    navController.navigate(Route.Success) { 
                        popUpTo(Route.Booking) { inclusive = true } 
                    } 
                }) 
            }
            composable<Route.Success> { 
                SuccessScreen(
                    userId = currentUserId ?: 0,
                    onGoHome = { 
                        navController.navigate(Route.Home) { 
                            popUpTo(Route.Home) { inclusive = true } 
                        } 
                    }
                ) 
            }
            composable<Route.Tickets> { 
                TicketScreen(userId = currentUserId ?: 0) 
            }
            composable<Route.Settings> {
                SettingsScreen(
                    userId = currentUserId ?: 0,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { 
                        navController.navigate(Route.Auth) { 
                            popUpTo(0) 
                        } 
                    }
                )
            }

            // --- Admin Routes ---
            composable<Route.AdminHome> {
                AdminHomepage(onNavigateToDetail = { id ->
                    navController.navigate(Route.AdminDetail(id))
                })
            }
            composable<Route.AdminDetail> { backStackEntry ->
                val detail: Route.AdminDetail = backStackEntry.toRoute()
                ParkingLotDetailPage(lotId = detail.lotId, onBack = { navController.popBackStack() })
            }
            composable<Route.AdminBooking> { PlaceholderScreen("Lịch trình đặt chỗ (Trống)") }
            composable<Route.AdminSettings> {
                SettingsScreen(
                    userId = currentUserId ?: 0,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { 
                        navController.navigate(Route.Auth) { 
                            popUpTo(0) 
                        } 
                    }
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
