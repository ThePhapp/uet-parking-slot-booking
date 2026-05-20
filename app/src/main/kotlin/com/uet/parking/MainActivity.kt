package com.uet.parking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.components.common.AppBottomNavigationBar
import com.uet.parking.ui.components.common.AppTopBar
import com.uet.parking.ui.screens.admin.AdminHomepage
import com.uet.parking.ui.screens.admin.ParkingLotDetailPage
import com.uet.parking.ui.screens.admin.AdminQrScanScreen
import com.uet.parking.ui.screens.auth.AuthScreen
import com.uet.parking.ui.screens.home.HomeScreen
import com.uet.parking.ui.screens.settings.SettingsScreen
import com.uet.parking.ui.screens.settings.EditProfileScreen
import com.uet.parking.ui.screens.booking.BookingFormScreen
import com.uet.parking.ui.screens.booking.SearchingScreen
import com.uet.parking.ui.screens.booking.SuccessScreen
import com.uet.parking.ui.screens.booking.TicketScreen
import com.uet.parking.ui.theme.ParkingTheme
import com.uet.parking.ui.viewmodel.*
import com.uet.parking.ui.screens.payment.PaymentScreen
import com.uet.parking.ui.navigation.Screen
import com.uet.parking.ui.screens.schedule.StudyScheduleScreen

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.uet.parking.utils.NotificationHelper.createNotificationChannel(this)
        setContent {
            ParkingTheme {
                MainNavigation(activityContext = this)
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MainNavigation(activityContext: android.content.Context) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val firestore = remember { FirebaseFirestore.getInstance() }
    val repository = remember { ParkingRepository(firestore) }

    var userRole by rememberSaveable { mutableStateOf<UserRole?>(null) }
    var currentUserId by rememberSaveable { mutableStateOf<String?>(null) }

    val handleLogout = {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.uet.parking.R.string.default_web_client_id))
            .build()
        GoogleSignIn.getClient(context, gso).signOut()
        
        currentUserId = null
        userRole = null
        navController.navigate(Screen.AUTH.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.AUTH.route) {
            // Already handled by handleLogout, but for safety:
            // if navigated here manually (e.g. back button if not cleared)
        }
        else if (currentRoute?.startsWith("admin") == true) {
            if (userRole == null) userRole = UserRole.ADMIN // Fallback
        }
        else if (currentRoute in listOf(Screen.HOME.route, Screen.BOOKING.route, Screen.TICKETS.route, Screen.SETTINGS.route, Screen.PAYMENT.route, Screen.STUDY_SCHEDULE.route)) {
            if (userRole == null) userRole = UserRole.USER // Fallback
        }
    }

    val isAdmin = userRole == UserRole.ADMIN || userRole == UserRole.GUARD
    val isUser = userRole == UserRole.USER

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        var hasNotificationPermission by remember {
            mutableStateOf(
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasNotificationPermission = isGranted
            }
        )

        LaunchedEffect(Unit) {
            if (!hasNotificationPermission) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val ticketIdFromIntent = (activityContext as? android.app.Activity)?.intent?.getStringExtra("ticketId")
    
    LaunchedEffect(currentUserId, userRole, ticketIdFromIntent) {
        if (currentUserId != null && ticketIdFromIntent != null) {
            navController.navigate(Screen.TICKETS.route)
            (activityContext as? android.app.Activity)?.intent?.removeExtra("ticketId")
        }
    }

    Scaffold(
        topBar = {
            if (currentRoute != Screen.AUTH.route && currentRoute?.startsWith("admin_qr_scan") != true) {
                AppTopBar(
                    title = when {
                        currentRoute == Screen.HOME.route -> "Trang chủ"
                        currentRoute == Screen.BOOKING.route -> "Đặt chỗ"
                        currentRoute == Screen.SEARCHING.route -> "Đang tìm kiếm"
                        currentRoute == Screen.SUCCESS.route -> "Thành công"
                        currentRoute == Screen.TICKETS.route -> "Vé của tôi"
                        currentRoute == Screen.SETTINGS.route || currentRoute == Screen.ADMIN_SETTINGS.route -> "Cài đặt"
                        currentRoute == Screen.EDIT_PROFILE.route -> "Thông tin cá nhân"
                        currentRoute == Screen.ADMIN_HOME.route -> "Quản trị bãi đỗ"
                        currentRoute == Screen.PAYMENT.route -> "Thanh toán"
                        currentRoute?.startsWith("admin_detail") == true -> "Chi tiết bãi đỗ"
                        currentRoute == Screen.ADMIN_BOOKING.route -> "Lịch trình đặt chỗ"
                        currentRoute == Screen.STUDY_SCHEDULE.route -> "Lịch học"
                        else -> "Campus Parking"
                    },
                    showBack = currentRoute?.startsWith("admin_detail") == true ||
                            currentRoute in listOf(
                                Screen.TICKETS.route,
                                Screen.BOOKING.route,
                                Screen.SEARCHING.route,
                                Screen.SUCCESS.route,
                                Screen.PAYMENT.route,
                                Screen.EDIT_PROFILE.route,
                                Screen.STUDY_SCHEDULE.route
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
            if ((isUser || isAdmin) && currentRoute?.startsWith("admin_qr_scan") != true) {
                AppBottomNavigationBar(
                    isAdmin = isAdmin,
                    selectedIndex = when (currentRoute) {
                        Screen.HOME.route, Screen.ADMIN_HOME.route -> 0
                        Screen.BOOKING.route, Screen.ADMIN_BOOKING.route -> 1
                        Screen.TICKETS.route -> 2
                        Screen.SETTINGS.route, Screen.ADMIN_SETTINGS.route, Screen.EDIT_PROFILE.route -> if (isAdmin) 2 else 3
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
                AuthScreen(
                    repository = repository,
                    onLoginSuccess = { userId, role ->
                        currentUserId = userId
                        userRole = role
                        val startRoute = if (role == UserRole.ADMIN || role == UserRole.GUARD) 
                            Screen.ADMIN_HOME.route 
                        else 
                            Screen.HOME.route
                        navController.navigate(startRoute) {
                            popUpTo(Screen.AUTH.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.HOME.route) {
                val userId = currentUserId ?: ""
                val homeViewModel: HomeViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onBookNow = { navController.navigate(Screen.BOOKING.route) },
                    onPaymentClick = { navController.navigate(Screen.PAYMENT.route) },
                    onStudyScheduleClick = { navController.navigate(Screen.STUDY_SCHEDULE.route) },
                    onSettingsClick = { navController.navigate(Screen.SETTINGS.route) }
                )
            }

            composable(Screen.PAYMENT.route) {
                val userId = currentUserId ?: ""
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

            composable(Screen.STUDY_SCHEDULE.route) {
                val userId = currentUserId ?: ""
                StudyScheduleScreen(userId = userId)
            }

            composable(Screen.BOOKING.route) {
                val userId = currentUserId ?: ""
                val bookingViewModel: BookingViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                BookingFormScreen(
                    viewModel = bookingViewModel,
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
                val userId = currentUserId ?: ""
                val bookingViewModel: BookingViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                SuccessScreen(
                    viewModel = bookingViewModel,
                    onGoHome = { navController.navigate(Screen.HOME.route) { popUpTo(Screen.HOME.route) { inclusive = true } } }
                )
            }

            composable(Screen.TICKETS.route) {
                val userId = currentUserId ?: ""
                val bookingViewModel: BookingViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                TicketScreen(viewModel = bookingViewModel)
            }

            composable(Screen.SETTINGS.route) {
                val userId = currentUserId ?: ""
                SettingsScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = handleLogout,
                    onEditProfileClick = { navController.navigate(Screen.EDIT_PROFILE.route) }
                )
            }

            composable(Screen.EDIT_PROFILE.route) {
                val userId = currentUserId ?: ""
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = ViewModelFactory(repository, userId)
                )
                EditProfileScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- Admin Routes ---
            composable(Screen.ADMIN_HOME.route) {
                AdminHomepage(
                    userId = currentUserId ?: "",
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.ADMIN_DETAIL.route.replace("{lotId}", id))
                    }
                )
            }

            composable(Screen.ADMIN_DETAIL.route) { backStackEntry ->
                val lotId = backStackEntry.arguments?.getString("lotId") ?: ""

                // Đã chỉ định rõ kiểu dữ liệu String để sửa lỗi "Cannot infer type"
                val scanResultState = backStackEntry.savedStateHandle.getLiveData<String>("scan_result").observeAsState()

                ParkingLotDetailPage(
                    lotId = lotId,
                    adminId = currentUserId ?: "",
                    onBack = { navController.popBackStack() },
                    onNavigateToQrScan = { id, mode ->
                        navController.navigate("admin_qr_scan/$id/$mode")
                    }
                )

                LaunchedEffect(scanResultState.value) {
                    scanResultState.value?.let { message ->
                        Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show()
                        backStackEntry.savedStateHandle.remove<String>("scan_result")
                    }
                }
            }

            composable("admin_qr_scan/{lotId}/{mode}") { backStackEntry ->
                val lotId = backStackEntry.arguments?.getString("lotId") ?: ""
                val mode = backStackEntry.arguments?.getString("mode") ?: ""

                AdminQrScanScreen(
                    lotId = lotId,
                    adminId = currentUserId ?: "",
                    mode = mode,
                    onBackWithMessage = { message -> // Sửa từ onBack thành onBackWithMessage để khớp 100% với file QR của bạn
                        navController.previousBackStackEntry?.savedStateHandle?.set("scan_result", message)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ADMIN_BOOKING.route) { PlaceholderScreen("Lịch trình đặt chỗ (Trống)") }
            composable(Screen.ADMIN_SETTINGS.route) {
                val userId = currentUserId ?: ""
                SettingsScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = handleLogout,
                    onEditProfileClick = { navController.navigate(Screen.EDIT_PROFILE.route) }
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