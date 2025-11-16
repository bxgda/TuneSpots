package com.bogda.tunespots.presentation.ui.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.services.LocationService
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MapUiState(
    val lastKnownLocation: LatLng? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    val cameraPositionState = CameraPositionState(
        // --- IZMENA JE OVDE ---
        position = CameraPosition.fromLatLngZoom(
            LatLng(43.3209, 21.8958), // Koordinate za Niš
            13f // Možete podesiti i početni nivo zuma, npr. 13f
        )
    )

    fun startLocationUpdates() {
        locationService.getCurrentLocation()
            .onEach { newLocationGeoPoint -> // Renamed for clarity
                // Check if the GeoPoint is not null
                newLocationGeoPoint?.let { geoPoint ->
                    // Convert GeoPoint to LatLng
                    val newLatLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                    _uiState.update { it.copy(lastKnownLocation = newLatLng) }
                }
            }
            .launchIn(viewModelScope)
    }
}
    