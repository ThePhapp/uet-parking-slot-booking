package com.uet.parking.ui.activity

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.uet.parking.data.repository.ParkingRepository
import com.uet.parking.ui.screens.auth.AuthScreen
import com.uet.parking.ui.screens.settings.SettingsScreen
import com.uet.parking.ui.screens.settings.EditProfileScreen
import com.uet.parking.ui.viewmodel.SettingsViewModel
import com.uet.parking.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val firestore = remember { FirebaseFirestore.getInstance() }
                    val repository = remember { ParkingRepository(firestore) }
                    
                    val context = LocalContext.current
                    var currentScreen by remember { mutableStateOf("auth") }
                    var currentUserId by remember { mutableStateOf("") }

                    when (currentScreen) {
                        "auth" -> {
                            AuthScreen(
                                repository = repository,
                                onLoginSuccess = { userId, role ->
                                    currentUserId = userId
                                    currentScreen = "settings"
                                }
                            )
                        }
                        "settings" -> {
                            SettingsScreen(
                                userId = currentUserId,
                                application = context.applicationContext as Application,
                                onBackClick = { currentScreen = "auth" },
                                onLogoutClick = { currentScreen = "auth" },
                                onEditProfileClick = { currentScreen = "edit_profile" }
                            )
                        }
                        "edit_profile" -> {
                            val viewModel: SettingsViewModel = viewModel(
                                factory = ViewModelFactory(repository, currentUserId, context.applicationContext as Application)
                            )
                            EditProfileScreen(
                                viewModel = viewModel,
                                onBackClick = { currentScreen = "settings" }
                            )
                        }
                    }
                }
            }
        }
    }
}
