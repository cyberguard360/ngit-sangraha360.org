package com.example.sg360.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module for the SG360 app.
 *
 * This file sets up the Retrofit instance and provides a singleton API service for making
 * HTTP requests. It includes logging for debugging purposes and configures timeouts for
 * network operations.
 */

// Define a logging interceptor to log HTTP requests and responses
private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    // Set the log level to log the entire request and response body
    level = HttpLoggingInterceptor.Level.BODY
}

/**
 * Creates an OkHttpClient with the logging interceptor and timeout configurations.
 *
 * The client is configured with:
 * - A read timeout of 5 minutes to handle long-running requests.
 * - An HTTP logging interceptor for debugging purposes.
 */
val client = OkHttpClient.Builder()
    .addInterceptor(interceptor) // Add the logging interceptor to log requests and responses
    .readTimeout(5, TimeUnit.MINUTES) // Set a read timeout of 5 minutes
    .build()

/**
 * Creates a Retrofit instance for making API calls.
 *
 * The Retrofit instance is configured with:
 * - A Gson converter for JSON serialization/deserialization.
 * - The base URL defined in [Utils.BASE].
 * - The custom OkHttpClient with logging and timeout settings.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
    .baseUrl(Utils.BASE) // Base URL for API endpoints
    .client(client) // Use the custom OkHttpClient
    .build()

/**
 * Singleton object providing access to the [SgApiService].
 *
 * This object ensures that only one instance of the API service is created and reused
 * throughout the app, improving performance and consistency.
 */
object SgApi {
    /**
     * Lazy-initialized instance of [SgApiService].
     *
     * The [retrofitService] is created using Retrofit's `create` method and provides
     * access to all API endpoints defined in the [SgApiService] interface.
     */
    val retrofitService: SgApiService by lazy {
        retrofit.create(SgApiService::class.java) // Create the SgApiService instance
    }
}