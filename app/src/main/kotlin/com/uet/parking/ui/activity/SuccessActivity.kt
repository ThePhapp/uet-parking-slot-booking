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
        val date       = intent.getStringExtra(EXTRA_DATE) ?: "--/--/----"
        val startTime  = intent.getStringExtra(EXTRA_START_TIME) ?: "--:--"
        val endTime    = intent.getStringExtra(EXTRA_END_TIME) ?: "--:--"
        val ticketCode = "CP-${(10000..99999).random()}-${('A'..'Z').random()}"

        setContent {
            SuccessScreen(
                date       = date,
                startTime  = startTime,
                endTime    = endTime,
                ticketCode = ticketCode,
                onGoHome = {
                    val i = Intent(this, BookingFormActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(i)
                    finish()
                },
                onHistoryClick = {
                    Toast.makeText(this, "Lịch sử đặt chỗ", Toast.LENGTH_SHORT).show()
                },
                onNavItemSelected = { index ->
                    when (index) {
                        0 -> {
                            val i = Intent(this, BookingFormActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(i)
                            finish()
                        }
                        2 -> Toast.makeText(this, "Settings - Coming soon", Toast.LENGTH_SHORT).show()
                    }
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
