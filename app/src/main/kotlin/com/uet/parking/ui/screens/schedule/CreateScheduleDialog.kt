package com.uet.parking.ui.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uet.parking.data.model.StudySchedule

@Composable
fun CreateScheduleDialog(
    userId: String,
    onDismiss: () -> Unit,
    onSave: (StudySchedule) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var dayOfWeekText by remember { mutableStateOf("2") }
    var startHourText by remember { mutableStateOf("7") }
    var endHourText by remember { mutableStateOf("9") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tạo lịch học")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Tên môn học") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Phòng học") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Giảng viên") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dayOfWeekText,
                    onValueChange = { dayOfWeekText = it },
                    label = { Text("Thứ: 2,3,4,5,6,7,8(CN)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHourText,
                        onValueChange = { startHourText = it },
                        label = { Text("Giờ bắt đầu") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endHourText,
                        onValueChange = { endHourText = it },
                        label = { Text("Giờ kết thúc") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val schedule = StudySchedule(
                        userId = userId,
                        subjectName = subjectName,
                        room = room,
                        teacherName = teacherName,
                        dayOfWeek = dayOfWeekText.toIntOrNull() ?: 2,
                        startHour = startHourText.toIntOrNull() ?: 7,
                        endHour = endHourText.toIntOrNull() ?: 9,
                        note = note
                    )

                    onSave(schedule)
                },
                enabled = subjectName.isNotBlank()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}