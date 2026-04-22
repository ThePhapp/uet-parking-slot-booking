package com.uet.parking.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.uet.parking.ui.screens.booking.BookingFormScreen

class BookingFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookingFormScreen(
                userId = 0,
                onContinue = { date, startTime, endTime ->
                    val intent = Intent(this, SuccessActivity::class.java).apply {
                        putExtra(SuccessActivity.EXTRA_DATE, date)
                        putExtra(SuccessActivity.EXTRA_START_TIME, startTime)
                        putExtra(SuccessActivity.EXTRA_END_TIME, endTime)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}
