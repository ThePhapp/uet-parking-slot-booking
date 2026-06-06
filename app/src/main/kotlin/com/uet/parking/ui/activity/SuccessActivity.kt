package com.uet.parking.ui.activity

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.screens.booking.SuccessScreen
import com.uet.parking.ui.viewmodel.BookingViewModel
import com.uet.parking.ui.viewmodel.ViewModelFactory

class SuccessActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DATE       = "extra_date"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME   = "extra_end_time"
        const val EXTRA_USER_ID    = "EXTRA_USER_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""

        setContent {
            val firestore = FirebaseFirestore.getInstance()
            val repository = ParkingRepository(firestore)
            
            val bookingViewModel: BookingViewModel = viewModel(
                factory = ViewModelFactory(repository, userId, application as Application)
            )

            SuccessScreen(
                viewModel = bookingViewModel,
                onGoHome = {
                    val i = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(i)
                    finish()
                }
            )
        }
    }
}
