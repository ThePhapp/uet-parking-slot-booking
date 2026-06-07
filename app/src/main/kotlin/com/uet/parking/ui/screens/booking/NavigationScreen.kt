package com.uet.parking.ui.screens.booking

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.model.Slot
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.viewmodel.NavigationViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    slot: Slot,
    onBack: () -> Unit,
    viewModel: NavigationViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val distance by viewModel.distanceToSlot.collectAsState()
    val azimuth by viewModel.azimuth.collectAsState()
    val bearing by viewModel.bearingToSlot.collectAsState()

    var permissionGranted by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (permissionGranted) {
            viewModel.startTracking(context)
        } else {
            showGpsDialog = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setDestination(slot)
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dẫn đường tới ${slot.coordinateLabel}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Compass UI with 3D Arrow
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer decorative circle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = PrimaryBlue.copy(alpha = 0.05f),
                            radius = size.minDimension / 2,
                        )
                    }

                    // Calculate rotation: bearing to target - device azimuth
                    val rotation by animateFloatAsState(targetValue = bearing - azimuth)
                    
                    CompassNeedle3D(rotation)
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Distance Text (Strictly in meters as requested)
                if (distance != null) {
                    val distInt = distance!!.roundToInt()
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$distInt",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "m",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    Text(
                        text = "KHOẢNG CÁCH HIỆN TẠI (ĐƯỜNG CHIM BAY)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Đang xác định vị trí...", color = Color.Gray)
                }
            }

            // Bottom card with destination info
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.DirectionsWalk, null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Đang đi tới",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Slot ${slot.coordinateLabel}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.DarkGray
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Khoảng cách hiển thị ở bên phải thẻ
                    if (distance != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${distance!!.roundToInt()} m",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "Còn lại",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text("Yêu cầu định vị") },
            text = { Text("Để chỉ đường tới Slot, vui lòng cho phép truy cập vị trí và bật GPS.") },
            confirmButton = {
                TextButton(onClick = {
                    showGpsDialog = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text("Cài đặt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) {
                    Text("Bỏ qua")
                }
            }
        )
    }
}

@Composable
fun CompassNeedle3D(rotation: Float) {
    Canvas(modifier = Modifier.size(220.dp)) {
        rotate(rotation) {
            val w = size.width
            val h = size.height
            val centerX = w / 2
            
            // 3D Arrow effect with two-tone shading
            
            // Left Side (Darker Blue)
            val pathLeft = Path().apply {
                moveTo(centerX, h * 0.1f)      // Tip
                lineTo(centerX - w * 0.22f, h * 0.85f) // Bottom Left
                lineTo(centerX, h * 0.7f)      // Inner Bottom
                close()
            }
            drawPath(pathLeft, Color(0xFF1565C0))

            // Right Side (Lighter Blue)
            val pathRight = Path().apply {
                moveTo(centerX, h * 0.1f)      // Tip
                lineTo(centerX + w * 0.22f, h * 0.85f) // Bottom Right
                lineTo(centerX, h * 0.7f)      // Inner Bottom
                close()
            }
            drawPath(pathRight, Color(0xFF42A5F5))
            
            // Subtle highlight at the pivot point
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = 5f,
                center = androidx.compose.ui.geometry.Offset(centerX, h * 0.7f)
            )
        }
    }
}
