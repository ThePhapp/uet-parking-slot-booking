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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.components.common.AppBottomNavigationBar
import com.uet.parking.ui.components.common.AppTopBar
import com.uet.parking.ui.screens.admin.AdminHomepage
import com.uet.parking.ui.screens.admin.AdminBookingScreen
import com.uet.parking.ui.screens.admin.AdminQrScanScreen
import com.uet.parking.ui.screens.admin.ParkingLotDetailPage
import com.uet.parking.ui.screens.auth.AuthScreen
import com.uet.parking.ui.screens.home.HomeScreen
import com.uet.parking.ui.screens.settings.SettingsScreen
import com.uet.parking.ui.screens.booking.BookingFormScreen
import com.uet.parking.ui.screens.booking.SearchingScreen
import com.uet.parking.ui.screens.booking.SuccessScreen
import com.uet.parking.ui.screens.booking.TicketScreen
import com.uet.parking.ui.theme.ParkingTheme
import com.uet.parking.ui.screens.payment.PaymentScreen
import com.uet.parking.ui.viewmodel.AdminViewModel
import com.uet.parking.ui.viewmodel.HomeViewModel
import com.uet.parking.ui.viewmodel.PaymentViewModel
import com.uet.parking.ui.viewmodel.AuthViewModel
import com.uet.parking.ui.viewmodel.SettingsViewModel
import com.uet.parking.ui.viewmodel.ViewModelFactory

enum class Screen(val route: String) {
    AUTH("auth"),
    HOME("home"),
    BOOKING("booking"),
    SEARCHING("searching"),
    SUCCESS("success"),
    TICKETS("tickets"),
    SETTINGS("settings"),
    ADMIN_HOME("admin_home"),
    ADMIN_DETAIL("admin_detail/{lotId}"),
    ADMIN_BOOKING("admin_booking"),
    ADMIN_QR_SCAN("admin_qr_scan"),
    ADMIN_SETTINGS("admin_settings"),
    PAYMENT("payment")
}

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

    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        ParkingRepository(
            database.userDao(),
            database.ticketDao(),
            database.parkingLotDao(),
            database.hourlyLoadDao(),
            database.userInfoDao(),
            database.adminInfoDao(),
            database.bookingDao()
        )
    }

    var userRole by rememberSaveable { mutableStateOf<UserRole?>(null) }
    var currentUserId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(currentRoute) {
        // Chỉ reset khi quay lại màn hình AUTH và không còn role (đã logout)
        if (currentRoute == Screen.AUTH.route && userRole == null) {
            currentUserId = null
        }
        else if (currentRoute?.startsWith("admin") == true) userRole = UserRole.ADMIN
        else if (currentRoute in listOf(Screen.HOME.route, Screen.BOOKING.route, Screen.TICKETS.route, Screen.SETTINGS.route)) userRole = UserRole.USER
    }

    val isAdmin = userRole == UserRole.ADMIN
    val isUser = userRole == UserRole.USER

    Scaffold(
        topBar = {
            if (currentRoute != Screen.AUTH.route) {
                AppTopBar(
                    title = when {
                        currentRoute == Screen.HOME.route -> "Trang chủ"
                        currentRoute == Screen.BOOKING.route -> "Đặt chỗ"
                        currentRoute == Screen.SEARCHING.route -> "Đang tìm kiếm"
                        currentRoute == Screen.SUCCESS.route -> "Thành công"
                        currentRoute == Screen.TICKETS.route -> "Vé của tôi"
                        currentRoute == Screen.SETTINGS.route || currentRoute == Screen.ADMIN_SETTINGS.route -> "Cài đặt"
                        currentRoute == Screen.ADMIN_HOME.route -> "Quản trị bãi đỗ"
                        currentRoute == Screen.PAYMENT.route -> "Thanh toán"
                        currentRoute?.startsWith("admin_detail") == true -> "Chi tiết bãi đỗ"
                        currentRoute == Screen.ADMIN_BOOKING.route -> "Quản lý đặt sân"
                        currentRoute == Screen.ADMIN_QR_SCAN.route -> "Quét QR Check-in"
                        else -> "Campus Parking"
                    },
                    showBack = currentRoute?.startsWith("admin_detail") == true ||
                            currentRoute in listOf(
                                Screen.TICKETS.route,
                                Screen.BOOKING.route,
                                Screen.SEARCHING.route,
                                Screen.SUCCESS.route,
                                Screen.PAYMENT.route
                            ),
                    onBackClick = { navController.popBackStack() },
                    onHomeClick = {
                        val homeRoute = if (isAdmin) Screen.ADMIN_HOME.route else Screen.HOME.route
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
                        Screen.HOME.route, Screen.ADMIN_HOME.route -> 0
                        Screen.BOOKING.route, Screen.ADMIN_BOOKING.route -> 1
                        Screen.TICKETS.route -> 2
                        Screen.SETTINGS.route, Screen.ADMIN_SETTINGS.route -> if (isAdmin) 2 else 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        val target = if (isAdmin) {
                            when (index) {
                                0 -> Screen.ADMIN_HOME.route
                                1 -> Screen.ADMIN_BOOKING.route
                                2 -> Screen.ADMIN_SETTINGS.route
                                else -> Screen.ADMIN_HOME.route
                            }
                        } else {
                            when (index) {
                                0 -> Screen.HOME.route
                                1 -> Screen.BOOKING.route
                                2 -> Screen.TICKETS.route
                                3 -> Screen.SETTINGS.route
                                else -> Screen.HOME.route
                            }
                        }

                        navController.navigate(target) {
                            val popUpTarget = if (isAdmin) Screen.ADMIN_HOME.route else Screen.HOME.route
                            popUpTo(popUpTarget) { inclusive = (target == popUpTarget) }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.AUTH.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AUTH.route) {
                val authViewModel: AuthViewModel = viewModel(
                    factory = ViewModelFactory(repository, 0)
                )
                AuthScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { userId, role ->
                        currentUserId = userId
                        userRole = role
                        val startRoute = if (role == UserRole.ADMIN) Screen.ADMIN_HOME.route else Screen.HOME.route
                        navController.navigate(startRoute) {
                            popUpTo(Screen.AUTH.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.HOME.route) {
                val userId = currentUserId ?: 0
                val homeViewModel: HomeViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onBookNow = { navController.navigate(Screen.BOOKING.route) },
                    onPaymentClick = { navController.navigate(Screen.PAYMENT.route) },
                    onSettingsClick = { navController.navigate(Screen.SETTINGS.route) }
                )
            }

            composable(Screen.PAYMENT.route) {
                val userId = currentUserId ?: 0
                val paymentViewModel: PaymentViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                PaymentScreen(
                    viewModel = paymentViewModel,
                    onBackHome = {
                        navController.navigate(Screen.HOME.route) {
                            popUpTo(Screen.HOME.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.BOOKING.route) {
                BookingFormScreen(
                    userId = currentUserId ?: 0,
                    onContinue = { _, _, _ -> navController.navigate(Screen.SEARCHING.route) }
                )
            }
            composable(Screen.SEARCHING.route) { 
                SearchingScreen(onNavigateToSuccess = { 
                    navController.navigate(Screen.SUCCESS.route) { 
                        popUpTo(Screen.BOOKING.route) { inclusive = true } 
                    } 
                }) 
            }
            composable(Screen.SUCCESS.route) {
                SuccessScreen(
                    userId = currentUserId ?: 0,
                    onGoHome = { 
                        navController.navigate(Screen.HOME.route) { 
                            popUpTo(Screen.HOME.route) { inclusive = true } 
                        } 
                    }
                )
            }
            composable(Screen.TICKETS.route) {
                TicketScreen(userId = currentUserId ?: 0)
            }
            composable(Screen.SETTINGS.route) {
                val userId = currentUserId ?: 0
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { 
                        userRole = null
                        navController.navigate(Screen.AUTH.route) { popUpTo(0) } 
                    }
                )
            }

            // --- Admin Routes ---
            composable(Screen.ADMIN_HOME.route) {
                AdminHomepage(
                    userId = currentUserId ?: 0,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.ADMIN_DETAIL.route.replace("{lotId}", id.toString()))
                    }
                )
            }
            composable(Screen.ADMIN_DETAIL.route) { backStackEntry ->
                val lotId = backStackEntry.arguments?.getString("lotId")?.toIntOrNull() ?: 0
                ParkingLotDetailPage(
                    lotId = lotId,
                    adminId = currentUserId ?: 0,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ADMIN_BOOKING.route) {
                AdminBookingScreen(
                    userId = currentUserId ?: 0,
                    onNavigateToQrScan = {
                        navController.navigate(Screen.ADMIN_QR_SCAN.route)
                    }
                )
            }
            composable(Screen.ADMIN_QR_SCAN.route) {
                AdminQrScanScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.ADMIN_SETTINGS.route) {
                val userId = currentUserId ?: 0
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { 
                        userRole = null
                        navController.navigate(Screen.AUTH.route) { popUpTo(0) } 
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
