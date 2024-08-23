package com.example.sg360

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sg360.utils.UserRepository
import com.example.sg360.worker.InstalledAppsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Name of the DataStore preferences file
private const val USER_PREFERENCE_NAME = "user_details"

// Extension property to easily access DataStore from Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCE_NAME
)

/**
 * Custom Application class for the SG360 app.
 */
class SG360Application : Application() {
    // Repository for managing user data
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        Log.d("Application", "onCreate: ")

        // Initialize the user repository
        userRepository = UserRepository(dataStore)
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.saveSecretKey("Uvl2viLLPlqEK8/rRs5bPQsCuElG9iqnt6pZkuVuncA=")
        }
        Log.d("Application", "onCreate: ${userRepository.username}")

        // Set up periodic background work
        setupPeriodicWork()
    }

    /**
     * Sets up periodic background work to check installed apps.
     */
    private fun setupPeriodicWork() {
        // Define constraints for the work (requires network connection)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a periodic work request that runs every 20 minutes
        val myWorkRequest = PeriodicWorkRequestBuilder<InstalledAppsWorker>(20, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        // Enqueue the work request
        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "InstalledAppsWorker",
            KEEP, // Keep existing work if it exists
            myWorkRequest
        )
    }
}