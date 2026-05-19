package com.uet.parking.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.model.StudySchedule

@Composable
fun StudyScheduleScreen(
    userId: String,
    viewModel: StudyScheduleViewModel = viewModel(
        factory = StudyScheduleViewModelFactory(userId)
    )
) {
    val schedules by viewModel.schedules.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFD))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Lịch học tuần",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dạng Google Calendar",
                    color = Color.Gray
                )
            }

            Button(onClick = { showDialog = true }) {
                Text("+ Tạo lịch")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        WeeklyCalendarGrid(schedules = schedules)
    }

    if (showDialog) {
        CreateScheduleDialog(
            userId = userId,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.addSchedule(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun WeeklyCalendarGrid(
    schedules: List<StudySchedule>
) {
    val days = listOf(
        2 to "T2",
        3 to "T3",
        4 to "T4",
        5 to "T5",
        6 to "T6",
        7 to "T7",
        8 to "CN"
    )

    val hours = (7..18).toList()

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.padding(8.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.width(48.dp))

                    days.forEach { (_, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            hours.forEach { hour ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text("${hour}:00", color = Color.Gray)
                        }

                        days.forEach { (dayNumber, _) ->
                            val scheduleAtCell = schedules.firstOrNull {
                                it.dayOfWeek == dayNumber &&
                                        hour >= it.startHour &&
                                        hour < it.endHour
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(2.dp)
                                    .background(
                                        color = if (scheduleAtCell != null)
                                            Color(0xFFE8F0FE)
                                        else
                                            Color(0xFFFDFDFD),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                if (scheduleAtCell != null && hour == scheduleAtCell.startHour) {
                                    Column(
                                        modifier = Modifier.padding(6.dp)
                                    ) {
                                        Text(
                                            text = scheduleAtCell.subjectName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A73E8)
                                        )
                                        Text(
                                            text = "${scheduleAtCell.startHour}:00 - ${scheduleAtCell.endHour}:00",
                                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                                        )
                                        Text(
                                            text = scheduleAtCell.room,
                                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}