package com.bogda.tunespots

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bogda.tunespots.data.services.LocationUpdateService
import com.bogda.tunespots.presentation.navigation.AppNavHost
import com.bogda.tunespots.presentation.theme.TuneSpotsTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start the service if user is already logged in
        if (firebaseAuth.currentUser != null) {
            startLocationService()
        }

        setContent {
            TuneSpotsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        onLoginSuccess = {
                            startLocationService()
                        },
                        onLogout = {
                            stopLocationService()
                        }
                    )
                }
            }
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationUpdateService::class.java)
        startService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationUpdateService::class.java)
        stopService(intent)
    }
}
