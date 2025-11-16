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

    private val _playlists = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastKnownLocation = locationService.getLocationUpdates()
        .map { geoPoint -> LatLng(geoPoint.latitude, geoPoint.longitude) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<MapUiState> = combine(
        _playlists,
        _lastKnownLocation
    ) { playlists, location ->
        val nearbyIds = calculateNearbyPlaylists(location, playlists)
        MapUiState(
            lastKnownLocation = location,
            playlists = playlists,
            nearbyPlaylistIds = nearbyIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState()
    )

    val cameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(
            LatLng(43.3209, 21.8958), // Koordinate za Niš
            13f
        )
    )

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
            distance <= 80
        }.map { it.id }.toSet()
    }
}
