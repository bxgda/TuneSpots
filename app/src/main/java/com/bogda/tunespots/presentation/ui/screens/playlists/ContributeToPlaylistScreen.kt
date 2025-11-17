package com.bogda.tunespots.presentation.ui.screens.playlists

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bogda.tunespots.BuildConfig
import com.bogda.tunespots.presentation.ui.components.ImageSourceDialog
import com.bogda.tunespots.presentation.ui.components.TrackItem
import com.bogda.tunespots.presentation.ui.viewmodels.playlists.ContributeToPlaylistViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeToPlaylistScreen(
    viewModel: ContributeToPlaylistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedTracks by viewModel.selectedTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isContributing by viewModel.isContributing.collectAsState()
    val contributionFinished by viewModel.contributionFinished.collectAsState()
    val playlist by viewModel.playlist.collectAsState()
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

    LaunchedEffect(contributionFinished) {
        if (contributionFinished) {
            onNavigateBack()
            viewModel.onContributionFinishedHandled()
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Contribute to Playlist") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Add your tracks to this playlist!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (playlist?.coverImageUrl.isNullOrEmpty()) {
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
            }

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
                onClick = { viewModel.contribute() },
                modifier = Modifier.fillMaxWidth(),
                enabled = (selectedTracks.isNotEmpty() || imageUri != null) && !isContributing
            ) {
                if (isContributing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Contribute")
                }
            }
        }
    }
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
