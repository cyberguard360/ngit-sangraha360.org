package com.example.sg360.di

import android.content.Context
import com.example.sg360.auth.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing authentication-related dependencies in the SG360 app.
 *
 * This module is responsible for creating and managing instances of [AuthRepository]. It ensures
 * that the repository is properly scoped as a singleton and injected wherever needed in the
 * application.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * Provides a singleton instance of [AuthRepository] for managing authentication logic.
     *
     * This method uses Dagger Hilt to inject the application context, which is required for
     * initializing the [AuthRepository]. The repository is scoped as a singleton to ensure
     * only one instance is created and reused throughout the app's lifecycle.
     *
     * @param context The application context, provided by Dagger Hilt.
     * @return A singleton instance of [AuthRepository].
     */
    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository {
        return AuthRepository(context)
    }
}