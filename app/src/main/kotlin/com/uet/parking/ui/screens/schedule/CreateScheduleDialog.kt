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
    var shiftText by remember { mutableStateOf("1") }
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
                    label = { Text("Thứ: 2,3,4,5,6,7") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shiftText,
                    onValueChange = { shiftText = it },
                    label = { Text("Ca học: 1, 2, 3, 4") },
                    supportingText = {
                        Text("Ca 1: 7h-9h40 | Ca 2: 9h50-12h30 | Ca 3: 13h30-16h20 | Ca 4: 16h30-19h")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

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
                        startHour = shiftText.toIntOrNull() ?: 1,
                        endHour = shiftText.toIntOrNull() ?: 1,
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