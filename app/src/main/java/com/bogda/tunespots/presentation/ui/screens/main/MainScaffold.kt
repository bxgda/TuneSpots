package com.bogda.tunespots.presentation.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bogda.tunespots.presentation.ui.components.BottomNavItem

@Composable
fun MainScaffold(onLogout: () -> Unit) {
    val mainNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = mainNavController)
        }
    ) { innerPadding ->
        // NavHost za ekrane unutar glavnog dela aplikacije
        NavHost(
            navController = mainNavController,
            startDestination = BottomNavItem.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Map.route) { MapScreen() }
            composable(BottomNavItem.Search.route) { SearchScreen() }
            composable(BottomNavItem.Leaderboard.route) { LeaderboardScreen() }
            composable(BottomNavItem.Playlists.route) { PlaylistsScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen(onLogout = onLogout) }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Map,
        BottomNavItem.Search,
        BottomNavItem.Leaderboard,
        BottomNavItem.Playlists,
        BottomNavItem.Profile,
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Vraća na početnu destinaciju (Map) da ne bi gomilao stek
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Izbegava kreiranje nove destinacije ako je ista ponovo izabrana
                        launchSingleTop = true
                        // Vraća stanje ekrana kada se ponovo izabere
                        restoreState = true
                    }
                }
            )
        }
    }
}