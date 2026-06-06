package com.uet.parking.utils

import java.text.SimpleDateFormat
import java.util.*

object ValidationUtils {
    fun validatePhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^[0-9]{10}$"))
    }

    fun validateStudentId(id: String): String? {
        if (id.isEmpty()) return "Mã sinh viên không được để trống"
        if (id.length != 8) return "Mã sinh viên phải có đúng 8 chữ số"
        if (!id.matches(Regex("^(1[5-9]|2[0-5])02\\d{4}$"))) {
            return "Mã sinh viên không hợp lệ"
        }
        return null
    }

    fun validateGender(g: String): String? {
        if (g != "Nam" && g != "Nữ") return "Vui lòng chọn Nam hoặc Nữ"
        return null
    }

    fun validateDate(date: String): String? {
        if (date.isEmpty()) return "Ngày sinh không được để trống"
        if (!date.matches(Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d$"))) {
            return "Định dạng đúng: DD/MM/YYYY"
        }
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            val parsedDate = sdf.parse(date)
            if (parsedDate != null && parsedDate.after(Date())) {
                "Ngày sinh không được ở tương lai"
            } else null
        } catch (e: Exception) {
            "Ngày không hợp lệ"
        }
    }
}
