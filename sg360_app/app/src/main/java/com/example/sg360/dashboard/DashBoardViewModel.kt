package com.example.sg360.dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sg360.MainActivity
import com.example.sg360.SG360Application
import com.example.sg360.data.UserRepository
import com.example.sg360.models.PredictResponse
import com.example.sg360.network.SgApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Represents the UI state of the Dashboard screen.
 *
 * Sealed interface with three possible states:
 * - [Success]: Dashboard is loaded and successfully displayed with the list of photos.
 * - [Error]: Error occurred while loading the dashboard.
 * - [Loading]: Dashboard is currently loading.
 */
sealed interface DashBoardUiState {
    /**
     * Success state of the Dashboard UI.
     *
     * @property photos The list of photos to display.
     */
    data class Success(val photos: PredictResponse) : DashBoardUiState
    /**
     * Error state of the Dashboard UI.
     *
     * @property errorMessage The error message to display.
     */
    data class Error(val errorMessage: String) : DashBoardUiState
    /**
     * Loading state of the Dashboard UI.
     *
     * Indicates that the Dashboard is currently loading.
     */
    object Loading : DashBoardUiState
}
class DashBoardViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    // State holding the list of installed apps
    val appListState: MutableState<List<String>> = mutableStateOf(emptyList())
    
    // State holding the current UI state of the Dashboard
    var dashBoardUiState: DashBoardUiState by mutableStateOf(DashBoardUiState.Loading)
        private set
    
    /**
     * Saves the list of installed apps obtained from the provided activity by launching a coroutine
     * to iterate through the list and save each app name using the UserRepository.
     */
    fun saveAppList(activity: MainActivity) {
        val installedApps = activity.retrieveInstalledApps()
        viewModelScope.launch {
            installedApps.forEach { appName ->
                userRepository.saveAppList(appName)
            }
        }
    }
    
    /**
     * Fetches the list of installed apps saved in the UserRepository and updates the appListState.
     */
    fun fetchAppList() {
        viewModelScope.launch {
            userRepository.appList.collect { appList ->
                appListState.value = appList?.toList() ?: emptyList() // Handle nullable appList
            }
        }
    }
    
    companion object {
        /**
         * A factory for creating DashBoardViewModel instances.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SG360Application)
                DashBoardViewModel(application.userRepository)
            }
        }
    }
    
    /**
     * Sends data to the Sangraha API and updates the dashBoardUiState with the response.
     */
    fun sendData(selectedApp: String, activity: MainActivity) {
        viewModelScope.launch {
            try {
                val accessToken = userRepository.accessToken.firstOrNull()
                println("HElerererer$accessToken")
                println(accessToken)
                val headerBody = "Bearer $accessToken"
                val username = userRepository.username.firstOrNull()
                val dataBody = activity.appFeatures(username, selectedApp)
                val response = SgApi.retrofitService.sendData(dataBody,headerBody)
                println(response.toString())
                dashBoardUiState = DashBoardUiState.Success(response) // Update state with response
            } catch (e: IOException) {
                println(e)
                dashBoardUiState = DashBoardUiState.Error("No internet.")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "Incorrect OTP. Please check again"
                    502 -> "Server not found."
                    else -> "An error occurred while signing in. Status code: ${e.code()}"
                }
                dashBoardUiState = DashBoardUiState.Error(errorMessage)
            }
        }
    }
}
