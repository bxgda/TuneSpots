package com.bogda.tunespots.presentation.ui.screens.main

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bogda.tunespots.R
import com.bogda.tunespots.domain.model.Playlist
import com.bogda.tunespots.presentation.ui.viewmodels.main.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel,
    onAddPlaylistClick: () -> Unit
) {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.lastKnownLocation) {
        uiState.lastKnownLocation?.let {
            viewModel.cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 1500
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            locationPermissions.allPermissionsGranted -> {
                MapView(
                    cameraPositionState = viewModel.cameraPositionState,
                    playlists = uiState.playlists,
                    nearbyPlaylistIds = uiState.nearbyPlaylistIds
                )
            }
            locationPermissions.shouldShowRationale -> {
                Text("Dozvola za lokaciju je neophodna za prikaz mape.")
            }
            else -> {
                Text("Molimo odobrite dozvolu za lokaciju u podešavanjima telefona.")
            }
        }

        FloatingActionButton(
            onClick = onAddPlaylistClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Dodaj plejlistu")
        }
    }
}

@Composable
private fun MapView(
    cameraPositionState: CameraPositionState,
    playlists: List<Playlist>,
    nearbyPlaylistIds: Set<String>
) {
    val context = LocalContext.current
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style),
                isMyLocationEnabled = true
            )
        )
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false
            )
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        playlists.forEach { playlist ->
            val location = playlist.location
            val isNearby = nearbyPlaylistIds.contains(playlist.id)
            val iconColor = if (isNearby) {
                BitmapDescriptorFactory.HUE_VIOLET
            } else {
                BitmapDescriptorFactory.HUE_AZURE
            }

            Marker(
                state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                title = playlist.name,
                snippet = playlist.description,
                icon = BitmapDescriptorFactory.defaultMarker(iconColor)
            )
        }
    }
}
