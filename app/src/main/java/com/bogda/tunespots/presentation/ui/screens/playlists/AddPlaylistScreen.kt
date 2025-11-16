package com.bogda.tunespots.presentation.ui.screens.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bogda.tunespots.R
import com.bogda.tunespots.data.model.Track
import com.bogda.tunespots.presentation.ui.viewmodels.main.MapViewModel
import com.bogda.tunespots.presentation.ui.viewmodels.playlists.AddPlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistScreen(
    mapViewModel: MapViewModel,
    viewModel: AddPlaylistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedTracks by viewModel.selectedTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAddingPlaylist by viewModel.isAddingPlaylist.collectAsState()
    val playlistAdded by viewModel.playlistAdded.collectAsState()

    LaunchedEffect(playlistAdded) {
        if (playlistAdded) {
            onNavigateBack()
            viewModel.onPlaylistAddedHandled()
        }
    }

    val genres = listOf("Pop", "Rock", "Hip-Hop", "Electronic", "Jazz", "Classical", "Folk")
    var isGenreDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add New Playlist") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.playlistName.value,
                onValueChange = { viewModel.playlistName.value = it },
                label = { Text("Playlist Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.playlistDescription.value,
                onValueChange = { viewModel.playlistDescription.value = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = isGenreDropdownExpanded,
                    onExpandedChange = { isGenreDropdownExpanded = !isGenreDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedGenre.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Genre") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenreDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = isGenreDropdownExpanded,
                        onDismissRequest = { isGenreDropdownExpanded = false })
                    {
                        genres.forEach { genre ->
                            DropdownMenuItem(
                                text = { Text(genre) },
                                onClick = {
                                    viewModel.selectedGenre.value = genre
                                    isGenreDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search for songs on Spotify") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Added songs: ${selectedTracks.size}")

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(searchResults, key = { it.id }) { track ->
                        TrackItem(
                            track = track,
                            isSelected = selectedTracks.any { it.id == track.id },
                            onToggleSelect = {
                                if (selectedTracks.any { it.id == track.id }) {
                                    viewModel.onTrackRemoved(track)
                                } else {
                                    viewModel.onTrackSelected(track)
                                }
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.addPlaylist() }, // Poziv bez parametara
                modifier = Modifier.fillMaxWidth(),
                // Gumb je omogućen čim su osnovni uslovi ispunjeni
                enabled = viewModel.playlistName.value.isNotBlank() && selectedTracks.isNotEmpty() && !isAddingPlaylist
            ) {
                if (isAddingPlaylist) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Add Playlist")
                }
            }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(track.title, maxLines = 1) },
        supportingContent = { Text(track.artist, maxLines = 1) },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.coverArtUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_music_placeholder), 
                error = painterResource(R.drawable.ic_music_placeholder),
                contentDescription = "Cover for ${track.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small)
            )
        },
        trailingContent = {
            IconButton(onClick = onToggleSelect) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = if (isSelected) "Remove song" else "Add song"
                )
            }
        }
    )
}