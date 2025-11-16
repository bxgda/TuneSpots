package com.bogda.tunespots.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
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
import com.bogda.tunespots.presentation.ui.viewmodels.main.MapViewModel
import com.google.firebase.auth.FirebaseAuth

// Definišemo SVE rute na jednom mestu radi preglednosti i sigurnosti
object Routes {
    // Auth rute
    const val AUTH_GRAPH = "auth_graph"
    const val AUTH_MAIN = "auth_main"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Rute glavne aplikacije (ugnježdeni graf)
    const val HOME_GRAPH = "home_graph"
    const val MAP = "map"
    const val SEARCH = "search"
    const val LEADERBOARD = "leaderboard"
    const val PLAYLISTS = "playlists"
    const val PROFILE = "profile"
    const val ADD_PLAYLIST_DIALOG = "add_playlist_dialog"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    // Jedan, glavni NavController za celu aplikaciju
    val appNavController = rememberNavController()

    val firebaseAuth = FirebaseAuth.getInstance()
    // Određuje da li da se počne sa autentifikacijom ili glavnim delom aplikacije
    val startDestination = if (firebaseAuth.currentUser != null) {
        Routes.HOME_GRAPH // Ako je korisnik ulogovan, ide direktno na glavni deo
    } else {
        Routes.AUTH_GRAPH // Ako nije, ide na graf za autentifikaciju
    }

    NavHost(
        navController = appNavController,
        startDestination = startDestination
    ) {
        // --- GRAF ZA AUTENTIFIKACIJU ---
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
                        // Nakon uspešnog logina, idi na HOME_GRAPH i obriši ceo AUTH_GRAPH
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
                        // Nakon uspešne registracije, idi na HOME_GRAPH i obriši ceo AUTH_GRAPH
                        appNavController.navigate(Routes.HOME_GRAPH) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }
        }

        // --- GLAVNI DEO APLIKACIJE (ugnježdeni graf unutar Scaffold-a) ---
        navigation(
            startDestination = Routes.MAP, // Početni ekran unutar Scaffold-a
            route = Routes.HOME_GRAPH
        ) {
            // Glavni ekrani koji se prikazuju unutar Scaffold-a
            composable(Routes.MAP) {
                backStackEntry -> // 1. Dohvatamo backStackEntry za MapScreen

                // 2. Pronalazimo roditeljski NavBackStackEntry za ceo HOME_GRAPH
                val homeGraphBackStackEntry = remember(backStackEntry) {
                    appNavController.getBackStackEntry(Routes.HOME_GRAPH)
                }

                // 3. Kreiramo ViewModel vezan za životni vek HOME_GRAPH-a
                val mapViewModel: MapViewModel = hiltViewModel(homeGraphBackStackEntry)

                MainScaffoldWithContent(navController = appNavController, topBar = {
                    TopAppBar(title = { Text("Map") })
                }) { modifier ->
                    MapScreen(
                        modifier = modifier, // Prosleđujemo padding iz Scaffold-a
                        viewModel = mapViewModel, // Prosleđujemo ViewModel
                        onAddPlaylistClick = { appNavController.navigate(Routes.ADD_PLAYLIST_DIALOG) }
                    )
                }
            }
            composable(Routes.SEARCH) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    // ISPRAVKA: Prosledi modifier
                    SearchScreen(modifier = modifier)
                }
            }
            composable(Routes.LEADERBOARD) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    // ISPRAVKA: Prosledi modifier
                    LeaderboardScreen(modifier = modifier)
                }
            }
            composable(Routes.PLAYLISTS) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    // ISPRAVKA: Prosledi modifier
                    PlaylistsScreen(modifier = modifier)
                }
            }
            composable(Routes.PROFILE) {
                MainScaffoldWithContent(navController = appNavController) { modifier ->
                    ProfileScreen(
                        // ISPRAVKA: Prosledi modifier
                        modifier = modifier,
                        onLogout = {
                            // Logika odjave, vraća na početak AUTH_GRAPH-a
                            appNavController.navigate(Routes.AUTH_GRAPH) {
                                popUpTo(Routes.HOME_GRAPH) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // --- Ekran za dodavanje plejliste (ne dijalog) ---
            composable(route = Routes.ADD_PLAYLIST_DIALOG) { backStackEntry ->
                AddPlaylistScreen(
                    onNavigateBack = { appNavController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Pomoćna Composable funkcija koja obavija svaki glavni ekran sa [MainScaffold]-om.
 * Ovo sprečava ponavljanje koda i prosleđuje potrebne parametre.
 */
@Composable
private fun MainScaffoldWithContent(
    navController: NavHostController,
    // ISPRAVKA: content sada prima modifier
    topBar: @Composable () -> Unit = {},
    content: @Composable (modifier: Modifier) -> Unit
) {
    MainScaffold(
        mainNavController = navController,
        topBar = topBar
    ) { paddingValues -> // `MainScaffold` nam daje `paddingValues`
        // Mi kreiramo modifier od tih vrednosti i prosleđujemo ga dalje
        content(Modifier.padding(paddingValues))
    }
}
