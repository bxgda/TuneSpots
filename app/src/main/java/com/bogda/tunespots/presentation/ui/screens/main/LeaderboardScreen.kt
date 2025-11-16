package com.bogda.tunespots.presentation.ui.screens.main

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LeaderboardScreen(modifier: Modifier = Modifier) {
    Scaffold(topBar = { TopAppBar(title = { Text("Top DJs") }) }) { padding ->
        // Sadržaj ekrana
    }
}