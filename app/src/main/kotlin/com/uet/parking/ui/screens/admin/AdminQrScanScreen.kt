package com.uet.parking.ui.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.BookingEntity
import com.uet.parking.data.model.enums.BookingStatus
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.util.QrCodeGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * Màn hình Admin Scan QR Code
 * Sử dụng CameraX + ML Kit Barcode Scanning
 */
@Composable
fun AdminQrScanScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // Scan result states
    var scanResult by remember { mutableStateOf<ScanResultState>(ScanResultState.Idle) }
    var isScanning by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            // Camera preview
            CameraPreviewWithScanner(
                isScanning = isScanning,
                onQrScanned = { qrContent ->
                    if (!isScanning) return@CameraPreviewWithScanner
                    isScanning = false

                    scope.launch {
                        scanResult = ScanResultState.Loading
                        scanResult = processQrScan(database, qrContent)
                    }
                }
            )

            // Scan overlay frame
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Scanner frame
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(3.dp, PrimaryBlue.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                )

                // Top label
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Quét mã QR Booking",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Đưa camera hướng vào mã QR của người dùng",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        } else {
            // No camera permission
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.LightGray
                )
                Spacer(Modifier.height(16.dp))
                Text("Cần quyền truy cập Camera", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Vui lòng cấp quyền camera để quét QR",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Cấp quyền Camera")
                }
            }
        }

        // Scan result overlay
        AnimatedVisibility(
            visible = scanResult !is ScanResultState.Idle,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ScanResultCard(
                state = scanResult,
                onDismiss = {
                    scanResult = ScanResultState.Idle
                    isScanning = true
                }
            )
        }
    }
}

/**
 * Camera Preview + ML Kit Scanner composable
 */
@Composable
fun CameraPreviewWithScanner(
    isScanning: Boolean,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (!isScanning) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        val scanner = BarcodeScanning.getClient()
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    if (barcode.valueType == Barcode.TYPE_TEXT) {
                                        barcode.rawValue?.let { value ->
                                            onQrScanned(value)
                                        }
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/**
 * State machine for scan results
 */
sealed class ScanResultState {
    data object Idle : ScanResultState()
    data object Loading : ScanResultState()
    data class Success(
        val booking: BookingEntity,
        val userName: String,
        val parkingLotName: String,
        val message: String
    ) : ScanResultState()
    data class Error(val message: String) : ScanResultState()
}

/**
 * Xử lý logic check-in khi quét QR
 */
suspend fun processQrScan(database: AppDatabase, qrContent: String): ScanResultState {
    // 1. Parse QR
    val data = QrCodeGenerator.parseQrContent(qrContent)
        ?: return ScanResultState.Error("Mã QR không hợp lệ hoặc không thuộc hệ thống UET Parking.")

    val bookingId = data["bookingId"] as? Int
        ?: return ScanResultState.Error("Mã QR không chứa thông tin booking.")

    // 2. Tìm booking trong DB
    val booking = database.bookingDao().getBookingById(bookingId)
        ?: return ScanResultState.Error("Booking #$bookingId không tồn tại trong hệ thống.")

    // 3. Kiểm tra trạng thái
    if (booking.isCheckedIn) {
        return ScanResultState.Error(
            "Booking #$bookingId đã được check-in trước đó.\n" +
            "Thời gian: ${booking.checkedInAt}"
        )
    }

    if (booking.status == BookingStatus.REJECTED) {
        return ScanResultState.Error("Booking #$bookingId đã bị từ chối, không thể check-in.")
    }

    if (booking.status == BookingStatus.PENDING) {
        return ScanResultState.Error("Booking #$bookingId chưa được duyệt. Vui lòng duyệt trước khi check-in.")
    }

    // 4. Kiểm tra QR có hết hạn không (dựa theo ngày + ca)
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    if (booking.bookingDate != today) {
        return ScanResultState.Error(
            "Booking #$bookingId không dành cho hôm nay.\n" +
            "Ngày booking: ${booking.bookingDate}"
        )
    }

    // Kiểm tra ca chơi đã kết thúc chưa
    val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val endTime = booking.bookingTime.split(" - ").getOrNull(1)?.trim() ?: ""
    if (endTime.isNotEmpty() && now > endTime) {
        return ScanResultState.Error(
            "Ca chơi đã kết thúc (${booking.bookingTime}).\nQR đã hết hạn."
        )
    }

    // 5. Thực hiện check-in
    val checkedInAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    database.bookingDao().checkInBooking(bookingId, checkedInAt)

    // 6. Lấy thông tin user và sân
    val user = database.userDao().getUserByIdSuspend(booking.userId)
    val userName = user?.name ?: user?.email ?: "User #${booking.userId}"

    val parkingLot = database.parkingLotDao().getParkingLotById(booking.fieldId)
    val parkingLotName = parkingLot?.name ?: "Sân #${booking.fieldId}"

    return ScanResultState.Success(
        booking = booking.copy(
            status = BookingStatus.CHECKED_IN,
            isCheckedIn = true,
            checkedInAt = checkedInAt
        ),
        userName = userName,
        parkingLotName = parkingLotName,
        message = "Check-in thành công!"
    )
}

/**
 * Card hiển thị kết quả quét QR
 */
@Composable
fun ScanResultCard(state: ScanResultState, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is ScanResultState.Loading -> {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Spacer(Modifier.height(16.dp))
                    Text("Đang xử lý...", fontSize = 14.sp, color = Color.Gray)
                }

                is ScanResultState.Success -> {
                    // Success icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF66BB6A),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        state.message,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(Modifier.height(20.dp))

                    // User info
                    ResultInfoRow(Icons.Default.Person, "Họ tên", state.userName)
                    ResultInfoRow(Icons.Default.LocationOn, "Sân", state.parkingLotName)
                    ResultInfoRow(Icons.Default.CalendarToday, "Ngày", state.booking.bookingDate)
                    ResultInfoRow(Icons.Default.AccessTime, "Ca chơi", "Ca ${state.booking.slot}")
                    ResultInfoRow(Icons.Default.Schedule, "Khung giờ", state.booking.bookingTime)
                    ResultInfoRow(Icons.Default.Login, "Check-in lúc", state.booking.checkedInAt ?: "")

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Quét tiếp", fontWeight = FontWeight.Bold)
                    }
                }

                is ScanResultState.Error -> {
                    // Error icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDECEA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Lỗi Check-in",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEF5350)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.message,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Thử lại", fontWeight = FontWeight.Bold)
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ResultInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
        Spacer(Modifier.width(10.dp))
        Text("$label:", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF11131F))
    }
}
