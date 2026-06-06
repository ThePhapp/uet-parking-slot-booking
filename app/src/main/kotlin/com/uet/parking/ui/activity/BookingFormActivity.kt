package com.uet.parking.ui.activity

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.screens.booking.BookingFormScreen
import com.uet.parking.ui.viewmodel.BookingViewModel
import com.uet.parking.ui.viewmodel.ViewModelFactory

class BookingFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userId = intent.getStringExtra("EXTRA_USER_ID") ?: ""

        setContent {
            val firestore = FirebaseFirestore.getInstance()
            val repository = ParkingRepository(firestore)
            
            val bookingViewModel: BookingViewModel = viewModel(
                factory = ViewModelFactory(repository, userId, application as Application)
            )

            BookingFormScreen(
                viewModel = bookingViewModel,
                onContinue = { date, startTime, endTime ->
                    val intent = Intent(this, SuccessActivity::class.java).apply {
                        putExtra(SuccessActivity.EXTRA_DATE, date)
                        putExtra(SuccessActivity.EXTRA_START_TIME, startTime)
                        putExtra(SuccessActivity.EXTRA_END_TIME, endTime)
                        putExtra("EXTRA_USER_ID", userId)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}
