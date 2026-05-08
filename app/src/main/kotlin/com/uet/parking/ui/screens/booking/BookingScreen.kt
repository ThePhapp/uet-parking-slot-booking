package com.uet.parking.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.model.HourlyLoad
import com.uet.parking.data.model.Ticket
import com.uet.parking.data.model.enums.TicketStatus
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingFormScreen(
    userId: Int,
    onContinue: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var selectedDate      by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var selectedStartTime by remember { mutableStateOf("07:00") }
    var selectedEndTime   by remember { mutableStateOf("09:00") }
    var errorMessage      by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    val startSlots = listOf(
        "Ca 1 — 07:00" to "07:00",
        "Ca 2 — 09:15" to "09:15",
        "Ca 3 — 12:30" to "12:30",
        "Ca 4 — 15:15" to "15:15"
    )
    val endSlots = listOf(
        "Ca 1 — 09:00" to "09:00",
        "Ca 2 — 11:15" to "11:15",
        "Ca 3 — 14:30" to "14:30",
        "Ca 4 — 17:15" to "17:15"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        val width = maxWidth
        val isWide = width > 800.dp
        val horizontalPadding = if (width > 1000.dp) (width - 1000.dp) / 2 + 24.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding)
        ) {
            // Progress Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bước 1 / 3", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("Chọn ngày & giờ đặt chỗ", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Text("1 / 3", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.33f },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Lịch chọn ngày (Hàng ngang - Tuần này)
            SectionLabel("Chọn ngày")
            WeekCalendarView(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it; errorMessage = "" }
            )

            Spacer(Modifier.height(24.dp))

            if (isWide) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        TimeSelectionSection(
                            startSlots = startSlots,
                            selectedStartTime = selectedStartTime,
                            onStartSelect = { selectedStartTime = it; errorMessage = "" },
                            endSlots = endSlots,
                            selectedEndTime = selectedEndTime,
                            onEndSelect = { selectedEndTime = it; errorMessage = "" }
                        )
                    }
                }
            } else {
                TimeSelectionSection(
                    startSlots = startSlots,
                    selectedStartTime = selectedStartTime,
                    onStartSelect = { selectedStartTime = it; errorMessage = "" },
                    endSlots = endSlots,
                    selectedEndTime = selectedEndTime,
                    onEndSelect = { selectedEndTime = it; errorMessage = "" }
                )
            }
            
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val fullSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val newStart = fullSdf.parse("$selectedDate $selectedStartTime")
                    val newEnd = fullSdf.parse("$selectedDate $selectedEndTime")

                    if (newStart != null && newEnd != null && !newStart.before(newEnd)) {
                        errorMessage = "Giờ bắt đầu phải sớm hơn giờ kết thúc"
                        return@Button
                    }

                    scope.launch {
                        try {
                            val existingTickets = database.ticketDao().getTicketsByUserId(userId).first()
                            
                            val isOverlapping = existingTickets.any { ticket ->
                                val ticketStart = fullSdf.parse(ticket.startTime ?: "")
                                val ticketEnd = fullSdf.parse(ticket.endTime ?: "")
                                
                                if (ticketStart != null && ticketEnd != null && newStart != null && newEnd != null) {
                                    newStart.before(ticketEnd) && newEnd.after(ticketStart)
                                } else false
                            }

                            if (isOverlapping) {
                                errorMessage = "Thời gian này trùng với một vé bạn đã đặt trước đó"
                                return@launch
                            }

                            // Xác định "Ca" (Shift) để cập nhật HourlyLoad
                            val shift = when(selectedStartTime) {
                                "07:00" -> 1
                                "09:15" -> 2
                                "12:30" -> 3
                                "15:15" -> 4
                                else -> 1
                            }

                            val ticket = Ticket(
                                userId = userId,
                                parkingId = 1,
                                startTime = "$selectedDate $selectedStartTime",
                                endTime = "$selectedDate $selectedEndTime",
                                status = TicketStatus.PENDING,
                                price = 10000.0
                            )
                            
                            // 1. Lưu vé vào DB
                            database.ticketDao().insertTicket(ticket)
                            
                            // 2. Cập nhật bảng HourlyLoad
                            val currentLoad = database.hourlyLoadDao().getLoad(1, selectedDate, shift)
                            if (currentLoad == null) {
                                database.hourlyLoadDao().insertOrUpdate(
                                    HourlyLoad(
                                        parkingId = 1,
                                        date = selectedDate,
                                        shift = shift,
                                        vehicleCount = 1
                                    )
                                )
                            } else {
                                database.hourlyLoadDao().incrementVehicleCount(1, selectedDate, shift)
                            }

                            onContinue(selectedDate, selectedStartTime, selectedEndTime)
                        } catch (e: Exception) {
                            errorMessage = "Lỗi hệ thống: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Tiếp tục →", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun WeekCalendarView(selectedDate: String, onDateSelected: (String) -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
    
    val weekDays = remember {
        val days = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        // Tạo danh sách 7 ngày từ hôm nay
        for (i in 0 until 7) {
            days.add(cal.time)
            cal.add(Calendar.DATE, 1)
        }
        days
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(weekDays) { date ->
            val dateStr = sdf.format(date)
            val isSelected = dateStr == selectedDate
            
            Surface(
                modifier = Modifier
                    .width(65.dp)
                    .height(85.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDateSelected(dateStr) },
                color = if (isSelected) PrimaryBlue else Color.White,
                tonalElevation = if (isSelected) 0.dp else 2.dp,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(0.5f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayFormat.format(date).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White.copy(0.7f) else Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateFormat.format(date),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSelectionSection(
    startSlots: List<Pair<String, String>>,
    selectedStartTime: String,
    onStartSelect: (String) -> Unit,
    endSlots: List<Pair<String, String>>,
    selectedEndTime: String,
    onEndSelect: (String) -> Unit
) {
    Column {
        SectionLabel("Giờ bắt đầu")
        TimeSlotGrid(slots = startSlots, selectedTime = selectedStartTime, onSelect = onStartSelect)
        Spacer(Modifier.height(16.dp))
        SectionLabel("Giờ kết thúc")
        TimeSlotGrid(slots = endSlots, selectedTime = selectedEndTime, onSelect = onEndSelect)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        modifier   = Modifier.padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize   = 15.sp,
        color      = Color(0xFF11131F)
    )
}

@Composable
private fun TimeSlotGrid(
    slots: List<Pair<String, String>>,
    selectedTime: String,
    onSelect: (String) -> Unit
) {
    Column {
        slots.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, time) ->
                    val isSelected = selectedTime == time
                    OutlinedButton(
                        onClick   = { onSelect(time) },
                        modifier  = Modifier.weight(1f).height(52.dp),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color(0xFFF2F4F6)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) PrimaryBlue else Color(0xFFC3C6D6)
                        )
                    ) {
                        Text(label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) PrimaryBlue else Color(0xFF434654))
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
