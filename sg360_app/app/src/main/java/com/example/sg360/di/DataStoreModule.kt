package com.example.sg360.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.sg360.data.AppAnalysisDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing DataStore-related dependencies in the SG360 app.
 *
 * This module is responsible for creating and managing instances of [DataStore] and
 * [AppAnalysisDataStore]. It ensures that these dependencies are properly scoped as singletons
 * and injected wherever needed in the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides a singleton instance of [DataStore<Preferences>] for storing app analysis results.
     *
     * This method uses [PreferenceDataStoreFactory] to create a DataStore instance that persists
     * preferences in a file named "app_analysis_results". The file is stored in the app's internal
     * storage directory.
     *
     * @param context The application context, provided by Dagger Hilt.
     * @return A singleton instance of [DataStore<Preferences>].
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("app_analysis_results")
        }
    }

    /**
     * Provides a singleton instance of [AppAnalysisDataStore] for managing app analysis data.
     *
     * This method creates an instance of [AppAnalysisDataStore], which uses the provided
     * [DataStore<Preferences>] to store and retrieve app analysis data. The application context
     * is also passed to ensure proper initialization.
     *
     * @param context The application context, provided by Dagger Hilt.
     * @param dataStore An instance of [DataStore<Preferences>], used for persisting app analysis data.
     * @return A singleton instance of [AppAnalysisDataStore].
     */
    @Provides
    @Singleton
    fun provideAppAnalysisDataStore(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): AppAnalysisDataStore {
        return AppAnalysisDataStore(context)
    }
}