package com.uet.parking.utils

import android.content.Context
import android.widget.Toast

/**
 * Utility class cho hiển thị Toast messages
 */
object ToastUtils {

    fun showShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
