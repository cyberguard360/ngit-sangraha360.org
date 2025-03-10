package com.example.sg360.models

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Data class representing the state of an installed app item.
 *
 * This class encapsulates all relevant information about an app, including its metadata,
 * loading/uploading states, and analysis results. It is used to manage the UI and logic
 * for individual apps in the application list.
 */
data class AppItemState(
    /**
     * The display name of the app (e.g., "My Application").
     */
    val name: String,

    /**
     * The unique package name of the app (e.g., "com.example.myapp").
     */
    val packageName: String,

    /**
     * The icon of the app, represented as an [ImageBitmap].
     * This is used to display the app's logo in the UI.
     */
    val icon: ImageBitmap,

    /**
     * The directory path where the app's APK file is located.
     */
    val sourceDir: String,

    /**
     * An array of directory paths for split APK files, if applicable.
     * This is `null` if the app does not use split APKs.
     */
    val splitSourceDirs: Array<String>?,

    /**
     * Indicates whether the app is currently being analyzed or processed.
     * - `true` if the app is in a loading state.
     * - `false` otherwise.
     */
    var isLoading: Boolean = false,

    /**
     * Indicates whether the analysis for the app has been completed.
     * - `true` if the analysis is done.
     * - `false` otherwise.
     */
    var isDone: Boolean = false,

    /**
     * The result of the analysis, if available.
     * This contains a string representation of the analysis outcome.
     * Defaults to `null` if no result is available yet.
     */
    var result: String? = null,

    /**
     * Indicates whether the app's APK file is currently being uploaded.
     * - `true` if the upload is in progress.
     * - `false` otherwise.
     */
    var isUploading: Boolean = false,

    /**
     * The current status of the APK upload process.
     * This property tracks whether the upload was successful, failed, or is still in progress.
     * Defaults to `null` if no upload has started.
     */
    var uploadStatus: UploadStatus? = null
)

/**
 * Enum class representing the possible statuses of an APK upload process.
 *
 * This enum is used to track the state of an app's APK upload in the [AppItemState] class.
 */
enum class UploadStatus {
    /**
     * The upload was completed successfully.
     */
    SUCCESS,

    /**
     * The upload failed due to an error or other issue.
     */
    FAILED,

    /**
     * The upload is currently in progress.
     */
    IN_PROGRESS
}