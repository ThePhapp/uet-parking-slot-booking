package com.uet.parking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.uet.parking.ui.activity.BookingFormActivity
import com.uet.parking.ui.screens.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    HomeScreen(
                        onBookNow = {
                            startActivity(Intent(this, BookingFormActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}