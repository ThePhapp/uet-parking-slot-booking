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
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.BoxWithConstraints
import com.uet.parking.ui.viewmodel.BookingViewModel

@Composable
fun BookingFormScreen(
    viewModel: BookingViewModel,
    onContinue: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.bookingUiState.collectAsState()
    val startSlots by viewModel.startTimeSlots.collectAsState()
    val endSlots by viewModel.endTimeSlots.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        val width = this.maxWidth
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

            // Lịch chọn ngày
            SectionLabel("Chọn ngày")
            WeekCalendarView(
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.selectDate(it) }
            )

            Spacer(Modifier.height(24.dp))

            TimeSelectionSection(
                selectedDate = uiState.selectedDate,
                startSlots = startSlots,
                selectedStartTime = uiState.selectedStartTime,
                onStartSelect = { viewModel.selectStartTime(it) },
                endSlots = endSlots,
                selectedEndTime = uiState.selectedEndTime,
                onEndSelect = { viewModel.selectEndTime(it) }
            )
            
            if (uiState.errorMessage.isNotEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.createBooking {
                        onContinue(uiState.selectedDate, uiState.selectedStartTime, uiState.selectedEndTime)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Tiếp tục →", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
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
    selectedDate: String,
    startSlots: List<Pair<String, String>>,
    selectedStartTime: String,
    onStartSelect: (String) -> Unit,
    endSlots: List<Pair<String, String>>,
    selectedEndTime: String,
    onEndSelect: (String) -> Unit
) {
    Column {
        SectionLabel("Giờ bắt đầu")
        TimeSlotGrid(slots = startSlots, selectedTime = selectedStartTime, onSelect = onStartSelect, selectedDate = selectedDate)
        Spacer(Modifier.height(16.dp))
        SectionLabel("Giờ kết thúc")
        TimeSlotGrid(slots = endSlots, selectedTime = selectedEndTime, onSelect = onEndSelect, selectedDate = selectedDate)
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
    onSelect: (String) -> Unit,
    selectedDate: String
) {
    val fullSdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    // Using a key so it updates if needed, though for a booking form, evaluating 'now' once is usually fine.
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        // optionally update 'now' periodically if the screen stays open
    }

    Column {
        slots.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, time) ->
                    val isSelected = selectedTime == time
                    
                    var isEnabled = true
                    try {
                        val slotTime = fullSdf.parse("$selectedDate $time")
                        if (slotTime != null) {
                            val diffHours = (slotTime.time - now.time) / (1000 * 60 * 60.0)
                            if (diffHours < 1.0) {
                                isEnabled = false
                            }
                        }
                    } catch (e: Exception) {}

                    OutlinedButton(
                        onClick   = { if(isEnabled) onSelect(time) },
                        modifier  = Modifier.weight(1f).height(52.dp),
                        enabled   = isEnabled,
                        shape     = RoundedCornerShape(12.dp),
                        colors    = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color(0xFFF2F4F6),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) PrimaryBlue else if (!isEnabled) Color(0xFFBDBDBD) else Color(0xFFC3C6D6)
                        )
                    ) {
                        Text(
                            label, 
                            fontSize = 13.sp, 
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, 
                            color = if (isSelected) PrimaryBlue else if (!isEnabled) Color(0xFF9E9E9E) else Color(0xFF434654)
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
