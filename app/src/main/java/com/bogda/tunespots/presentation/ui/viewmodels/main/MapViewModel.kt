package com.bogda.tunespots.presentation.ui.viewmodels.main

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.repository.PlaylistRepository
import com.bogda.tunespots.data.services.LocationService
import com.bogda.tunespots.domain.model.Playlist
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val lastKnownLocation: LatLng? = null,
    val playlists: List<Playlist> = emptyList(),
    val nearbyPlaylistIds: Set<String> = emptySet()
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationService: LocationService,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    val cameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(
            LatLng(43.3209, 21.8958), // Koordinate za Niš
            13f
        )
    )

    init {
        fetchPlaylists()
    }

    private fun calculateNearbyPlaylists(
        userLocation: LatLng?,
        playlists: List<Playlist>
    ): Set<String> {
        if (userLocation == null) return emptySet()

        val userAndroidLocation = Location("user").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }

        return playlists.filter { playlist ->
            val playlistAndroidLocation = Location("playlist").apply {
                latitude = playlist.location.latitude
                longitude = playlist.location.longitude
            }
            val distance = userAndroidLocation.distanceTo(playlistAndroidLocation)
            distance <= 20
        }.map { it.id }.toSet()
    }

    private fun fetchPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlists ->
                val nearbyIds = calculateNearbyPlaylists(_uiState.value.lastKnownLocation, playlists)
                _uiState.update { it.copy(playlists = playlists, nearbyPlaylistIds = nearbyIds) }
            }
        }
    }

    fun startLocationUpdates() {
        locationService.getCurrentLocation()
            .onEach { newLocationGeoPoint ->
                newLocationGeoPoint?.let { geoPoint ->
                    val newLatLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                    val nearbyIds = calculateNearbyPlaylists(newLatLng, _uiState.value.playlists)
                    _uiState.update { it.copy(lastKnownLocation = newLatLng, nearbyPlaylistIds = nearbyIds) }
                }
            }
            .launchIn(viewModelScope)
    }
}
