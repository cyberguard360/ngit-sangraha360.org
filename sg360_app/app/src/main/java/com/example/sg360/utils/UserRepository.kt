package com.example.sg360.utils

import android.system.Os.remove
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val USERNAME = stringPreferencesKey("username")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val APP_LIST = stringSetPreferencesKey("app_list")
        val CLUSTER_NAME = stringPreferencesKey("cluster_name")
        val EPOCH_COUNT = intPreferencesKey("epoch_count")
        val ROUND_COUNT = intPreferencesKey("round_count")
        val MODEL_DATA = stringPreferencesKey("model_data")
        val SECRET_KEY = stringPreferencesKey("secret_key")
        const val TAG = "UserRepository"
    }

    val username: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[USERNAME]
        }

    val accessToken: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    val appList: Flow<Set<String>?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[APP_LIST] ?: emptySet()
        }

    val clusterName: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[CLUSTER_NAME]
        }

    val epochCount: Flow<Int?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[EPOCH_COUNT]
        }

    val roundCount: Flow<Int?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[ROUND_COUNT]
        }

    val modelData: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[MODEL_DATA]
        }

    val secretKey: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SECRET_KEY]
        }

    /**
     * Saves the given username to the data store.
     *
     * @param username the username to be saved
     */
    
    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }

    /**
     * Saves the given access token to the data store.
     *
     * @param accessToken the access token to be saved
     */
    suspend fun saveAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    /**
     * Saves the given refresh token to the data store.
     *
     * @param refreshToken the refresh token to be saved
     */
    suspend fun saveRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    /**
     * Saves the given app to the app list in the data store.
     *
     * @param app the app to be saved
     */
    suspend fun saveAppList(app: String) {
        dataStore.edit { preferences ->
            val currentApps = preferences[APP_LIST] ?: emptySet()
            val updatedApps = currentApps.toMutableSet().apply { add(app) }
            preferences[APP_LIST] = updatedApps
        }
    }

    suspend fun deleteAppFromList(app: String) {
        dataStore.edit { preferences ->
            val currentApps = preferences[APP_LIST] ?: emptySet()
            val updatedApps = currentApps.toMutableSet().apply { remove(app) }
            preferences[APP_LIST] = updatedApps
        }
    }

    /**
     * Saves the given username to the data store.
     *
     * @param clusterName the username to be saved
     */

    suspend fun saveClusterName(clusterName: String) {
        dataStore.edit { preferences ->
            preferences[CLUSTER_NAME] = clusterName
        }
    }

    /**
     * Saves the given username to the data store.
     *
     * @param epochCount the username to be saved
     */

    suspend fun saveEpochCount(epochCount: Int) {
        dataStore.edit { preferences ->
            preferences[EPOCH_COUNT] = epochCount
        }
    }

    /**
     * Saves the given username to the data store.
     *
     * @param roundCount the username to be saved
     */

    suspend fun saveRoundCount(roundCount: Int) {
        dataStore.edit { preferences ->
            preferences[ROUND_COUNT] = roundCount
        }
    }

    /**
     * Saves the given username to the data store.
     *
     * @param modelData the username to be saved
     */

    suspend fun saveModelData(modelData: String) {
        dataStore.edit { preferences ->
            preferences[MODEL_DATA] = modelData
        }
    }

    /**
     * Saves the given username to the data store.
     *
     * @param roundCount the username to be saved
     */
    suspend fun saveSecretKey(secretKey: String) {
        dataStore.edit { preferences ->
            preferences[SECRET_KEY] = secretKey
        }
    }
}
