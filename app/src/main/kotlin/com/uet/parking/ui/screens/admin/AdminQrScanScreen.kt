package com.uet.parking.ui.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.viewmodel.ParkingLotDetailViewModel
import com.uet.parking.ui.viewmodel.ParkingLotDetailViewModelFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AdminQrScanScreen(
    lotId: String,
    adminId: String,
    mode: String,
    onBackWithMessage: (String) -> Unit // Đã đổi tên hàm callback để nhận tin nhắn trả về
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val repository = remember { ParkingRepository(firestore) }

    val viewModel: ParkingLotDetailViewModel = viewModel(
        factory = ParkingLotDetailViewModelFactory(repository, lotId, adminId)
    )

    // Lắng nghe trạng thái thông báo từ ViewModel gửi về sau khi gọi Firebase
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Chặn luồng phản hồi: Chỉ phản hồi quay lại khi ViewModel đã trả về thông điệp xử lý xong
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            viewModel.clearToast()
            onBackWithMessage(msg) // Truyền ngược thông điệp về màn hình trước và đóng camera
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mode == "checkin") "Quét mã vào bãi" else "Quét mã ra bãi") },
                navigationIcon = {
                    IconButton(onClick = { onBackWithMessage("Đã hủy quét mã.") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasCameraPermission) {
                QrScannerView(
                    onCodeScanned = { code ->
                        if (mode == "checkin") {
                            viewModel.processCheckIn(context, code)
                        } else {
                            viewModel.processCheckOut(context, code)
                        }
                    }
                )
                QRScannerOverlay()
            } else {
                Text(
                    "Cần quyền truy cập camera để quét mã QR",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerView(onCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    var isScanned by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val scanner = BarcodeScanning.getClient()
                var sendedToViewModel = false // Cờ hiệu nội bộ chặn spam khung hình đa luồng

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && !isScanned && !sendedToViewModel) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (barcodes.isNotEmpty() && !isScanned && !sendedToViewModel) {
                                    isScanned = true
                                    sendedToViewModel = true // Khóa tức thì luồng xử lý camera

                                    barcodes[0].rawValue?.let { code ->
                                        onCodeScanned(code)
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
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun QRScannerOverlay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Đặt mã QR vào giữa khung hình",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}