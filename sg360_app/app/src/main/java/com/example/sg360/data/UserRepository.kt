package com.example.sg360.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
}
