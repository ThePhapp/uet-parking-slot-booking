package com.uet.parking.ui.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotPage(lotId: Int?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết Bãi đỗ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = PrimaryBlue) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { BottomNavigationBar() },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item {
                Column {
                    Text("KHU VỰC A - TẦNG 1", fontSize = 10.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    Text("Bãi đỗ xe số $lotId", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                }
            }
            item { WorkloadCard() }
            item { ScanCard() }
            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun WorkloadCard(
    currentLoad: Int = 150,
    expectedLoad: Int = 180,
    totalCapacity: Int = 200,
    nextIn: Int = 30,
    nextOut: Int = 15
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "TẢI LƯỢNG CHI TIẾT",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biểu đồ vòng cung cho tải lượng hiện tại
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawArc(
                            color = SurfaceVariant,
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(20f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = PrimaryBlue,
                            startAngle = -220f,
                            sweepAngle = 260f * (currentLoad.toFloat() / totalCapacity),
                            useCenter = false,
                            style = Stroke(20f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(currentLoad * 100 / totalCapacity)}%", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("Hiện tại", fontSize = 8.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Thông tin chi tiết
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow("Hiện tại:", "$currentLoad / $totalCapacity")
                    DetailRow("Dự kiến:", "$expectedLoad / $totalCapacity")
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = SurfaceVariant
                    )
                    
                    Text(
                        "Ca sau: $nextIn xe vào, $nextOut xe ra",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
    }
}

@Composable
fun ScanCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.Black)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(120.dp).border(2.dp, Color.White.copy(0.5f), RoundedCornerShape(16.dp)))
                Text("Quét mã QR", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
            }
            Column(modifier = Modifier.background(Color.White).padding(20.dp)) {
                OutlinedTextField(value = "", onValueChange = {}, placeholder = { Text("Nhập mã vé") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                    Text("Xác nhận")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailPreview() { ParkingLotPage(3, {}) }
