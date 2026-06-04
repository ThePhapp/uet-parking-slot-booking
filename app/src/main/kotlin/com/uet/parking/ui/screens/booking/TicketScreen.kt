package com.uet.parking.ui.screens.booking

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import com.uet.parking.ui.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(viewModel: BookingViewModel) {
    val context = LocalContext.current
    val tickets by viewModel.userTickets.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val pullToRefreshState = rememberPullToRefreshState()
    
    val firestore = remember { FirebaseFirestore.getInstance() }
    val repository = remember { ParkingRepository(firestore) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var ticketToDelete by remember { mutableStateOf<Ticket?>(null) }

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshData()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        if (tickets.isEmpty()) {
            // Bao bọc EmptyView trong LazyColumn hoặc Box có khả năng scroll để PullToRefresh hoạt động
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { EmptyTicketsView() }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tickets) { ticket ->
                    var parkingLot by remember { mutableStateOf<ParkingLot?>(null) }
                    
                    LaunchedEffect(ticket.parkingId) {
                        ticket.parkingId?.let { id ->
                            parkingLot = repository.getParkingLotById(id)
                        }
                    }

                    val ticketCode = "PKG-${ticket.ticketId ?: "N/A"}-UET"

                    TicketItem(
                        ticket = ticket,
                        parkingLot = parkingLot,
                        onCopyCode = {
                            val clip = ClipData.newPlainText("Ticket Code", ticketCode)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, "Đã sao chép mã vé!", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = {
                            ticketToDelete = ticket
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa vé này không? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            ticketToDelete?.ticketId?.let { id ->
                                viewModel.deleteTicket(context, id)
                                showDeleteDialog = false
                                ticketToDelete = null
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
                    ) {
                        Text("Xóa", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Hủy")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun TicketItem(
    ticket: Ticket, 
    parkingLot: ParkingLot?, 
    onCopyCode: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val canDelete = ticket.status != TicketStatus.IN_PROGRESS

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalParking, null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = parkingLot?.name ?: "Đang tải...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parkingLot?.address ?: "---",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${ticket.startTime?.substringBefore(" ") ?: "---"} | ${ticket.startTime?.substringAfter(" ") ?: "---"} - ${ticket.endTime?.substringAfter(" ") ?: "---"}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                TicketStatusBadge(ticket.status)

                if (canDelete) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, "Xóa", tint = Color(0xFFBA1A1A))
                    }
                } else {
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Lock, "Đang sử dụng", tint = Color.LightGray)
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mã vé: PKG-${ticket.ticketId ?: "N/A"}-UET",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onCopyCode,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy, 
                                contentDescription = "Copy", 
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val ticketCode = "PKG-${ticket.ticketId ?: "N/A"}-UET"
                        val qrBitmap = remember(ticketCode) {
                            com.uet.parking.utils.QrUtils.generateQrCode(ticketCode, 300)
                        }
                        
                        if (qrBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(150.dp)
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Mã QR cho vé",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}

@Composable
fun TicketStatusBadge(status: TicketStatus?) {
    Surface(
        color = when(status) {
            TicketStatus.PENDING -> Color(0xFFFFF3E0)
            TicketStatus.IN_PROGRESS -> Color(0xFFE3F2FD)
            TicketStatus.CONFIRMED -> Color(0xFFE8F5E9)
            else -> Color(0xFFF5F5F5)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status?.value ?: "Unknown",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = when(status) {
                TicketStatus.PENDING -> Color(0xFFEF6C00)
                TicketStatus.IN_PROGRESS -> PrimaryBlue
                TicketStatus.CONFIRMED -> Color(0xFF2E7D32)
                else -> Color.Gray
            }
        )
    }
}

@Composable
fun EmptyTicketsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Bạn chưa có vé nào", color = Color.Gray, fontSize = 16.sp)
    }
}
