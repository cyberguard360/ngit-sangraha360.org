package com.example.sg360

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ComponentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sg360.dashboard.DashBoard
import com.example.sg360.dashboard.DashBoardViewModel
import com.example.sg360.data.UserRepository
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
    var emailState by remember {
        mutableStateOf("")
    }

    var userName by remember {
        mutableStateOf("")
    }

    NavHost(
        navController = navController,
        startDestination = Routes.login,
    ) {
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
                ) // This function should trigger sign-in process
            }
        }

        composable(Routes.register) {
            val signInViewModel: SignInSignUpViewModel = viewModel(
                factory = SignInSignUpViewModel.Factory
            )
            RegisterScreen(
                navigateToLogin = { navController.navigate(Routes.dashboard) },
                navigateToVerify = { navController.navigate(Routes.dashboard) },
                signInUiState = signInViewModel.signInUiState
            ) {
                    email, username, password, confirmpass, tc  ->
                signInViewModel.getSignUp(
                    email,
                    username,
                    password,
                    confirmpass,
                    tc, navigateToVerify = { navController.navigate(Routes.verifyscreen) }
                )

                emailState = email
                userName = username
            }
        }

        composable(Routes.verifyscreen) {
            val signInViewModel: SignInSignUpViewModel = viewModel(
                factory = SignInSignUpViewModel.Factory
            )
            signInViewModel.signInUiState = SignInUiState.Loading
            VerificationScreen(
                navigateToDashBoard = { navController.navigate(Routes.dashboard) },
                signInUiState = signInViewModel.signInUiState,
            ) { otp ->
                signInViewModel.verifyOTP(
                    emailState,
                    otp,
                    userName,
                    navigateToLogin = { navController.navigate(Routes.login) }
                )
            }
        }

        composable(Routes.dashboard) {
            val dashBoardViewModel: DashBoardViewModel = viewModel(
                factory = DashBoardViewModel.Factory
            )
            dashBoardViewModel.saveAppList(activity)
            dashBoardViewModel.fetchAppList()
            DashBoard(
                apkNames = dashBoardViewModel.appListState.value,
                dashBoardUi = dashBoardViewModel.dashBoardUiState,
            )
            {
                appName -> dashBoardViewModel.sendData(appName, activity)
            }
        }
    }
}