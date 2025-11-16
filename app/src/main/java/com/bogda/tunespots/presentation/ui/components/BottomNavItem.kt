package com.bogda.tunespots.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Map : BottomNavItem("Map", Icons.Default.Map, "map")
    object Search : BottomNavItem("Search", Icons.Default.Search, "search")
    object Leaderboard : BottomNavItem("Top DJs", Icons.Default.Leaderboard, "leaderboard")
    object Playlists : BottomNavItem("Playlists", Icons.AutoMirrored.Filled.List, "playlists")
    object Profile : BottomNavItem("Profile", Icons.Default.AccountCircle, "profile")
}
