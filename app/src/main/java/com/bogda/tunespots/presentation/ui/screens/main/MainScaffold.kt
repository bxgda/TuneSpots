package com.bogda.tunespots.presentation.ui.screens.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
// OBAVEZNO DODAJTE OVAJ IMPORT
import com.bogda.tunespots.presentation.navigation.Routes
import com.bogda.tunespots.presentation.ui.components.BottomNavItem

@Composable
fun MainScaffold(
    mainNavController: NavHostController,
    topBar: @Composable () -> Unit = {},
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = {
            BottomNavigationBar(navController = mainNavController)
        }
    ) { innerPadding ->
        content(innerPadding)
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
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = isSelected,
                onClick = {
                    // Proverite da li su rute usklađene. Ako je screen.route "search_screen",
                    // a u Routes.kt je "search", aplikacija će pući.
                    // Osigurajte da BottomNavItem koristi rute iz Routes objekta.
                    navController.navigate(screen.route) {
                        // ISPRAVKA: Pop-up do HOME_GRAPH, ne do početka celog grafa
                        popUpTo(Routes.HOME_GRAPH) {
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
