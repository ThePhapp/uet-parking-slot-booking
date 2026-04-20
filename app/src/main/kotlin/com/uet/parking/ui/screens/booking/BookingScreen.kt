package com.uet.parking.ui.screens.booking

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingFormScreen(
    onContinue:       (date: String, startTime: String, endTime: String) -> Unit,
    onHistoryClick:   () -> Unit,
    onNavItemSelected: (Int) -> Unit
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

    Scaffold(
        topBar    = { BookingTopBar(onHistoryClick = onHistoryClick) },
        bottomBar = { BookingBottomNav(selectedIndex = 0, onItemSelected = onNavItemSelected) },
        containerColor = BkColor.Surface
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp)
            ) {
                ProgressSection(
                    stepLabel   = "Bước 1 / 3",
                    stepTitle   = "Chọn ngày & giờ đặt chỗ",
                    stepCounter = "1 / 3",
                    progress    = 0.33f
                )

                Spacer(Modifier.height(16.dp))

                // Calendar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BkColor.Card),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            CalendarView(ctx).apply {
                                minDate = Calendar.getInstance().timeInMillis
                                setOnDateChangeListener { _, year, month, day ->
                                    val cal = Calendar.getInstance()
                                    cal.set(year, month, day)
                                    selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(cal.time)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Start time
                SectionLabel("Giờ bắt đầu")
                TimeSlotGrid(
                    slots        = startSlots,
                    selectedTime = selectedStartTime,
                    onSelect     = { selectedStartTime = it }
                )

                Spacer(Modifier.height(12.dp))

                // End time
                SectionLabel("Giờ kết thúc")
                TimeSlotGrid(
                    slots        = endSlots,
                    selectedTime = selectedEndTime,
                    onSelect     = { selectedEndTime = it }
                )
            }

            // Nút tiếp tục
            Button(
                onClick = {
                    val date = selectedDate.ifEmpty {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    }
                    onContinue(date, selectedStartTime, selectedEndTime)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start  = 16.dp,
                        end    = 16.dp,
                        bottom = padding.calculateBottomPadding() + 12.dp
                    )
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BkColor.Primary)
            ) {
                Text(
                    "Tiếp tục →",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = BkColor.OnPrimary
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        color      = BkColor.TextMain
    )
}

@Composable
private fun TimeSlotGrid(
    slots:        List<Pair<String, String>>,
    selectedTime: String,
    onSelect:     (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        slots.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (label, time) ->
                    val isSelected = selectedTime == time
                    OutlinedButton(
                        onClick   = { onSelect(time) },
                        modifier  = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape     = RoundedCornerShape(10.dp),
                        colors    = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) BkColor.SlotSelectedBg else BkColor.SlotNormalBg
                        ),
                        border    = if (isSelected)
                            androidx.compose.foundation.BorderStroke(1.5.dp, BkColor.Primary)
                        else
                            androidx.compose.foundation.BorderStroke(1.dp, BkColor.Stroke)
                    ) {
                        Text(
                            label,
                            fontSize   = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (isSelected) BkColor.Primary else BkColor.TextSub
                        )
                    }
                }
                // fill empty cell nếu row lẻ
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

