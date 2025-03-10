package com.example.sg360

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sg360.worker.InstalledAppsWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Custom Application class for the SG360 app.
 *
 * This class serves as the entry point for application-wide initialization logic.
 * It is annotated with @HiltAndroidApp to enable dependency injection using Hilt.
 * Responsibilities include:
 * - Providing a global instance of the application context.
 * - Initializing periodic background tasks (e.g., InstalledAppsWorker).
 */
@HiltAndroidApp
class SG360Application : Application() {

    companion object {
        /**
         * Global instance of the SG360Application.
         * This allows access to the application context from anywhere in the app.
         * The setter is private to ensure it can only be initialized within this class.
         */
        lateinit var instance: SG360Application
            private set
    }

    /**
     * Called when the application is created.
     * Initializes the application instance and sets up periodic background work.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize the global application instance
        instance = this

        // Schedule periodic background work to monitor installed apps
//        scheduleInstalledAppsWorker()
    }

    /**
     * Schedules a periodic background task to monitor installed apps.
     *
     * This method uses WorkManager to schedule a [InstalledAppsWorker] task that runs periodically.
     * The task is constrained to run only when the device has an active network connection.
     */
    private fun scheduleInstalledAppsWorker() {
        // Define constraints for the worker (e.g., requires network connectivity)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Task requires an active network connection
            .build()

        // Create a periodic work request for the InstalledAppsWorker
        val workRequest = PeriodicWorkRequestBuilder<InstalledAppsWorker>(
            12, TimeUnit.HOURS // Run every 12 hours
        )
            .setConstraints(constraints) // Apply the defined constraints
            .build()

        // Enqueue the work request using WorkManager
        CoroutineScope(Dispatchers.Default).launch {
            WorkManager.getInstance(this@SG360Application)
                .enqueueUniquePeriodicWork(
                    "InstalledAppsWorker", // Unique name for the work
                    KEEP, // Keep existing work if already scheduled
                    workRequest
                )
        }
    }
}