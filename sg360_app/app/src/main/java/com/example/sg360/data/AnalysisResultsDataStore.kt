package com.example.sg360.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.sg360.models.AnalysisResult
import com.example.sg360.models.StaticAnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to get the DataStore from the Context
private val Context.analysisDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_analysis_results"
)

/**
 * Data class that combines static and dynamic analysis results for storage.
 *
 * This class represents the combined data structure used to store both static and dynamic
 * analysis results for an app. It includes metadata such as the package name and timestamp.
 */
@kotlinx.serialization.Serializable
data class AppAnalysisData(
    val packageName: String, // Unique identifier for the app
    val staticAnalysisResult: StaticAnalysisResult, // Static analysis results
    val dynamicAnalysisResult: AnalysisResult?, // Dynamic analysis results (optional)
    val timestamp: Long = System.currentTimeMillis() // Timestamp of when the data was saved
)

/**
 * Repository for managing app analysis data using DataStore.
 *
 * This class provides methods for saving, retrieving, and managing app analysis data using
 * Android's DataStore API. It ensures data persistence and retrieval in a structured format.
 */
@Singleton
class AppAnalysisDataStore @Inject constructor(
    private val context: Context // Application context provided via dependency injection
) {
    private val json = Json { ignoreUnknownKeys = true } // JSON serializer/deserializer

    /**
     * Saves app analysis data to DataStore.
     *
     * This method serializes the [AppAnalysisData] object into a JSON string and stores it
     * in DataStore using the app's package name as the key.
     *
     * @param analysisData The [AppAnalysisData] object to save.
     */
    suspend fun saveAnalysisResult(analysisData: AppAnalysisData) {
        val key = stringPreferencesKey(analysisData.packageName)
        context.analysisDataStore.edit { preferences ->
            val serializedData = json.encodeToString(analysisData)
            preferences[key] = serializedData
            Log.d("DataStore", "Saved analysis for ${analysisData.packageName}: $serializedData")
        }
    }

    /**
     * Retrieves app analysis data from DataStore.
     *
     * This method retrieves the stored JSON string for the given package name, deserializes it
     * into an [AppAnalysisData] object, and returns it as a [Flow].
     *
     * @param packageName The package name of the app to retrieve analysis data for.
     * @return A [Flow] emitting the [AppAnalysisData] object or `null` if no data is found.
     */
    fun getAnalysisResult(packageName: String): Flow<AppAnalysisData?> {
        val key = stringPreferencesKey(packageName)
        return context.analysisDataStore.data.map { preferences ->
            val result = preferences[key]?.let { serializedData ->
                try {
                    json.decodeFromString<AppAnalysisData>(serializedData)
                } catch (e: Exception) {
                    null
                }
            }
            Log.d("DataStore", "Loaded analysis for $packageName: $result")
            result
        }
    }

    /**
     * Checks if analysis result exists and is not older than the specified maximum age.
     *
     * This method validates whether the cached analysis data is still valid based on its age.
     * If the data is missing or older than the specified maximum age, it is considered invalid.
     *
     * @param analysisData The [AppAnalysisData] object to validate.
     * @param maxAgeMs The maximum allowed age of the cached result in milliseconds (default is 7 days).
     * @return `true` if the analysis data is valid, `false` otherwise.
     */
    fun hasValidAnalysisResult(
        analysisData: AppAnalysisData?,
        maxAgeMs: Long = 7 * 24 * 60 * 60 * 1000 // Default: 7 days
    ): Boolean {
        if (analysisData == null) return false
        val currentTime = System.currentTimeMillis()
        val resultAge = currentTime - analysisData.timestamp
        if (analysisData.dynamicAnalysisResult == null) {
            Log.d("DataStore", "Dynamic analysis missing for ${analysisData.packageName} → Rechecking")
            return false
        }
        return resultAge <= maxAgeMs
    }

    /**
     * Clears all stored analysis results.
     *
     * This method removes all entries from the DataStore, effectively clearing all cached
     * analysis data.
     */
    suspend fun clearAllAnalysisResults() {
        context.analysisDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Removes analysis result for a specific package.
     *
     * This method deletes the analysis data associated with the given package name from DataStore.
     *
     * @param packageName The package name of the app to remove analysis data for.
     */
    suspend fun removeAnalysisResult(packageName: String) {
        val key = stringPreferencesKey(packageName)
        context.analysisDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}