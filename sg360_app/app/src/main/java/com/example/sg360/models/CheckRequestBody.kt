package com.example.sg360.models

/**
 * Data class representing the request body for checking the analysis status of an app.
 *
 * This class is used to serialize the request payload sent to the server when querying
 * whether an app has already been analyzed. It contains the necessary information to
 * identify the app on the server.
 */
data class CheckRequestBody(
    /**
     * The unique package name of the app (e.g., "com.example.myapp").
     *
     * This value is used by the server to identify the app and check its analysis status.
     */
    val package_name: String
)