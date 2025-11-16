package com.bogda.tunespots.presentation.ui.screens.main

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PlaylistsScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Playlists") }) }) { padding ->
        // Sadržaj ekrana
    }
}