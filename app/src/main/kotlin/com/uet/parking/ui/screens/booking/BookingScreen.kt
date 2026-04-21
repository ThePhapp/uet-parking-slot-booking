package com.uet.parking.ui.screens.booking

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.uet.parking.ui.theme.BackgroundGray
import com.uet.parking.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingFormScreen(
    onContinue: (String, String, String) -> Unit = { _, _, _ -> },
    onHistoryClick: () -> Unit = {},
    onNavItemSelected: (Int) -> Unit = {}
) {
    var selectedDate      by remember { mutableStateOf("") }
    var selectedStartTime by remember { mutableStateOf("07:00") }
    var selectedEndTime   by remember { mutableStateOf("09:00") }

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
                .padding(bottom = 100.dp)
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

            if (isWide) {
                // Tablet/Desktop Layout: Calendar and Time side by side
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Box(modifier = Modifier.weight(1.2f)) {
                        CalendarCard(onDateChange = { selectedDate = it })
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        TimeSelectionSection(
                            startSlots = startSlots,
                            selectedStartTime = selectedStartTime,
                            onStartSelect = { selectedStartTime = it },
                            endSlots = endSlots,
                            selectedEndTime = selectedEndTime,
                            onEndSelect = { selectedEndTime = it }
                        )
                    }
                }
            } else {
                // Mobile Layout: Stacked
                CalendarCard(onDateChange = { selectedDate = it })
                Spacer(Modifier.height(24.dp))
                TimeSelectionSection(
                    startSlots = startSlots,
                    selectedStartTime = selectedStartTime,
                    onStartSelect = { selectedStartTime = it },
                    endSlots = endSlots,
                    selectedEndTime = selectedEndTime,
                    onEndSelect = { selectedEndTime = it }
                )
            }
        }

        // Action Button
        Button(
            onClick = {
                val date = selectedDate.ifEmpty {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                }
                onContinue(date, selectedStartTime, selectedEndTime)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Tiếp tục →", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun CalendarCard(onDateChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                CalendarView(ctx).apply {
                    minDate = Calendar.getInstance().timeInMillis
                    setOnDateChangeListener { _, year, month, day ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, day)
                        onDateChange(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
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
