package com.example.sg360.dashboard

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sg360.data.AppAnalysisData
import com.example.sg360.data.AppAnalysisDataStore
import com.example.sg360.models.AnalysisResult
import com.example.sg360.models.AnalysisStatusResponse
import com.example.sg360.models.AppItemState
import com.example.sg360.models.StaticAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject


@HiltViewModel
class DashBoardViewModel @Inject constructor(
    private val repository: DashBoardRepository, // Repository for app analysis operations
    private val analysisDataStore: AppAnalysisDataStore, // DataStore for caching analysis results
    application: Application // Application context for accessing system resources
) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext // Application context
    private val TAG = "DashBoardViewModel" // Log tag for debugging

    // Mutable state flow to manage the UI state of the dashboard
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)

    // Public state flow exposing the current UI state to the UI layer
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Mutable state flow to manage the list of installed apps
    private val _installedApps = MutableStateFlow<List<AppItemState>>(emptyList())

    // Public state flow exposing the list of installed apps to the UI layer
    val installedApps: StateFlow<List<AppItemState>> = _installedApps.asStateFlow()

    /**
     * Initializes the ViewModel by loading the list of installed apps.
     *
     * This method uses a coroutine to load installed apps asynchronously and updates the
     * `_installedApps` state flow with the results.
     */
    init {
        viewModelScope.launch {
            loadInstalledApps(context).collect { apps ->
                _installedApps.value = apps
            }
        }
    }

    /**
     * Resets the UI state to the idle state.
     *
     * This method is used to clear the current analysis state and return the dashboard to its
     * initial idle state.
     */
    fun resetState() {
        _uiState.value = DashboardUiState.Idle
    }

    /**
     * Scans and analyzes an app by performing static and dynamic analysis.
     *
     * This method orchestrates the entire analysis process for a given app:
     * 1. Checks for locally stored analysis results to avoid redundant computations.
     * 2. Performs static analysis if no valid local data exists.
     * 3. Checks the server for existing dynamic analysis results.
     * 4. Uploads APK files and performs dynamic analysis if necessary.
     *
     * @param appItemState The [AppItemState] object representing the app to analyze.
     */
    fun scanAndAnalyzeApp(appItemState: AppItemState) {
        // Update UI to indicate the start of the analysis process
        _uiState.value = DashboardUiState.StaticAnalysisInProgress("Preparing scan...")

        viewModelScope.launch {
            // Step 1: Check for locally stored analysis
            val localAnalysisData = analysisDataStore.getAnalysisResult(appItemState.packageName).firstOrNull()
            val isLocalDataValid = localAnalysisData?.let {
                analysisDataStore.hasValidAnalysisResult(it)
            } ?: false

            // If we have complete valid analysis, show final results
            if (localAnalysisData != null && isLocalDataValid && localAnalysisData.dynamicAnalysisResult != null) {
                Log.d(TAG, "Using complete locally stored analysis for ${appItemState.packageName}")
                _uiState.value = DashboardUiState.DynamicResult(
                    staticAnalysisResult = localAnalysisData.staticAnalysisResult,
                    dynamicAnalysisResult = localAnalysisData.dynamicAnalysisResult,
                    timestamp = localAnalysisData.timestamp ?: System.currentTimeMillis()
                )
                return@launch
            }

            // Step 2: Perform Static Analysis if needed
            _uiState.value = DashboardUiState.StaticAnalysisInProgress("Analyzing app code...")
            val staticAnalysisResult: StaticAnalysisResult = if (localAnalysisData?.staticAnalysisResult != null) {
                Log.d(TAG, "Using stored static analysis for ${appItemState.packageName}")
                localAnalysisData.staticAnalysisResult
            } else {
                Log.d(TAG, "Performing static analysis for ${appItemState.packageName}")
                val result = repository.performStaticAnalysis(appItemState.packageName)

                // Save static analysis immediately
                val analysisData = AppAnalysisData(
                    packageName = appItemState.packageName,
                    staticAnalysisResult = result,
                    dynamicAnalysisResult = null,
                    timestamp = System.currentTimeMillis()
                )
                analysisDataStore.saveAnalysisResult(analysisData)
                Log.d(TAG, "Saved static analysis for ${appItemState.packageName}")

                result
            }

            // Update UI with static analysis results
            _uiState.value = DashboardUiState.StaticResult(
                result = staticAnalysisResult,
                timestamp = System.currentTimeMillis()
            )

            // Step 3: Check if Dynamic Analysis exists on server
            val analysisStatus = repository.checkAnalysisStatus(appItemState.packageName)

            if (analysisStatus.alreadyAnalyzed && analysisStatus.previousResults != null) {
                Log.d(TAG, "Using server analysis for ${appItemState.packageName}")

                // Store final result locally
                val finalAnalysisData = AppAnalysisData(
                    packageName = appItemState.packageName,
                    staticAnalysisResult = staticAnalysisResult,
                    dynamicAnalysisResult = analysisStatus.previousResults,
                    timestamp = System.currentTimeMillis()
                )
                analysisDataStore.saveAnalysisResult(finalAnalysisData)

                _uiState.value = DashboardUiState.DynamicResult(
                    staticAnalysisResult = staticAnalysisResult,
                    dynamicAnalysisResult = analysisStatus.previousResults,
                    timestamp = System.currentTimeMillis()
                )
                return@launch
            }

            // If we get here, we need to perform dynamic analysis
            _uiState.value = DashboardUiState.DynamicAnalysisInProgress(
                stage = "Collecting APK files...",
                staticAnalysisResult = staticAnalysisResult
            )

            val apkFiles = repository.getApkFilesForPackage(appItemState)

            if (apkFiles.isEmpty()) {
                _uiState.value = DashboardUiState.StaticResult(
                    result = staticAnalysisResult,
                    timestamp = System.currentTimeMillis(),
                    dynamicError = "No APK files found for ${appItemState.packageName}"
                )
                return@launch
            }

            try {
                repository.uploadApkFiles(
                    appItemState.name,
                    appItemState.packageName,
                    apkFiles,
                    staticAnalysisResult // Reusing static analysis result instead of recalculating
                ).collect { progress ->
                    when (progress) {
                        is UploadProgress.Started -> {
                            _uiState.value = DashboardUiState.DynamicAnalysisInProgress(
                                stage = "Starting upload...",
                                staticAnalysisResult = staticAnalysisResult
                            )
                        }

                        is UploadProgress.Uploading -> {
                            _uiState.value = DashboardUiState.DynamicAnalysisInProgress(
                                stage = "Uploading: ${progress.percentComplete}%",
                                staticAnalysisResult = staticAnalysisResult
                            )
                        }

                        is UploadProgress.PreparingDynamicAnalysis -> {
                            _uiState.value = DashboardUiState.DynamicAnalysisInProgress(
                                stage = "Preparing for dynamic analysis...",
                                staticAnalysisResult = staticAnalysisResult
                            )
                        }

                        is UploadProgress.Completed -> {
                            val finalAnalysisData = AppAnalysisData(
                                packageName = appItemState.packageName,
                                staticAnalysisResult = staticAnalysisResult,
                                dynamicAnalysisResult = progress.result.previousResults,
                                timestamp = System.currentTimeMillis()
                            )
                            analysisDataStore.saveAnalysisResult(finalAnalysisData)
                            Log.d(TAG, "Saved final analysis for ${appItemState.packageName}")

                            _uiState.value = DashboardUiState.DynamicResult(
                                staticAnalysisResult = staticAnalysisResult,
                                dynamicAnalysisResult = progress.result.previousResults!!,
                                timestamp = System.currentTimeMillis()
                            )
                        }

                        is UploadProgress.Error -> {
                            if (_uiState.value is DashboardUiState.DynamicAnalysisInProgress) {
                                val currentState = _uiState.value as DashboardUiState.DynamicAnalysisInProgress
                                _uiState.value = DashboardUiState.StaticResult(
                                    result = currentState.staticAnalysisResult,
                                    timestamp = System.currentTimeMillis(),
                                    dynamicError = progress.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed: ${e.message}")
            }
        }
    }

    /**
     * Clears all cached analysis results from the DataStore.
     *
     * This method removes all stored analysis data, ensuring that subsequent analyses start fresh.
     */
    fun clearAllCachedResults() {
        viewModelScope.launch {
            analysisDataStore.clearAllAnalysisResults()
        }
    }

    /**
     * Clears the cached analysis result for a specific package.
     *
     * This method removes the analysis data associated with the given package name from the DataStore.
     *
     * @param packageName The package name of the app to clear cached results for.
     */
    fun clearCachedResult(packageName: String) {
        viewModelScope.launch {
            analysisDataStore.removeAnalysisResult(packageName)
        }
    }

    /**
     * Loads the list of installed apps on the device.
     *
     * This method queries the system's package manager to retrieve all installed applications,
     * filters out non-launchable apps and the current app, and maps the results into a list of
     * [AppItemState] objects. The list is sorted alphabetically by app name length.
     *
     * @param context The application context used to access the package manager.
     * @return A [Flow] emitting the list of installed apps as [AppItemState] objects.
     */
    private fun loadInstalledApps(context: Context): Flow<List<AppItemState>> = flow {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter {
                // Filter out apps without launch intents and the current app
                packageManager.getLaunchIntentForPackage(it.packageName) != null &&
                        it.packageName != context.packageName
            }
            .map { appInfo ->
                // Map each app info to an AppItemState object
                AppItemState(
                    name = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName,
                    icon = appInfo.loadIcon(packageManager).toBitmap().asImageBitmap(),
                    sourceDir = appInfo.sourceDir,
                    splitSourceDirs = appInfo.splitSourceDirs
                )
            }
            .sortedBy { it.name.length } // Sort apps by name length
        emit(installedApps) // Emit the final list of installed apps
    }.flowOn(Dispatchers.IO) // Perform the operation on the IO dispatcher for efficiency
}