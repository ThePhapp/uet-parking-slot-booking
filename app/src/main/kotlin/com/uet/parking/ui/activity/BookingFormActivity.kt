package com.uet.parking.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.uet.parking.ui.screens.booking.BookingFormScreen

class BookingFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookingFormScreen(
                onContinue = { date, startTime, endTime ->
                    val intent = Intent(this, SearchingActivity::class.java).apply {
                        putExtra(SearchingActivity.EXTRA_DATE, date)
                        putExtra(SearchingActivity.EXTRA_START_TIME, startTime)
                        putExtra(SearchingActivity.EXTRA_END_TIME, endTime)
                    }
                    startActivity(intent)
                },
                onHistoryClick = {
                    Toast.makeText(this, "Lịch sử đặt chỗ", Toast.LENGTH_SHORT).show()
                },
                onNavItemSelected = { index ->
                    when (index) {
                        1 -> Toast.makeText(this, "My Tickets - Coming soon", Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(this, "Settings - Coming soon", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
