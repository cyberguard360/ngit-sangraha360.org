package com.example.sg360.signinsignup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sg360.SG360Application
import com.example.sg360.utils.UserRepository
import com.example.sg360.models.*
import com.example.sg360.network.SgApi
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException

/**
 * Represents the state of the sign-in and sign-up screens.
 */
sealed interface SignInUiState {
    data class Success(val photos: String) : SignInUiState
    data class Error(val errorMessage: String) : SignInUiState
    data object Loading : SignInUiState
}

/**
 * ViewModel for the sign-in and sign-up screens.
 *
 * @param userRepository The repository for user data.
 */
class SignInSignUpViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SG360Application)
                SignInSignUpViewModel(application.userRepository)
            }
        }
    }

    var signInUiState: SignInUiState by mutableStateOf(SignInUiState.Loading)

    /**
     * Handles the sign in process.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param navigateToDashBoard The function to navigate to the dashboard screen.
     */
    fun getSignIn(email: String, password: String, navigateToDashBoard: () -> Unit) {
        viewModelScope.launch {
            signInUiState = try {
                val loginData = LoginBod(email, password)
                val response = SgApi.retrofitService.login(loginData)

                SignInUiState.Loading.also {
                    signInUiState = SignInUiState.Success(response.toString())
                    userRepository.saveAccessToken(response.accessToken)
                    userRepository.saveRefreshToken(response.refreshToken)

                    navigateToDashBoard()
                }
            } catch (e: IOException) {
                // Update UI state to Error if an IOException occurs
                SignInUiState.Error("No Network")
            }catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    404 -> "Unauthorized: Please check your credentials and try again."
                    502 -> "Server not found."
                    else -> "An error occurred while signing in. Status code: $e.code()"
                }
                SignInUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Handles the sign up process.
     *
     * @param email The user's email.
     * @param username The user's username.
     * @param password The user's password.
     * @param confirmPass The user's confirm password.
     * @param tc The user's acceptance of the T&C.
     * @param navigateToVerify The lambda function to be invoked after a successful sign up.
     */

    fun getSignUp(email: String, username: String, password: String,  confirmPass: String, tc: String, navigateToVerify: () -> Unit) {
        viewModelScope.launch {
            signInUiState = try {
                val regisData = RegisterBod(email, username, password, confirmPass, tc)
                val response = SgApi.retrofitService.register(regisData)

                SignInUiState.Loading.also {
                    signInUiState = SignInUiState.Success(response.toString())
                    userRepository.saveUsername(username)

                    navigateToVerify()
                }
            } catch (e: IOException) {
                // Log the error for debugging purposes
                println(e)

                // Update UI state to Error if an IOException occurs
                SignInUiState.Error("No internet")
            }catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    404 -> "Unauthorized: Please check your credentials and try again."
                    502 -> "Server not found."
                    400 -> "email already exists"
                    else -> "An error occurred while signing in. Status code: $e.code()"
                }
                SignInUiState.Error(errorMessage)
            }
        }
    }


    /**
     * Verifies the OTP for a given email and username.
     *
     * @param email The email of the user.
     * @param otp The OTP to be verified.
     * @param navigateToLogin The lambda function to navigate to the login screen after successful verification.
     * @return void
     */
    fun verifyOTP(email: String, otp: String, navigateToLogin: () -> Unit) {
        viewModelScope.launch {
            signInUiState = try {
                val verifyData = VerifyBod(email, otp)
                val response = SgApi.retrofitService.verify(verifyData)

                SignInUiState.Loading.also {
                    signInUiState = SignInUiState.Success(response.toString())

                    navigateToLogin()
                }
            } catch (e: IOException) {
                // Log the error for debugging purposes
                println(e)

                // Update UI state to Error if an IOException occurs
                SignInUiState.Error("No internet.")
            }catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "Incorrect OTP. Please check again"
                    502 -> "Server not found."
                    else -> "An error occurred while signing in. Status code: $e.code()"
                }
                SignInUiState.Error(errorMessage)
            }
        }
    }
}