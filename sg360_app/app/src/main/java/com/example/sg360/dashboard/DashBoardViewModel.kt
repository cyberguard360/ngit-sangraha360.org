package com.example.sg360.dashboard

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sg360.AppScanner
import com.example.sg360.MainActivity
import com.example.sg360.SG360Application
import com.example.sg360.TFLiteClassifier
import com.example.sg360.models.AllAppsData
import com.example.sg360.models.App
import com.example.sg360.models.AppDetail
import com.example.sg360.models.AppInfo
import com.example.sg360.models.AppItemState
import com.example.sg360.models.ClusterRequest
import com.example.sg360.models.RegisterRes
import com.example.sg360.network.SgApi
import com.example.sg360.utils.UserRepository
import com.example.sg360.utils.jsonSerializableToTensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

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
    data class Success(val photos: RegisterRes) : DashBoardUiState
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
    data object Loading : DashBoardUiState
}

/**
 * ViewModel for the Dashboard screen.
 *
 * @param userRepository Repository for user-related data operations.
 */
class DashBoardViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    // Mutable state flow to hold the list of app item states
    private val _appItemStates = MutableStateFlow<List<AppItemState>>(emptyList())
    val appItemStates: StateFlow<List<AppItemState>> = _appItemStates


    fun scanAllApps() {
        viewModelScope.launch {
            val pendingApps = _appItemStates.value
                .map { appItemState ->
                    App (
                        name = appItemState.appName,
                        packageName = appItemState.packageName,
                        source = "kuch bhi"
                    )
                }
            if (pendingApps.isNotEmpty()) {
                val requestData =  ClusterRequest(clusterName = "z_-3", apps= pendingApps)
                try {
                    val responseData = SgApi.retrofitService.sendClientData(requestData)
                    Log.d("DashBoardViewModel", "Cluster: ${responseData.Cluster}")
                    userRepository.saveClusterName(responseData.Cluster)
                    userRepository.saveEpochCount(responseData.Epoch)
                    userRepository.saveRoundCount(responseData.Round)
                    userRepository.saveModelData(responseData.modelData)
                    Log.d("DashBoardViewModel", "Model Data: ${responseData.modelData}")
                    newfunction()
                } catch (e: Exception) {
                    Log.e("DashBoardViewModel", "Error sending app data", e)
                }
            }
        }
    }

    fun newfunction() {
        viewModelScope.launch {
            val modelData = userRepository.modelData.firstOrNull() ?: ""
            val secretKey = userRepository.secretKey.firstOrNull() ?: ""
            if (modelData != null) {
                jsonSerializableToTensor(modelData, secretKey)
            }
        }
    }

//    /**
//     * Initiates a scan for all apps.
//     */
//    fun scanAllApps() {
//        viewModelScope.launch {
//            val pendingApps = _appItemStates.value
//                .filter { !it.isDone } // Exclude apps that are already done
//                .map { appItemState ->
//                    AppInfo(
//                        name = appItemState.appName,
//                        packageName = appItemState.packageName,
//                        source = "-" // Assuming the source URL is the same for all
//                    )
//                }
//
//            if (pendingApps.isNotEmpty()) {
//                val appData = AllAppsData(apps = pendingApps)
//                try {
//                    // Replace with actual API call to send app data and get response
//                    val response = SgApi.retrofitService.sendAppData(appData)
//                    val responseMsgs = response.msg // Ensure it's not null
//
//                    _appItemStates.value = _appItemStates.value.map { appItemState ->
//                        val matchingMsg =
//                            responseMsgs.firstOrNull { it.packageName == appItemState.packageName }
//                        if (matchingMsg != null) {
//                            when (matchingMsg.prediction) {
//                                "Benign" -> {
//                                    saveAppList(appItemState.packageName, "Success")
//                                    appItemState.copy(result = matchingMsg.prediction, isDone = true)
//                                }
//                                "Malware" -> {
//                                    saveAppList(appItemState.packageName, "Failure")
//                                    appItemState.copy(result = matchingMsg.prediction)
//                                }
//                                "Ambiguous" -> {
//                                    saveAppList(appItemState.packageName, "Unknown")
//                                    appItemState.copy(result = "Unknown")
//                                }
//                                else -> {
//                                    appItemState.copy(result = "Unknown")
//                                }
//                            }
//                        } else {
//                            appItemState.copy(result = "Unknown")
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e("DashBoardViewModel", "Error sending app data", e)
//                }
//            } else {
//                Log.d("DashBoardViewModel", "No apps to scan")
//            }
//        }
//    }

    /**
     * Creates the list of app item states from the provided list of app details.
     *
     * @param appDetail List of app details to create app item states from.
     */
    fun createAppList(appDetail: List<AppDetail>) {
        viewModelScope.launch {
            val appList = userRepository.appList.firstOrNull() ?: emptyList()
            _appItemStates.value = appDetail.map { appInfo ->
                val appEntry = appList.firstOrNull { it.endsWith(appInfo.packageName) }
                val resultValue = when {
                    appEntry?.startsWith(".malware.") == true -> "Malware"
                    appEntry?.startsWith(".benign.") == true -> "Benign"
                    appEntry?.startsWith(".unknown.") == true -> "Unknown"
                    else -> null
                }
                AppItemState(
                    appName = appInfo.appName,
                    packageName = appInfo.packageName,
                    icon = appInfo.icon,
                    isLoading = false,
                    result = resultValue,
                    isDone = (appEntry != null) && (resultValue == "Benign")
                )
            }
        }
    }

    /**
     * Saves the app list with a specific label based on the app name.
     *
     * @param appName Name of the app to save the list for.
     * @param label Label to assign to the app list ("Success", "Unknown", "Failure").
     */
    private fun saveAppList(appName: String, label: String) {
        viewModelScope.launch {
            try {
                when (label) {
                    "Success" -> {
                        userRepository.deleteAppFromList(".malware.$appName")
                        userRepository.deleteAppFromList(".unknown.$appName")
                        userRepository.saveAppList(".benign.$appName")
                    }
                    "Unknown" -> {
                        userRepository.deleteAppFromList(".benign.$appName")
                        userRepository.deleteAppFromList(".malware.$appName")
                        userRepository.saveAppList(".unknown.$appName")
                    }
                    else -> {
                        userRepository.deleteAppFromList(".benign.$appName")
                        userRepository.deleteAppFromList(".unknown.$appName")
                        userRepository.saveAppList(".malware.$appName")
                    }
                }
                Log.d("DashBoardViewModel", userRepository.appList.firstOrNull().toString())
            } catch (e: Exception) {
                Log.e("DashBoardViewModel", "Error saving app list for $appName", e)
            }
        }
    }

    /**
     * Factory for creating DashBoardViewModel instances.
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SG360Application)
                DashBoardViewModel(application.userRepository)
            }
        }
    }
}
