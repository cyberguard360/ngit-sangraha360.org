package com.example.sg360.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the possible states of the sign-in process.
 */
sealed interface SignInUiState {
    /**
     * Indicates a successful sign-in operation.
     *
     * @property data Additional data related to the success (e.g., user token).
     */
    data class Success(val data: String) : SignInUiState

    /**
     * Indicates an error during the sign-in process.
     *
     * @property errorMessage A descriptive error message.
     */
    data class Error(val errorMessage: String) : SignInUiState

    /**
     * Indicates that the sign-in process is in progress.
     */
    data object Loading : SignInUiState

    /**
     * Indicates that the sign-in process is idle (no action is being performed).
     */
    data object Idle : SignInUiState
}

/**
 * ViewModel for managing authentication-related logic.
 *
 * This class handles user login, registration, and OTP verification. It interacts with the
 * [AuthRepository] to persist and retrieve user data.
 *
 * @param repository The [AuthRepository] instance used for data operations.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    // Tracks the email entered by the user
    var email by mutableStateOf("")

    // Tracks the password entered by the user
    var password by mutableStateOf("")

    // Tracks the username entered by the user during registration
    var username by mutableStateOf("")

    // Tracks the OTP entered by the user during verification
    var otp by mutableStateOf("")

    // Tracks any error messages to display to the user
    var errorMessage by mutableStateOf("")

    /**
     * Performs user login and navigates to the dashboard on success.
     *
     * This method compares the entered credentials with the stored credentials from the repository.
     * If the credentials match, the user is navigated to the dashboard; otherwise, an error message
     * is displayed.
     *
     * @param navController The navigation controller used to navigate to the dashboard.
     */
    fun login(navController: NavController) {
        viewModelScope.launch {
            // Retrieve stored user credentials from the repository
            val (storedEmail, storedPassword) = repository.getUser().first()

            // Validate the entered credentials
            if (email == storedEmail && password == storedPassword) {
                navController.navigate("dashboard") // Navigate to the dashboard on success
            } else {
                errorMessage = "Invalid credentials" // Update error message on failure
            }
        }
    }

    /**
     * Registers a new user and navigates to the OTP verification screen.
     *
     * This method saves the entered email and password to the repository and navigates the user
     * to the verification screen.
     *
     * @param navController The navigation controller used to navigate to the verification screen.
     */
    fun register(navController: NavController) {
        viewModelScope.launch {
            // Save the new user's credentials to the repository
            repository.saveUser(email, password)

            // Navigate to the OTP verification screen
            navController.navigate("verify")
        }
    }
}