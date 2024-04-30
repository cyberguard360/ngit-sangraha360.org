package com.example.sg360.network

import com.example.sg360.utils.Utils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Define a logging interceptor to log HTTP requests and responses
private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    // Set the log level to log the whole request and response body
    level = HttpLoggingInterceptor.Level.BODY
}

// Create an OkHttpClient with the logging interceptor
private val client: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(interceptor) // Add the logging interceptor
    .build()

// Create a Retrofit instance with the Gson converter and the base URL from Utils
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create()) // Add the Gson converter
    .baseUrl(Utils.BASE) // Set the base URL
    .client(client) // Set the OkHttpClient
    .build()

// Create a singleton instance of the SgApiService using Retrofit
object SgApi {
    val retrofitService: SgApiService by lazy {
        retrofit.create(SgApiService::class.java) // Create the SgApiService instance
    }
}
