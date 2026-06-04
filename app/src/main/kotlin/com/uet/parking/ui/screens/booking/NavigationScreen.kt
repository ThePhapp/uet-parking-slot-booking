package com.uet.parking.ui.screens.booking

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.model.Slot
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

    var permissionGranted by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (permissionGranted) {
            viewModel.startLocationTracking(context)
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                        controller.setZoom(18.0)
                        controller.setCenter(slotGeoPoint)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    // Marker cho Slot
                    val slotMarker = Marker(mapView)
                    slotMarker.position = slotGeoPoint
                    slotMarker.title = "Slot ${slot.coordinateLabel}"
                    slotMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(slotMarker)

                    // Marker cho User & Đường nối
                    currentLocation?.let { loc ->
                        val userGeoPoint = GeoPoint(loc.latitude, loc.longitude)
                        val userMarker = Marker(mapView)
                        userMarker.position = userGeoPoint
                        userMarker.title = "Bạn đang ở đây"
                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(userMarker)

                        val polyline = Polyline()
                        polyline.setPoints(listOf(userGeoPoint, slotGeoPoint))
                        polyline.outlinePaint.color = android.graphics.Color.BLUE
                        polyline.outlinePaint.strokeWidth = 8f
                        mapView.overlays.add(polyline)
                    }

                    mapView.invalidate()
                }
            )

            // Info Card at Bottom
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vị trí: ${slot.coordinateLabel}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (!permissionGranted) {
                        Text(
                            text = "Không thể lấy vị trí hiện tại. Bạn vẫn có thể xem vị trí slot trên bản đồ.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (distance != null) {
                        val distInt = distance!!.roundToInt()
                        if (distInt <= 10) {
                            Text(
                                text = "Bạn đã đến gần slot được phân công!",
                                color = Color(0xFF4CAF50), // Green
                                style = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Text(
                                text = "Khoảng cách còn lại: $distInt mét",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Đang cập nhật vị trí...",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        Text("Đang tìm vị trí của bạn...")
                    }
                }
            }
        }
    }

    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text("Yêu cầu định vị") },
            text = { Text("Vui lòng bật định vị (GPS) để dẫn đường tới slot.") },
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
