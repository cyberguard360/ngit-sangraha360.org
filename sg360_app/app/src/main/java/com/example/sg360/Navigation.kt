package com.example.sg360

import VerificationScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sg360.auth.AuthViewModel
import com.example.sg360.dashboard.DashBoard
import com.example.sg360.dashboard.DashBoardViewModel
import com.example.sg360.auth.LoginScreen
import com.example.sg360.auth.RegisterScreen

/**
 * Navigation host for the SG360 application.
 *
 * This composable function defines the navigation graph for the app using Jetpack Compose's
 * Navigation component. It sets up routes for different screens and injects view models
 * using Hilt for dependency injection.
 *
 * @param navController The [NavHostController] used to manage navigation within the app.
 *                      Defaults to a new instance created via [rememberNavController].
 */
@Composable
fun Sg360NavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.login, // Start the app on the login screen
    ) {
        // Login Screen
        composable(Routes.login) {
            val authViewModel: AuthViewModel = hiltViewModel() // Inject AuthViewModel using Hilt
            LoginScreen(navController, authViewModel)
        }

        // Register Screen
        composable(Routes.register) {
            val authViewModel: AuthViewModel = hiltViewModel() // Inject AuthViewModel using Hilt
            RegisterScreen(navController, authViewModel)
        }

        // Verification Screen
        composable(Routes.verifyscreen) {
            VerificationScreen(navController)
        }

        // Dashboard Screen
        composable(Routes.dashboard) {
            val dashBoardViewModel: DashBoardViewModel = hiltViewModel() // Inject DashBoardViewModel using Hilt
            val appItemStates by dashBoardViewModel.installedApps.collectAsState() // Observe installed apps state

            DashBoard(
                appItemStates = appItemStates, // Pass the list of installed apps to the dashboard
                viewModel = dashBoardViewModel, // Pass the view model for additional functionality
                scanApp = { appItemState ->
                    // Trigger app scanning and analysis when requested
                    dashBoardViewModel.scanAndAnalyzeApp(appItemState)
                }
            )
        }
    }
}