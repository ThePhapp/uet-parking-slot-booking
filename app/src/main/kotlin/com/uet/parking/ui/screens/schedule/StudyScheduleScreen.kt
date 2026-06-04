package com.uet.parking.ui.screens.schedule

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.model.StudySchedule
import com.uet.parking.ui.viewmodel.StudyScheduleViewModel
import com.uet.parking.ui.viewmodel.StudyScheduleViewModelFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.LaunchedEffect
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScheduleScreen(
    userId: String,
    viewModel: StudyScheduleViewModel = viewModel(
        factory = StudyScheduleViewModelFactory(userId)
    )
) {
    val schedules by viewModel.schedules.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<StudySchedule?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()

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
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
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

                }

                Button(onClick = { showDialog = true }) {
                    Text("+ Tạo lịch")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            WeeklyCalendarGrid(
                schedules = schedules,
                onScheduleClick = { schedule ->
                    selectedSchedule = schedule
                },
                onEmptyClick = {
                    selectedSchedule = null
                }
            )


        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )
    }

    if (showDialog) {
        CreateScheduleDialog(
            userId = userId,
            onDismiss = { showDialog = false },
            onSave = { newSchedule ->
                val isDuplicate = schedules.any {
                    it.dayOfWeek == newSchedule.dayOfWeek &&
                            it.startHour == newSchedule.startHour
                }

                if (isDuplicate) {
                    Toast.makeText(
                        context,
                        "Không được tạo lịch học trùng",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.addSchedule(newSchedule)
                    showDialog = false
                }
            }
        )
    }

    selectedSchedule?.let { schedule ->
        AlertDialog(
            onDismissRequest = {
                selectedSchedule = null
            },
            title = {
                Text("Xóa lịch học")
            },
            text = {
                Text("Bạn có muốn xóa lịch học ${schedule.subjectName} không?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSchedule(schedule.id)
                        selectedSchedule = null
                    }
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedSchedule = null
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun WeeklyCalendarGrid(
    schedules: List<StudySchedule>,
    onScheduleClick: (StudySchedule) -> Unit,
    onEmptyClick: () -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    val days = listOf(
        2 to "T2",
        3 to "T3",
        4 to "T4",
        5 to "T5",
        6 to "T6",
        7 to "T7"
    )

    val shifts = listOf(
        Triple(1, "Ca 1", "7h - 9h40"),
        Triple(2, "Ca 2", "9h50 - 12h30"),
        Triple(3, "Ca 3", "13h30 - 16h10"),
        Triple(4, "Ca 4", "16h20 - 19h")
    )

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp)
                .horizontalScroll(horizontalScrollState)
        ) {
            item {
                Row {
                    Box(modifier = Modifier.width(64.dp))

                    days.forEach { (_, label) ->
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            shifts.forEach { (shiftNumber, shiftName, shiftTime) ->
                item {
                    Row(
                        modifier = Modifier.height(112.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = shiftName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = shiftTime,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        days.forEach { (dayNumber, _) ->
                            val scheduleAtCell = schedules.firstOrNull {
                                it.dayOfWeek == dayNumber &&
                                        it.startHour == shiftNumber
                            }

                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                                    .background(
                                        color = if (scheduleAtCell != null)
                                            Color(0xFFE8F0FE)
                                        else
                                            Color(0xFFFDFDFD),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (scheduleAtCell != null) {
                                            onScheduleClick(scheduleAtCell)
                                        } else {
                                            onEmptyClick()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (scheduleAtCell != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = scheduleAtCell.subjectName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A73E8),
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = scheduleAtCell.room,
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
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