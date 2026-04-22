package com.uet.parking.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.uet.parking.ui.screens.booking.SuccessScreen

class SuccessActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DATE       = "extra_date"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME   = "extra_end_time"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy userId từ Intent nếu có, mặc định là 0
        val userId = intent.getIntExtra("userId", 0)

        setContent {
            SuccessScreen(
                userId = userId,
                onGoHome = {
                    val i = Intent(this, BookingFormActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(i)
                    finish()
                }
            )
        }
    }

    @Deprecated("Use onBackPressedDispatcher")
    override fun onBackPressed() {
        val i = Intent(this, BookingFormActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(i)
        finish()
    }
}
