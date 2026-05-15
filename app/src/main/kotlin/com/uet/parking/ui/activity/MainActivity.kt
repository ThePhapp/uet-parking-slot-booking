package com.uet.parking.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.screens.auth.AuthScreen
import com.uet.parking.ui.screens.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val firestore = remember { FirebaseFirestore.getInstance() }
                    val repository = remember { ParkingRepository(firestore) }
                    
                    var currentScreen by remember { mutableStateOf("auth") }
                    var currentUserId by remember { mutableStateOf("") } // Đổi sang String cho Firebase

                    if (currentScreen == "auth") {
                        AuthScreen(
                            repository = repository,
                            onLoginSuccess = { userId, role ->
                                currentUserId = userId
                                currentScreen = "settings"
                            }
                        )
                    } else {
                        SettingsScreen(
                            userId = currentUserId,
                            onBackClick = { currentScreen = "auth" },
                            onLogoutClick = { currentScreen = "auth" }
                        )
                    }
                }
            }
        }
    }
}
