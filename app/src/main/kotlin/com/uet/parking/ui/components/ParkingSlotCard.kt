package com.uet.parking.ui.components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Custom component: ParkingSlotCard
 * Hiển thị thông tin một chỗ đỗ xe
 */
class ParkingSlotCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL
        setPadding(16, 16, 16, 16)
    }

    fun setParkingData(slotNumber: String, location: String, isAvailable: Boolean) {
        removeAllViews()

        val slotText = TextView(context).apply {
            text = "Chỗ: $slotNumber"
            textSize = 16f
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val statusColor = if (isAvailable) android.graphics.Color.GREEN else android.graphics.Color.RED
        val statusText = if (isAvailable) "Trống" else "Đã đỗ"

        val status = TextView(context).apply {
            text = statusText
            textSize = 14f
            setTextColor(statusColor)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        addView(slotText)
        addView(status)
    }
}
