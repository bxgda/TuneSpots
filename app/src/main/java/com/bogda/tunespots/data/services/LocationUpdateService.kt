package com.bogda.tunespots.data.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bogda.tunespots.data.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class LocationUpdateService : Service() {

    @Inject
    lateinit var locationService: LocationService

    @Inject
    lateinit var locationRepository: LocationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationService.getLocationUpdates()
            .onEach { location ->
                locationRepository.setLocation(location)
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(serviceScope)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
