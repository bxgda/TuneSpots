package com.bogda.tunespots.data.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.bogda.tunespots.R
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class LocationUpdateService : Service(), DefaultLifecycleObserver {

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var locationCallback: LocationCallback

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super<Service>.onCreate()

        // FIX: Initialize locationCallback BEFORE registering the observer.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateUserLocation(GeoPoint(location.latitude, location.longitude))
                }
            }
        }

        // Now it's safe to add the observer, because locationCallback is initialized.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // FIX: Specify the foreground service type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(            NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION // Add this line
            )
        } else {
            // For older versions, the manifest declaration is enough
            startForeground(NOTIFICATION_ID, createNotification())
        }

        return START_STICKY
    }

    // Kada aplikacija uđe u foreground
    override fun onStart(owner: LifecycleOwner) {
        // Pozivamo startLocationUpdates sa isForeground = true
        startLocationUpdates(isForeground = true)
    }

    // Kada aplikacija uđe u background
    override fun onStop(owner: LifecycleOwner) {
        // Pozivamo startLocationUpdates sa isForeground = false
        startLocationUpdates(isForeground = false)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(isForeground: Boolean) {
        // Uvek ukloni prethodne update-e pre postavljanja novih
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val interval = if (isForeground) FOREGROUND_INTERVAL else BACKGROUND_INTERVAL
        val priority = if (isForeground) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        // Kreiranje LocationRequest-a sa modernim Builderom
        val locationRequest = LocationRequest.Builder(priority, interval).apply {
            // Postavlja minimalni interval za ažuriranje. Zamena za setFastestInterval.
            setMinUpdateIntervalMillis(interval)
            // setWaitForAccurateLocation postavljen na false omogućava da se ne čeka
            // na preciznu lokaciju, što je korisno za balansiranu potrošnju.
            setWaitForAccurateLocation(false)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun updateUserLocation(geoPoint: GeoPoint) {
        firebaseAuth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .update("lastLocation", geoPoint)
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Updates",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TuneSpots is running")
            .setContentText("Tracking your location to find nearby playlists")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super<Service>.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "location_updates"
        private const val NOTIFICATION_ID = 12345

        private const val FOREGROUND_INTERVAL = 5000L  // 5 seconds
        private const val BACKGROUND_INTERVAL = 10000L // 10 seconds
    }
}