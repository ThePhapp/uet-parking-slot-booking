package com.uet.parking.data.model

data class StudySchedule(
    val id: String = "",
    val userId: String = "",
    val subjectName: String = "",
    val room: String = "",
    val teacherName: String = "",
    val dayOfWeek: Int = 2, // 2 = Thứ 2, 3 = Thứ 3, ..., 8 = Chủ nhật
    val startHour: Int = 7,
    val endHour: Int = 9,
    val color: String = "#4285F4",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)