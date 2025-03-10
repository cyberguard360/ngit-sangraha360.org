package com.example.sg360.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to provide a DataStore instance for the application context
val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * Repository for managing user authentication data.
 *
 * This class is responsible for saving and retrieving user credentials (email and password)
 * using Android's DataStore. It ensures persistence and secure storage of user data.
 *
 * @param context The application context used to access the DataStore.
 */
@Singleton
class AuthRepository @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Defines keys for storing user preferences in DataStore.
     */
    private object PreferencesKeys {
        val EMAIL = stringPreferencesKey("email") // Key for storing the user's email
        val PASSWORD = stringPreferencesKey("password") // Key for storing the user's password
    }

    /**
     * Saves the user's email and password to DataStore.
     *
     * This method persists the provided credentials in the app's DataStore for later retrieval.
     *
     * @param email The user's email to save.
     * @param password The user's password to save.
     */
    suspend fun saveUser(email: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL] = email // Save email
            preferences[PreferencesKeys.PASSWORD] = password // Save password
        }
    }

    /**
     * Retrieves the user's email and password from DataStore.
     *
     * This method provides a flow that emits the stored email and password as a pair.
     * If no data is stored, the values will be null.
     *
     * @return A flow emitting a Pair containing the email and password (nullable).
     */
    fun getUser(): Flow<Pair<String?, String?>> = context.dataStore.data.map { preferences ->
        Pair(
            preferences[PreferencesKeys.EMAIL], // Retrieve email
            preferences[PreferencesKeys.PASSWORD] // Retrieve password
        )
    }
}