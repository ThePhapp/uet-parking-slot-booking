package com.uet.parking.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uet.parking.data.local.db.AppDatabase
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.screens.auth.AuthScreen
import com.uet.parking.ui.screens.settings.SettingsScreen
import com.uet.parking.ui.viewmodel.AuthViewModel
import com.uet.parking.ui.viewmodel.SettingsViewModel
import com.uet.parking.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember {
                ParkingRepository(
                    database.userDao(),
                    database.ticketDao(),
                    database.parkingLotDao(),
                    database.hourlyLoadDao(),
                    database.userInfoDao(),
                    database.adminInfoDao()
                )
            }

            MaterialTheme {
                Surface {
                    var currentScreen by remember { mutableStateOf("auth") }
                    var currentUserId by remember { mutableStateOf(0) }

                    if (currentScreen == "auth") {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = ViewModelFactory(repository, 0)
                        )
                        AuthScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { userId, role ->
                                currentUserId = userId
                                currentScreen = "settings"
                            }
                        )
                    } else {
                        val settingsViewModel: SettingsViewModel = viewModel(
                            key = "settings_$currentUserId",
                            factory = ViewModelFactory(repository, currentUserId)
                        )
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { currentScreen = "auth" },
                            onLogoutClick = { currentScreen = "auth" }
                        )
                    }
                }
            }
        }
    }
}
