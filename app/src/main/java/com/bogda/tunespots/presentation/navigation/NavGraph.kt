package com.bogda.tunespots.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bogda.tunespots.presentation.auth.AuthMainScreen
import com.bogda.tunespots.presentation.auth.LoginScreen
import com.bogda.tunespots.presentation.auth.RegisterScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.bogda.tunespots.presentation.auth.LoginViewModel
import com.bogda.tunespots.presentation.auth.RegisterViewModel
import com.bogda.tunespots.presentation.ui.screens.main.MainScaffold
import com.google.firebase.auth.FirebaseAuth

// Definišemo rute kao konstante da bismo izbegli greške u kucanju
object Routes {
    const val AUTH_MAIN = "auth_main"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home_scaffold" // Ruta za glavni ekran nakon prijave
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    val firebaseAuth = FirebaseAuth.getInstance()
    val startDestination = if (firebaseAuth.currentUser != null) {
        Routes.HOME // Ako jeste, počni sa Home ekrana
    } else {
        Routes.AUTH_MAIN // Ako nije, počni sa ekrana za autentifikaciju
    }

    NavHost(
        navController = navController,
        startDestination = startDestination // Početni ekran je naš novi AuthMainScreen
    ) {
        // --- Ekrani za Autentifikaciju ---

        composable(Routes.AUTH_MAIN) {
            AuthMainScreen(
                onLoginClick = {
                    navController.navigate(Routes.LOGIN)
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.LOGIN) { backStackEntry ->
            val viewModel: LoginViewModel = hiltViewModel(backStackEntry)
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_MAIN) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Routes.REGISTER) { backStackEntry ->
            val viewModel: RegisterViewModel = hiltViewModel(backStackEntry)
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_MAIN) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // --- Glavni ekran aplikacije (nakon prijave) ---

        composable(Routes.HOME) {
            MainScaffold( // <-- ZAMENI MainScreen sa MainScaffold
                onLogout = {
                    // Očisti ceo backstack i idi na AUTH_MAIN
                    navController.navigate(Routes.AUTH_MAIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    }
}
