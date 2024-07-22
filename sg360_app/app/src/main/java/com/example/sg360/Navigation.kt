package com.example.sg360

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sg360.dashboard.DashBoard
import com.example.sg360.dashboard.DashBoardViewModel
import com.example.sg360.network.Routes
import com.example.sg360.signinsignup.LoginScreen
import com.example.sg360.signinsignup.RegisterScreen
import com.example.sg360.signinsignup.SignInSignUpViewModel
import com.example.sg360.signinsignup.SignInUiState
import com.example.sg360.signinsignup.VerificationScreen

@Composable
fun Sg360NavHost(
    activity: MainActivity,
    navController: NavHostController = rememberNavController(),
) {
    // State for email and username
    var emailState by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = Routes.dashboard,
    ) {
        // Login Screen
        composable(Routes.login) {
            val signInViewModel: SignInSignUpViewModel = viewModel(
                factory = SignInSignUpViewModel.Factory
            )
            LoginScreen(
                navigateToRegister = { navController.navigate(Routes.register) },
                navigateToDashBoard = { navController.navigate(Routes.dashboard) },
                signInUiState = signInViewModel.signInUiState
            ) { email, password ->
                signInViewModel.getSignIn(
                    email,
                    password,
                    navigateToDashBoard = { navController.navigate(Routes.dashboard) }
                )
            }
        }

        // Register Screen
        composable(Routes.register) {
            val signInViewModel: SignInSignUpViewModel = viewModel(
                factory = SignInSignUpViewModel.Factory
            )
            RegisterScreen(
                navigateToLogin = { navController.navigate(Routes.dashboard) },
                navigateToVerify = { navController.navigate(Routes.dashboard) },
                signInUiState = signInViewModel.signInUiState
            ) { email, username, password, confirmpass, tc ->
                signInViewModel.getSignUp(
                    email,
                    username,
                    password,
                    confirmpass,
                    tc,
                    navigateToVerify = { navController.navigate(Routes.verifyscreen) }
                )
                emailState = email
                userName = username
            }
        }

        // Verification Screen
        composable(Routes.verifyscreen) {
            val signInViewModel: SignInSignUpViewModel = viewModel(
                factory = SignInSignUpViewModel.Factory
            )
            signInViewModel.signInUiState = SignInUiState.Loading
            VerificationScreen(
                signInUiState = signInViewModel.signInUiState,
            ) { otp ->
                signInViewModel.verifyOTP(
                    emailState,
                    otp,
                    navigateToLogin = { navController.navigate(Routes.login) }
                )
            }
        }

        // Dashboard Screen
        composable(Routes.dashboard) {
            val dashBoardViewModel: DashBoardViewModel = viewModel(
                factory = DashBoardViewModel.Factory
            )
            LaunchedEffect(Unit) {
                dashBoardViewModel.createAppList(activity.retrieveInstalledApps())
            }
            val appItemStates by dashBoardViewModel.appItemStates.collectAsState()
            val appScanner: AppScanner = AppScanner(activity)
            DashBoard(
                appItemStates = appItemStates,
                scanAllApps = { dashBoardViewModel.scanAllApps() },
                refresh = {
                    navController.navigate(Routes.dashboard) {}
                },
                scanApp = { packageName -> appScanner.scanApp(packageName) }
            )
        }
    }

}
