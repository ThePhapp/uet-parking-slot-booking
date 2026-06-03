package com.uet.parking.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.theme.PrimaryContainer
import com.uet.parking.ui.theme.SurfaceVariant
import com.uet.parking.ui.viewmodel.ParkingLotDetailViewModel
import com.uet.parking.ui.viewmodel.ParkingLotDetailViewModelFactory

@Composable
fun ParkingLotDetailPage(
    lotId: String,
    adminId: String,
    scanResult: String? = null,
    onScanResultHandled: () -> Unit = {},
    onBack: () -> Unit,
    onNavigateToQrScan: (String, String) -> Unit
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val repository = remember { ParkingRepository(firestore) }
    val slotRepository = remember { com.uet.parking.data.repository.SlotRepository(firestore) }

    val viewModel: ParkingLotDetailViewModel = viewModel(
        factory = ParkingLotDetailViewModelFactory(repository, slotRepository, lotId, adminId)
    )

    val lot by viewModel.lot.collectAsState()
    val nextShiftLoad by viewModel.nextShiftLoad.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Hiển thị Toast cho các thông báo nội bộ của ViewModel
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Refresh dữ liệu khi có kết quả quét QR từ màn hình trước truyền về
    LaunchedEffect(scanResult) {
        scanResult?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.refreshLotData()
            onScanResultHandled()
        }
    }

    if (lot == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    val primaryGradient = Brush.linearGradient(
        colors = listOf(PrimaryBlue, PrimaryContainer)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            (lot?.address ?: "").uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            lot?.name ?: "",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    Surface(
                        color = PrimaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            lot?.status ?: "",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            item {
                // Căn chỉnh 2 cột có độ cao bằng nhau bằng IntrinsicSize.Min
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WorkloadGaugeCard(
                        lot = lot!!,
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight()
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShiftStatsCard(
                            inCount = nextShiftLoad,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        StatusGradientCard(
                            gradient = primaryGradient,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }
            }

            item {
                AdminScanActionsCard(
                    onScanCheckIn = { onNavigateToQrScan(lotId, "checkin") },
                    onScanCheckOut = { onNavigateToQrScan(lotId, "checkout") }
                )
            }

            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun AdminScanActionsCard(onScanCheckIn: () -> Unit, onScanCheckOut: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "KIỂM SOÁT VÉ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onScanCheckIn,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "Quét vào",
                        fontSize = 11.sp, // Giảm font size
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onScanCheckOut,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.QrCode, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "Quét ra",
                        fontSize = 11.sp, // Giảm font size
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun WorkloadGaugeCard(lot: ParkingLot, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "TẢI LƯỢNG HIỆN TẠI",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                Canvas(modifier = Modifier.size(130.dp)) {
                    drawArc(
                        color = SurfaceVariant,
                        startAngle = -225f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PrimaryBlue,
                        startAngle = -225f,
                        sweepAngle = 270f * (lot.density / 100f),
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${lot.density}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray
                    )
                    Text("CÔNG SUẤT", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Hiện tại", fontSize = 9.sp, color = Color.Gray)
                    Text("${lot.current ?: 0} xe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Kỳ vọng", fontSize = 9.sp, color = Color.Gray)
                    Text("${lot.capacity ?: 0} xe", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        }
    }
}

@Composable
fun ShiftStatsCard(modifier: Modifier = Modifier, inCount: Int = 0, outCount: Int = 0) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("CA TIẾP THEO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                Text("+$inCount", fontWeight = FontWeight.Black, color = PrimaryBlue)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp), tint = Color(0xFFF57C00))
                Text("-$outCount", fontWeight = FontWeight.Black, color = Color(0xFFF57C00))
            }
        }
    }
}

@Composable
fun StatusGradientCard(modifier: Modifier = Modifier, gradient: Brush) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(gradient),
        contentAlignment = Alignment.Center // Căn giữa nội dung trong Box theo cả 2 chiều
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { // Căn giữa Text trong Column
            Text(
                "TRẠNG THÁI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                "Ổn định",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
