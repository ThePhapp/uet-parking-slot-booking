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
import com.uet.parking.ui.screens.auth.AuthScreen
import com.uet.parking.ui.screens.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    var currentScreen by remember { mutableStateOf("auth") }

                    if (currentScreen == "auth") {
                        AuthScreen(onLoginSuccess = {
                            currentScreen = "settings"
                        })
                    } else {
                        SettingsScreen(
                            onBackClick = { currentScreen = "auth" },
                            onLogoutClick = { currentScreen = "auth" }
                        )
                    }
                }
            }
        }
    }
}