package com.example.sg360.di

import android.content.Context
import com.example.sg360.dashboard.DashBoardRepository
import com.example.sg360.data.AppAnalysisDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing dependencies in the SG360 app.
 *
 * This module is responsible for defining how certain dependencies are created and provided
 * throughout the application. It ensures that dependencies like [DashBoardRepository] are
 * properly injected and scoped as singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object SGModule {

    /**
     * Provides an instance of [DashBoardRepository] as a singleton dependency.
     *
     * This method uses Dagger Hilt to inject the required dependencies:
     * - The application context ([@ApplicationContext]).
     * - An instance of [AppAnalysisDataStore], which is used for storing and managing app analysis data.
     *
     * @param context The application context, provided by Dagger Hilt.
     * @param analysisDataStore An instance of [AppAnalysisDataStore], used for managing app analysis data.
     * @return A singleton instance of [DashBoardRepository].
     */
    @Singleton
    @Provides
    fun provideDashBoardRepository(
        @ApplicationContext context: Context,
        analysisDataStore: AppAnalysisDataStore // Pass DataStore instance
    ): DashBoardRepository {
        return DashBoardRepository(context, analysisDataStore)
    }
}