package com.uet.parking.ui.screens.booking

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.model.Slot
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.viewmodel.NavigationViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    slot: Slot,
    onBack: () -> Unit,
    viewModel: NavigationViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Cấu hình OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val currentLocation by viewModel.currentLocation.collectAsState()
    val distance by viewModel.distanceToSlot.collectAsState()
    val etaMinutes by viewModel.etaMinutes.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val currentInstruction by viewModel.currentInstruction.collectAsState()

    var permissionGranted by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (permissionGranted) {
            viewModel.startLocationTracking(context)
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

    val slotGeoPoint = if (slot.latitude != null && slot.longitude != null) {
        GeoPoint(slot.latitude, slot.longitude)
    } else {
        GeoPoint(21.037000, 105.782000)
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(19.0)
                        controller.setCenter(slotGeoPoint)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    // Polyline dẫn đường
                    currentLocation?.let { loc ->
                        val userGeoPoint = GeoPoint(loc.latitude, loc.longitude)
                        
                        val polyline = Polyline()
                        if (routePoints.isNotEmpty()) {
                            // Vẽ đường uốn lượn lấy từ OSRM
                            polyline.setPoints(routePoints)
                        } else {
                            // Nếu chưa lấy được hoặc không có route, dự phòng vẽ đường thẳng
                            polyline.setPoints(listOf(userGeoPoint, slotGeoPoint))
                        }
                        
                        polyline.outlinePaint.color = android.graphics.Color.parseColor("#1976D2") // PrimaryBlue
                        polyline.outlinePaint.strokeWidth = 16f
                        polyline.outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                        polyline.outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        mapView.overlays.add(polyline)

                        // Marker User
                        val userMarker = Marker(mapView)
                        userMarker.position = userGeoPoint
                        userMarker.title = "Bạn đang ở đây"
                        // Sử dụng icon chấm tròn mặc định của hệ thống
                        androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)?.let {
                            userMarker.icon = it
                        }
                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        mapView.overlays.add(userMarker)

                        // Auto-follow center
                        if (isTracking) {
                            mapView.controller.animateTo(userGeoPoint)
                        }
                    }

                    // Marker Slot
                    val slotMarker = Marker(mapView)
                    slotMarker.position = slotGeoPoint
                    slotMarker.title = "Slot ${slot.coordinateLabel}"
                    androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)?.let {
                        slotMarker.icon = it
                    }
                    slotMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(slotMarker)

                    // Phát hiện thao tác kéo bản đồ -> Tắt Auto follow
                    mapView.setOnTouchListener { v, event ->
                        if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                            viewModel.setTracking(false)
                        }
                        false
                    }

                    mapView.invalidate()
                }
            )

            // Top Banner: Hướng dẫn rẽ
            AnimatedVisibility(
                visible = !currentInstruction.isNullOrBlank(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentInstruction ?: "",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // FAB Recenter
            AnimatedVisibility(
                visible = !isTracking,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 180.dp, end = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.setTracking(true) },
                    containerColor = Color.White,
                    contentColor = PrimaryBlue,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(Icons.Default.MyLocation, "Recenter")
                }
            }

            // Premium Bottom Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Điểm đến của bạn",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Slot ${slot.coordinateLabel}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.DarkGray
                                )
                            )
                        }
                        
                        // Icon P
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalParking, null, tint = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (!permissionGranted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Chưa cấp quyền GPS. Vui lòng bật định vị.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }
                    } else if (distance != null && etaMinutes != null) {
                        val distInt = distance!!.roundToInt()
                        
                        if (distInt <= 10) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bạn đã đến nơi!",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Distance
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.DirectionsWalk, null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (distInt >= 1000) "${String.format("%.1f", distInt/1000.0)} km" else "$distInt m",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.DarkGray
                                    )
                                    Text("Khoảng cách", fontSize = 12.sp, color = Color.Gray)
                                }
                                
                                // Divider
                                Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color.LightGray.copy(0.5f)))
                                
                                // ETA
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.Timer, null, tint = Color(0xFFF57C00), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$etaMinutes phút",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.DarkGray
                                    )
                                    Text("Đi bộ", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryBlue, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Đang định vị...", color = Color.Gray)
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
            text = { Text("Để tính toán khoảng cách và thời gian tới Slot, vui lòng cho phép truy cập vị trí và bật GPS.") },
            confirmButton = {
                TextButton(onClick = {
                    showGpsDialog = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text("Mở Cài đặt")
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
