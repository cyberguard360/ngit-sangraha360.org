package com.example.sg360.worker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker class to fetch and process installed non-system applications.
 *
 * This class extends [CoroutineWorker] and performs background tasks related to fetching
 * the list of installed user applications on the device. It runs on a background thread
 * using Kotlin coroutines and handles success or failure states appropriately.
 *
 * Responsibilities:
 * - Fetch the list of installed non-system applications.
 * - Notify the user about the worker's progress and result.
 * - Handle exceptions gracefully and log errors for debugging.
 */
class InstalledAppsWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    /**
     * Executes the background work for this worker.
     *
     * This method performs the following steps:
     * 1. Notifies the user that the worker has started.
     * 2. Fetches the list of installed non-system applications.
     * 3. Logs any errors that occur during execution.
     * 4. Returns a [Result] indicating success or failure.
     *
     * @return [Result.success] if the work completes successfully, [Result.failure] otherwise.
     */
    override suspend fun doWork(): Result {
        // Notify the user that the worker is running
        makeStatusNotification("Worker Running", applicationContext)

        return withContext(Dispatchers.IO) {
            try {
                // Retrieve the package manager to access application information
                val packageManager: PackageManager = applicationContext.packageManager

                // Fetch all installed applications and filter out system apps
                val installedApps = packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES
                ).filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }

                // Notify the user that the worker completed successfully
                makeStatusNotification("Worker Success", applicationContext)

                // Indicate that the work finished successfully
                Result.success()
            } catch (throwable: Throwable) {
                // Log the error for debugging purposes
                Log.e("InstalledAppsWorker", "Error applying work", throwable)

                // Indicate that the work failed
                Result.failure()
            }
        }
    }

    /**
     * Displays a status notification to inform the user about the worker's progress.
     *
     * @param message The message to display in the notification.
     * @param context The application context used to create the notification.
     */
    private fun makeStatusNotification(message: String, context: Context) {
        // Implementation for creating and displaying a notification
        // This method should be implemented elsewhere in the project
    }
}