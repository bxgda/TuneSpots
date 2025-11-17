package com.bogda.tunespots.presentation.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bogda.tunespots.presentation.navigation.Routes
import com.bogda.tunespots.presentation.ui.components.TopDJList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    modifier: Modifier = Modifier, 
    viewModel: LeaderboardViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Top DJs") }) }) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "An unexpected error occurred.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                TopDJList(
                    djs = uiState.djs,
                    modifier = Modifier.fillMaxSize(),
                    onItemClick = { dj ->
                        navController.navigate(Routes.publicProfile(dj.id))
                    }
                )
            }
        }
    }
}