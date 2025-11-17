package com.bogda.tunespots.presentation.ui.screens.playlists

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bogda.tunespots.presentation.ui.components.TrackItem
import com.bogda.tunespots.presentation.ui.viewmodels.playlists.PlaylistState
import com.bogda.tunespots.presentation.ui.viewmodels.playlists.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val playlistState by viewModel.playlistState.collectAsState()

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
                        .padding(16.dp)
                ) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = state.playlist.coverImageUrl,
                                contentDescription = "Playlist image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.playlist.name, style = MaterialTheme.typography.headlineMedium)
                            state.playlist.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = state.owner.profilePictureUrl,
                                    contentDescription = "Owner image",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                Text(" by ${state.owner.username}", style = MaterialTheme.typography.labelMedium)
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
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                            )
                                            Text(contributor.username, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                    items(state.tracks, key = { it.id }) {
                        TrackItem(track = it, onToggleSelect = {})
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
