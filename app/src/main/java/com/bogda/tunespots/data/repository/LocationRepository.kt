package com.bogda.tunespots.data.repository

import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor() {
    private val _lastKnownLocation = MutableStateFlow<GeoPoint?>(null)
    val lastKnownLocation = _lastKnownLocation.asStateFlow()

    fun setLocation(newLocation: GeoPoint) {
        _lastKnownLocation.value = newLocation
    }
}
