package com.bogda.tunespots.presentation.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bogda.tunespots.domain.model.Playlist
import com.bogda.tunespots.presentation.ui.components.PlaylistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAuthored by remember { mutableStateOf(true) }
    var showContributed by remember { mutableStateOf(true) }

    Scaffold(topBar = { TopAppBar(title = { Text("Playlists") }) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                FilterChip(
                    selected = showAuthored,
                    onClick = { showAuthored = !showAuthored },
                    label = { Text("Authored") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = showContributed,
                    onClick = { showContributed = !showContributed },
                    label = { Text("Contributed") }
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
                    val playlists = mutableListOf<Pair<Playlist, String>>()
                    if(showAuthored) playlists.addAll(uiState.authoredPlaylists)
                    if(showContributed) playlists.addAll(uiState.contributedPlaylists)
                    items(playlists.distinct()) { (playlist, authorName) ->
                        PlaylistItem(
                            playlist = playlist,
                            authorName = authorName,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
