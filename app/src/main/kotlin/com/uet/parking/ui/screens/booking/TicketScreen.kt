package com.uet.parking.ui.screens.booking

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
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.ParkingLot
import com.uet.parking.data.model.Ticket
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@Composable
fun TicketScreen(userId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val tickets by database.ticketDao().getTicketsByUserId(userId).collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        if (tickets.isEmpty()) {
            EmptyTicketsView()
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
                            parkingLot = database.parkingLotDao().getParkingLotById(id)
                        }
                    }

                    TicketItem(
                        ticket = ticket,
                        parkingLot = parkingLot,
                        onDelete = {
                            scope.launch {
                                database.ticketDao().deleteTicket(ticket)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TicketItem(ticket: Ticket, parkingLot: ParkingLot?, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

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
                    Text(
                        text = "Mã vé: PKG-${ticket.ticketId}-UET",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ngày: ${ticket.startTime?.substringBefore(" ")}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                TicketStatusBadge(ticket.status)

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Xóa", tint = Color(0xFFBA1A1A))
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow(Icons.Default.LocalParking, "Bãi xe", parkingLot?.name ?: "Đang tải...")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.LocationOn, "Địa chỉ", parkingLot?.address ?: "Đang tải...")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.ConfirmationNumber, "Khung giờ", "${ticket.startTime?.substringAfter(" ")} - ${ticket.endTime?.substringAfter(" ")}")
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
fun TicketStatusBadge(status: String?) {
    Surface(
        color = when(status) {
            "Pending" -> Color(0xFFFFF3E0)
            "Confirmed" -> Color(0xFFE8F5E9)
            else -> Color(0xFFF5F5F5)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status ?: "Unknown",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = when(status) {
                "Pending" -> Color(0xFFEF6C00)
                "Confirmed" -> Color(0xFF2E7D32)
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
