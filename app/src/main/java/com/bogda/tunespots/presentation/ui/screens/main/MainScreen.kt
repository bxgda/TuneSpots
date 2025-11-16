package com.bogda.tunespots.presentation.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bogda.tunespots.presentation.ui.components.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // Koristimo tvoju listu itema iz BottomNavItem
    val navItems = listOf(
        BottomNavItem.Map,
        BottomNavItem.Search,
        BottomNavItem.Leaderboard,
        BottomNavItem.Playlists,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            // Počinjemo od mape, koristeći rutu iz tvoje klase
            startDestination = BottomNavItem.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Definišemo destinaciju za MapScreen
            composable(BottomNavItem.Map.route) {
                // Ovde se poziva Composable iz MapScreen.kt
                MapScreen()
            }
            // Ovde dodajemo ostale ekrane
            composable(BottomNavItem.Search.route) {
                // TODO: Ubaci SearchScreen() kada ga napraviš
                Box(modifier = Modifier.padding(innerPadding)) { Text("Search Screen") }
            }
            composable(BottomNavItem.Leaderboard.route) {
                // TODO: Ubaci LeaderboardScreen() kada ga napraviš
                Box(modifier = Modifier.padding(innerPadding)) { Text("Leaderboard Screen") }
            }
            composable(BottomNavItem.Playlists.route) {
                // TODO: Ubaci PlaylistsScreen() kada ga napraviš
                Box(modifier = Modifier.padding(innerPadding)) { Text("Playlists Screen") }
            }
            composable(BottomNavItem.Profile.route) {
                // TODO: Ubaci ProfileScreen() kada ga napraviš
                Box(modifier = Modifier.padding(innerPadding)) { Text("Profile Screen") }
            }
        }
    }
}
