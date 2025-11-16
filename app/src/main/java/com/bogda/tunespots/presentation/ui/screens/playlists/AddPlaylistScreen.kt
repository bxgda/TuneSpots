package com.bogda.tunespots.presentation.ui.screens.playlists

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bogda.tunespots.BuildConfig
import com.bogda.tunespots.R
import com.bogda.tunespots.data.model.Track
import com.bogda.tunespots.presentation.ui.components.ImageSourceDialog
import com.bogda.tunespots.presentation.ui.viewmodels.playlists.AddPlaylistViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistScreen(
    viewModel: AddPlaylistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedTracks by viewModel.selectedTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAddingPlaylist by viewModel.isAddingPlaylist.collectAsState()
    val playlistAdded by viewModel.playlistAdded.collectAsState()
    val imageUri by viewModel.playlistImageUri

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.playlistImageUri.value = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            viewModel.playlistImageUri.value = tempCameraUri
        }
    }

    LaunchedEffect(playlistAdded) {
        if (playlistAdded) {
            onNavigateBack()
            viewModel.onPlaylistAddedHandled()
        }
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismissRequest = { showImageSourceDialog = false },
            onTakePhoto = {
                val newUri = createImageUri(context)
                tempCameraUri = newUri
                cameraLauncher.launch(newUri)
            },
            onChooseFromGallery = {
                galleryLauncher.launch("image/*")
            }
        )
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
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Playlist image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add playlist image",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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
                onClick = { viewModel.addPlaylist() },
                modifier = Modifier.fillMaxWidth(),
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

private fun createImageUri(context: Context): Uri {
    val imageFile = File.createTempFile(
        "JPEG_${System.currentTimeMillis()}_",
        ".jpg",
        context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        imageFile
    )
}
