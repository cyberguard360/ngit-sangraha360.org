package com.example.sg360.dashboard

import android.content.Context
import android.util.Log
import com.example.sg360.data.AppAnalysisData
import com.example.sg360.data.AppAnalysisDataStore
import com.example.sg360.models.AnalysisResult
import com.example.sg360.models.AnalysisStatusResponse
import com.example.sg360.models.AppItemState
import com.example.sg360.models.CheckRequestBody
import com.example.sg360.models.StaticAnalysisResult
import com.example.sg360.network.SgApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.math.min

class DashBoardRepository @Inject constructor(
    @ApplicationContext private val context: Context, // Application context for accessing system resources
    private val analysisDataStore: AppAnalysisDataStore // DataStore for caching analysis results
) {

    companion object {
        /**
         * The size of each chunk for uploading files, set to 10MB.
         *
         * This constant defines the maximum size of each file chunk during the upload process.
         * It ensures efficient handling of large files by breaking them into smaller parts.
         */
        private const val CHUNK_SIZE = 10 * 1024 * 1024L // 10MB chunks

        /**
         * Log tag used for debugging purposes.
         *
         * This constant is used as the tag for logging messages related to the repository.
         */
        private const val TAG = "DashBoardRepository"
    }

    /**
     * Performs static analysis on the app.
     *
     * This method uses the [AppScanner] class to scan the app and extract raw features. It then
     * constructs and returns a [StaticAnalysisResult] object containing the malware prediction and
     * various counts derived from the raw features.
     *
     * @param packageName The package name of the app to analyze.
     * @return A [StaticAnalysisResult] object containing the results of the static analysis.
     */
    fun performStaticAnalysis(packageName: String): StaticAnalysisResult {
        // Initialize the app scanner and perform the scan
        val appScanner = AppScanner(context)
        val malwarePrediction = appScanner.scanApp(packageName)

        // Extract the raw features used in the scan
        val rawFeatures = appScanner.getRawFeatures(packageName, context)

        // Construct and return the static analysis result
        return StaticAnalysisResult(
            malwarePrediction = malwarePrediction,
            permissionCount = rawFeatures.getOrNull(9)?.toInt() ?: 0, // Permissions count
            activityCount = rawFeatures.getOrNull(6)?.toInt() ?: 0, // Activities count
            serviceCount = (rawFeatures.getOrNull(8)?.toInt() ?: 0) / 2, // Services count
            receiverCount = (rawFeatures.getOrNull(8)?.toInt() ?: 0) / 2, // Broadcast receivers count
            providerCount = rawFeatures.getOrNull(3)?.toInt() ?: 0, // Content providers count
            featureCount = rawFeatures.getOrNull(4)?.toInt() ?: 0, // Features count
            rawFeatures = rawFeatures // Raw feature data
        )
    }

    /**
     * Checks if the app has already been analyzed, first locally and then remotely if needed.
     *
     * This method queries the server to determine whether the app has already been analyzed.
     * If the API call fails, it assumes the app needs to be analyzed.
     *
     * @param packageName The package name of the app to check.
     * @return An [AnalysisStatusResponse] object indicating whether the app has been analyzed
     *         and containing any previous results if available.
     */
    suspend fun checkAnalysisStatus(packageName: String): AnalysisStatusResponse {
        // If no valid local result, check API
        return try {
            // Make the API call to check the analysis status
            val response = SgApi.retrofitService.checkAnalysisStatus(CheckRequestBody(packageName))
            AnalysisStatusResponse(
                alreadyAnalyzed = response.status, // Whether the app has been analyzed
                previousResults = response // Previous analysis results, if any
            )
        } catch (e: Exception) {
            // Log the error and assume the app needs to be analyzed
            Log.e(TAG, "Error checking analysis status", e)
            AnalysisStatusResponse(false, null) // Assume not analyzed if API call fails
        }
    }

    /**
     * Gets all APK files for a package.
     *
     * This method collects the base APK file and any split APK files associated with the app.
     * It logs detailed information about each APK file found and handles errors gracefully.
     *
     * @param appItemState The [AppItemState] object representing the app.
     * @return A list of [File] objects representing the APK files, or an empty list if an error occurs.
     */
    fun getApkFilesForPackage(appItemState: AppItemState): List<File> {
        try {
            val apkFiles = mutableListOf<File>()
            Log.d(TAG, "Collecting APK files for package: ${appItemState.packageName}")

            // Add the base APK file
            appItemState.sourceDir.let {
                val sourceFile = File(it)
                Log.d(TAG, "Source APK found: ${sourceFile.absolutePath}, Size: ${sourceFile.length()} bytes")
                apkFiles.add(sourceFile)
            }

            // Add any split APK files
            appItemState.splitSourceDirs?.let { splitDirs ->
                Log.d(TAG, "Split APKs found: ${splitDirs.size}")
                splitDirs.forEach { splitPath ->
                    val splitFile = File(splitPath)
                    Log.d(TAG, "Split APK: ${splitFile.absolutePath}, Size: ${splitFile.length()} bytes")
                    apkFiles.add(splitFile)
                }
            }

            Log.d(TAG, "Total APK files collected: ${apkFiles.size}")
            return apkFiles
        } catch (e: Exception) {
            // Log the error and return an empty list
            Log.e(TAG, "Error collecting APK files", e)
            return emptyList()
        }
    }

    /**
     * Uploads APK files with progress reporting and saves results to local storage.
     *
     * This method uploads APK files for an app in chunks, reports upload progress via [UploadProgress],
     * and saves the final analysis results to the local data store. It handles errors gracefully and
     * ensures that the upload process is tracked and reported accurately.
     *
     * @param appName The name of the app being analyzed.
     * @param packageName The package name of the app being analyzed.
     * @param apkFiles A list of APK files to upload.
     * @param staticAnalysisResult The static analysis result associated with the app.
     * @return A [Flow] emitting [UploadProgress] updates during the upload process.
     */
    fun uploadApkFiles(
        appName: String,
        packageName: String,
        apkFiles: List<File>,
        staticAnalysisResult: StaticAnalysisResult
    ): Flow<UploadProgress> = channelFlow {
        // If no APK files are provided, emit an error and terminate
        if (apkFiles.isEmpty()) {
            send(UploadProgress.Error("No APK files found for $packageName"))
            return@channelFlow
        }

        var uploadedSize = 0L // Tracks the total size of uploaded data
        val totalSize = apkFiles.sumOf { it.length() } // Total size of all APK files
        var tempDynamicAnalysisResult: AnalysisStatusResponse? = null // Stores the final analysis result

        // Notify that the upload has started
        send(UploadProgress.Started)

        // Iterate through each APK file and upload it
        apkFiles.forEachIndexed { index, file ->
            try {
                val isLastChunk = index == apkFiles.size - 1 // Check if this is the last file

                // Perform the upload in a background thread
                val analysisResult = withContext(Dispatchers.IO) {
                    uploadLargeApk(
                        file,
                        appName,
                        packageName,
                        isLastChunk
                    ) { chunkSize ->
                        // Update the uploaded size and calculate progress percentage
                        uploadedSize += chunkSize
                        val progress = ((uploadedSize.toFloat() / totalSize) * 100).toInt()

                        // Emit the current upload progress
                        trySend(UploadProgress.Uploading(progress))
                    }
                }

                // Store the analysis result if this is the last chunk
                if (isLastChunk) {
                    tempDynamicAnalysisResult = analysisResult
                }

                // Notify that dynamic analysis preparation is starting for the penultimate file
                if (index == apkFiles.size - 2) {
                    send(UploadProgress.PreparingDynamicAnalysis)
                }

            } catch (e: Exception) {
                // Log the error and emit an error state
                Log.e(TAG, "Error uploading file", e)
                send(UploadProgress.Error("Error uploading file: ${e.message}"))
                return@channelFlow
            }
        }

        // Handle the case when no analysis result was obtained
        if (tempDynamicAnalysisResult == null) {
            send(UploadProgress.Error("Failed to obtain analysis results for $packageName"))
            return@channelFlow
        }

        // Create a final local variable for safe usage in the closure
        val finalDynamicAnalysisResult = tempDynamicAnalysisResult

        // Save the analysis results to the local data store
        if (finalDynamicAnalysisResult?.previousResults != null) {
            try {
                val analysisData = AppAnalysisData(
                    packageName = packageName,
                    staticAnalysisResult = staticAnalysisResult,
                    dynamicAnalysisResult = finalDynamicAnalysisResult.previousResults
                )
                analysisDataStore.saveAnalysisResult(analysisData)
                Log.d(TAG, "Saved analysis results to local storage for $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving analysis results to local storage", e)
            }
        }

        // Notify that the upload and analysis are complete
        send(UploadProgress.Completed(finalDynamicAnalysisResult!!))
    }.flowOn(Dispatchers.IO) // Ensure the flow runs on the IO dispatcher for efficiency

    /**
     * Uploads a large APK file in chunks and reports progress.
     *
     * This method breaks the APK file into smaller chunks based on the predefined `CHUNK_SIZE`,
     * uploads each chunk sequentially, and reports progress via the provided callback.
     * It also handles the final result of the upload process and returns an [AnalysisStatusResponse].
     *
     * @param file The APK file to upload.
     * @param appName The name of the app being analyzed.
     * @param packageName The package name of the app being analyzed.
     * @param isLastChunk Indicates whether this is the last chunk of the APK file.
     * @param progressCallback A callback function to report the size of each uploaded chunk.
     * @return An [AnalysisStatusResponse] object containing the result of the upload process.
     */
    private suspend fun uploadLargeApk(
        file: File,
        appName: String,
        packageName: String,
        isLastChunk: Boolean,
        progressCallback: (Long) -> Unit
    ): AnalysisStatusResponse {
        // Calculate the total number of chunks based on the file size and chunk size
        val totalChunks = (file.length() + CHUNK_SIZE - 1) / CHUNK_SIZE
        var lastChunkResult: AnalysisResult? = null

        Log.d(TAG, "Starting upload for file: ${file.name}")
        Log.d(TAG, "File size: ${file.length()} bytes")
        Log.d(TAG, "Chunk size: $CHUNK_SIZE bytes")
        Log.d(TAG, "Total chunks: $totalChunks")

        // Iterate through each chunk and upload it
        for (chunkIndex in 0 until totalChunks) {
            val startByte = chunkIndex * CHUNK_SIZE
            val endByte = min((chunkIndex + 1) * CHUNK_SIZE, file.length()) - 1

            Log.d(TAG, "Uploading chunk $chunkIndex")
            Log.d(TAG, "Chunk range: $startByte - $endByte")

            // Upload the current chunk and get the result
            val chunkResult = uploadChunk(
                file = file,
                startByte = startByte,
                endByte = endByte,
                chunkIndex = chunkIndex,
                totalChunks = totalChunks,
                appName = appName,
                packageName = packageName,
                isLastChunk = isLastChunk,
            )

            Log.d(TAG, "Chunk $chunkIndex upload result: ${chunkResult.status}")

            // Update progress using the callback
            progressCallback(endByte - startByte + 1)

            // Store the result of the last chunk for final processing
            if (chunkIndex == totalChunks - 1) {
                lastChunkResult = chunkResult
            }
        }

        Log.d(TAG, "Upload completed for file: ${file.name}")

        // Return the final analysis status based on the last chunk's result
        return AnalysisStatusResponse(
            alreadyAnalyzed = lastChunkResult?.status == true,
            previousResults = lastChunkResult
        )
    }

    /**
     * Uploads a single chunk of an APK file.
     *
     * This method prepares the request body for the chunk, including metadata such as the chunk index,
     * total chunks, app name, and package name, and sends it to the server via the API.
     *
     * @param file The APK file being uploaded.
     * @param startByte The starting byte position of the chunk.
     * @param endByte The ending byte position of the chunk.
     * @param chunkIndex The index of the current chunk.
     * @param totalChunks The total number of chunks for the file.
     * @param appName The name of the app being analyzed.
     * @param packageName The package name of the app being analyzed.
     * @param isLastChunk Indicates whether this is the last chunk of the APK file.
     * @return An [AnalysisResult] object containing the result of the chunk upload.
     * @throws IOException If the upload fails or the response is empty.
     */
    private suspend fun uploadChunk(
        file: File,
        startByte: Long,
        endByte: Long,
        chunkIndex: Long,
        totalChunks: Long,
        appName: String,
        packageName: String,
        isLastChunk: Boolean,
    ): AnalysisResult {
        // Create the request body for the chunk using MinimalMemoryRequestBody
        val requestBody = MinimalMemoryRequestBody(
            file = file,
            contentType = "application/vnd.android.package-archive",
            startByte = startByte,
            endByte = endByte
        )
        val part = MultipartBody.Part.createFormData(
            "chunk",
            file.name,
            requestBody
        )

        // Prepare metadata fields for the request
        val appNameBody = appName.toRequestBody("text/plain".toMediaTypeOrNull())
        val packageNameBody = packageName.toRequestBody("text/plain".toMediaTypeOrNull())
        val chunkIndexBody = chunkIndex.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val totalChunksBody = totalChunks.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val fileNameBody = file.name.toRequestBody("text/plain".toMediaTypeOrNull())

        // Perform the API call to upload the chunk
        val response = SgApi.retrofitService.uploadApkChunk(
            chunk = part,
            appName = appNameBody,
            packageName = packageNameBody,
            chunkIndex = chunkIndexBody,
            totalChunks = totalChunksBody,
            fileName = fileNameBody,
            isLastChunk = isLastChunk
        )

        // Handle unsuccessful responses
        if (!response.isSuccessful) {
            throw IOException("Chunk upload failed: ${response.code()}")
        }

        // Extract and return the body (AnalysisResult) from the response
        return response.body() ?: throw IOException("Empty response body")
    }
}

/**
 * Represents the progress of the upload operation.
 *
 * This sealed class defines the possible states of an upload operation, such as starting,
 * uploading progress, preparing for dynamic analysis, completion, or encountering an error.
 * It is used to track and communicate the status of the upload process to the UI layer.
 */
sealed class UploadProgress {

    /**
     * Indicates that the upload operation has started.
     *
     * This state is used to signal the beginning of the upload process.
     */
    data object Started : UploadProgress()

    /**
     * Indicates the progress of the upload operation.
     *
     * @param percentComplete The percentage of the upload that has been completed (0-100).
     */
    data class Uploading(val percentComplete: Int) : UploadProgress()

    /**
     * Indicates that the system is preparing for dynamic analysis after the upload.
     *
     * This state is used to signal that the uploaded files are being processed for dynamic analysis.
     */
    data object PreparingDynamicAnalysis : UploadProgress()

    /**
     * Indicates that the upload operation has completed successfully.
     *
     * @param result The [AnalysisStatusResponse] containing the results of the upload and analysis.
     */
    data class Completed(val result: AnalysisStatusResponse) : UploadProgress()

    /**
     * Indicates that an error occurred during the upload operation.
     *
     * @param message A descriptive error message explaining the issue.
     */
    data class Error(val message: String) : UploadProgress()
}