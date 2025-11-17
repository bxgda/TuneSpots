package com.bogda.tunespots.presentation.ui.screens.playlists

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bogda.tunespots.R
import com.bogda.tunespots.presentation.ui.components.TrackItem
import com.bogda.tunespots.presentation.ui.viewmodels.main.MapViewModel
import com.bogda.tunespots.presentation.ui.viewmodels.playlists.PlaylistState
import com.bogda.tunespots.presentation.ui.viewmodels.playlists.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onContributeClick: (String) -> Unit
) {
    val playlistState by playlistViewModel.playlistState.collectAsState()
    val mapUiState by mapViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            (playlistState as? PlaylistState.Loaded)?.playlist?.id?.let { playlistId ->
                if (mapUiState.nearbyPlaylistIds.contains(playlistId)) {
                    FloatingActionButton(onClick = { onContributeClick(playlistId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit playlist")
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val state = playlistState) {
            is PlaylistState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            is PlaylistState.Loaded -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            if (mapUiState.nearbyPlaylistIds.contains(state.playlist.id)) {
                                Text(
                                    text = "You can be contributor!",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            AsyncImage(
                                model = state.playlist.coverImageUrl,
                                contentDescription = "Playlist image",
                                placeholder = painterResource(id = R.drawable.ic_music_placeholder),
                                error = painterResource(id = R.drawable.ic_music_placeholder),
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.playlist.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            state.playlist.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Autor:", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                                AsyncImage(
                                    model = state.owner.profilePictureUrl,
                                    contentDescription = state.owner.username,
                                    placeholder = painterResource(id = R.drawable.ic_music_placeholder),
                                    error = painterResource(id = R.drawable.ic_music_placeholder),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                Text(state.owner.username, style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (state.contributors.isNotEmpty()) {
                                Text("Contributors:", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    state.contributors.forEach { contributor ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                                            AsyncImage(
                                                model = contributor.profilePictureUrl,
                                                contentDescription = contributor.username,
                                                placeholder = painterResource(id = R.drawable.ic_music_placeholder),
                                                error = painterResource(id = R.drawable.ic_music_placeholder),
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                            )
                                            Text(contributor.username, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tracks:", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    items(state.tracks, key = { it.id }) { track ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                        ) {
                            TrackItem(track = track, onToggleSelect = { })
                        }
                    }
                }
            }
            is PlaylistState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
