package com.bogda.tunespots.presentation.ui.screens.main

import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bogda.tunespots.presentation.navigation.Routes
import com.bogda.tunespots.presentation.ui.components.PlaylistItem
import com.bogda.tunespots.presentation.ui.viewmodels.main.MapViewModel
import com.bogda.tunespots.presentation.ui.viewmodels.main.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    mapViewModel: MapViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val mapUiState by mapViewModel.uiState.collectAsState()

    val currentUserLocation = mapUiState.lastKnownLocation?.let { latLng ->
        Location("provider").apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Search") }) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search by name or author") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                items(uiState.genres) { genre ->
                    FilterChip(
                        selected = uiState.selectedGenres.contains(genre),
                        onClick = { viewModel.onGenreSelected(genre, !uiState.selectedGenres.contains(genre)) },
                        label = { Text(genre) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "Radius: ${uiState.radius.toInt()} m")
                Slider(
                    value = uiState.radius,
                    onValueChange = { viewModel.onRadiusChange(it) },
                    valueRange = 200f..5000f,
                    steps = 48
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = uiState.error ?: "An unexpected error occurred.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val filteredPlaylists = uiState.playlists.filter { (playlist, author) ->
                        if (currentUserLocation == null) return@filter false

                        val playlistLocation = Location("provider").apply {
                            latitude = playlist.location.latitude
                            longitude = playlist.location.longitude
                        }
                        val distanceInMeters = currentUserLocation.distanceTo(playlistLocation)

                        val matchesSearchQuery = if (uiState.searchQuery.isBlank()) {
                            true
                        } else {
                            playlist.name.contains(uiState.searchQuery, ignoreCase = true) ||
                                    author.username.contains(uiState.searchQuery, ignoreCase = true)
                        }

                        uiState.selectedGenres.contains(playlist.genre) &&
                                distanceInMeters <= uiState.radius &&
                                matchesSearchQuery
                    }
                    items(filteredPlaylists) { (playlist, author) ->
                        PlaylistItem(
                            playlist = playlist,
                            authorName = author.username,
                            authorImageUrl = author.profilePictureUrl,
                            modifier = Modifier.padding(16.dp),
                            onClick = { navController.navigate(Routes.playlistDetail(playlist.id)) }
                        )
                    }
                }
            }
        }
    }
}
