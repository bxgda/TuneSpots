package com.bogda.tunespots.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.bogda.tunespots.presentation.auth.AuthMainScreen
import com.bogda.tunespots.presentation.auth.LoginScreen
import com.bogda.tunespots.presentation.auth.LoginViewModel
import com.bogda.tunespots.presentation.auth.RegisterScreen
import com.bogda.tunespots.presentation.auth.RegisterViewModel
import com.bogda.tunespots.presentation.ui.screens.main.LeaderboardScreen
import com.bogda.tunespots.presentation.ui.screens.main.MainScaffold
import com.bogda.tunespots.presentation.ui.screens.main.MapScreen
import com.bogda.tunespots.presentation.ui.screens.main.PlaylistsScreen
import com.bogda.tunespots.presentation.ui.screens.main.ProfileScreen
import com.bogda.tunespots.presentation.ui.screens.main.SearchScreen
import com.bogda.tunespots.presentation.ui.screens.playlists.AddPlaylistScreen
import com.bogda.tunespots.presentation.ui.screens.playlists.PlaylistScreen
import com.bogda.tunespots.presentation.ui.viewmodels.main.MapViewModel
import com.google.firebase.auth.FirebaseAuth

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val AUTH_MAIN = "auth_main"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val HOME_GRAPH = "home_graph"
    const val MAP = "map"
    const val SEARCH = "search"
    const val LEADERBOARD = "leaderboard"
    const val PLAYLISTS = "playlists"
    const val PROFILE = "profile"
    const val ADD_PLAYLIST_DIALOG = "add_playlist_dialog"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"

    fun playlistDetail(playlistId: String) = "playlist/$playlistId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val appNavController = rememberNavController()

    val firebaseAuth = FirebaseAuth.getInstance()
    val startDestination = if (firebaseAuth.currentUser != null) {
        Routes.HOME_GRAPH
    } else {
        Routes.AUTH_GRAPH
    }

    NavHost(
        navController = appNavController,
        startDestination = startDestination
    ) {
        navigation(startDestination = Routes.AUTH_MAIN, route = Routes.AUTH_GRAPH) {
            composable(Routes.AUTH_MAIN) {
                AuthMainScreen(
                    onLoginClick = { appNavController.navigate(Routes.LOGIN) },
                    onRegisterClick = { appNavController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.LOGIN) { backStackEntry ->
                val viewModel: LoginViewModel = hiltViewModel(backStackEntry)
                LoginScreen(
                    onNavigateBack = { appNavController.popBackStack() },
                    onLoginSuccess = {
                        appNavController.navigate(Routes.HOME_GRAPH) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(Routes.REGISTER) { backStackEntry ->
                val viewModel: RegisterViewModel = hiltViewModel(backStackEntry)
                RegisterScreen(
                    onNavigateBack = { appNavController.popBackStack() },
                    onRegisterSuccess = {
                        appNavController.navigate(Routes.HOME_GRAPH) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }
        }

        navigation(
            startDestination = Routes.MAP,
            route = Routes.HOME_GRAPH
        ) {
            composable(Routes.MAP) {
                backStackEntry ->
                val homeGraphBackStackEntry = remember(backStackEntry) {
                    appNavController.getBackStackEntry(Routes.HOME_GRAPH)
                }
                val mapViewModel: MapViewModel = hiltViewModel(homeGraphBackStackEntry)

                MainScaffoldWithContent(navController = appNavController, topBar = {
                    TopAppBar(title = { Text("Map") })
                }) { modifier ->
                    MapScreen(
                        modifier = modifier,
                        viewModel = mapViewModel,
                        onAddPlaylistClick = { appNavController.navigate(Routes.ADD_PLAYLIST_DIALOG) },
                        onPlaylistClick = { playlistId -> appNavController.navigate(Routes.playlistDetail(playlistId)) }
                    )
                }
            }
            composable(Routes.SEARCH) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    SearchScreen(modifier = modifier)
                }
            }
            composable(Routes.LEADERBOARD) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    LeaderboardScreen(modifier = modifier)
                }
            }
            composable(Routes.PLAYLISTS) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    PlaylistsScreen(modifier = modifier, navController = appNavController)
                }
            }
            composable(Routes.PROFILE) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    ProfileScreen(
                        modifier = modifier,
                        onLogout = {
                            appNavController.navigate(Routes.AUTH_GRAPH) {
                                popUpTo(Routes.HOME_GRAPH) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(route = Routes.ADD_PLAYLIST_DIALOG) {
                AddPlaylistScreen(
                    onNavigateBack = { appNavController.popBackStack() }
                )
            }

            composable(
                route = Routes.PLAYLIST_DETAIL,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) {
                PlaylistScreen(
                    onNavigateBack = { appNavController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun MainScaffoldWithContent(
    navController: NavHostController,
    topBar: @Composable () -> Unit = {},
    content: @Composable (modifier: Modifier) -> Unit
) {
    MainScaffold(
        mainNavController = navController,
        topBar = topBar
    ) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}
