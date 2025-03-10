package com.example.sg360.dashboard

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.util.Arrays

/**
 * AppScanner class for analyzing Android applications.
 *
 * This class is responsible for scanning Android apps and classifying them as either "Malware" or "Benign"
 * using a TensorFlow Lite classifier. It extracts features from the app's package information and uses
 * them as input for the classification model.
 *
 * @property context The application context used to access system services like PackageManager.
 */
class AppScanner(private val context: Context) {
    private var classifier: TFLiteClassifier? = null // TensorFlow Lite classifier instance

    /**
     * Scans an app and classifies it as either "Malware" or "Benign".
     *
     * This method initializes the classifier (if not already initialized), prepares input features
     * based on the app's package information, and performs the classification. The result is determined
     * by comparing the malware probability against a threshold (0.5).
     *
     * @param packageName The package name of the app to scan.
     * @return A string indicating the scan result ("Malware", "Benign", or "Scan failed").
     */
    fun scanApp(packageName: String): String {
        try {
            // Initialize the classifier if it hasn't been already
            if (classifier == null) {
                classifier = TFLiteClassifier(context)
            }

            val packageManager = context.packageManager
            val inputFeatures = prepareInputFeatures(packageManager, packageName)

            // Perform prediction using the classifier
            val prediction = classifier!!.predict(inputFeatures)
            Log.d("Prediction", prediction?.joinToString("\n") ?: "None")

            // Extract malware probability from prediction (index 1 corresponds to malware probability)
            val malwareProbability = prediction?.get(1) ?: 0f

            // Classify based on a threshold of 0.5
            return if (malwareProbability > 0.5) "Malware" else "Benign"
        } catch (e: Exception) {
            // Log and handle errors during the scan process
            Log.e("AppScanner", "Error during scan: $e")
            return "Scan failed"
        }
    }

    /**
     * Prepares input features for the classifier based on the app's package information.
     *
     * This method extracts various attributes of the app (e.g., permissions, activities, services)
     * and converts them into a FloatArray that serves as input for the TensorFlow Lite model.
     * Placeholder values are used for unsupported features.
     *
     * @param pm The PackageManager instance used to retrieve package information.
     * @param packageName The package name of the app.
     * @return A FloatArray containing the prepared input features.
     */
    private fun prepareInputFeatures(pm: PackageManager, packageName: String): FloatArray {
        val input = FloatArray(10) // 10 features, excluding the label
        try {
            // Retrieve detailed package information
            val packageInfo = pm.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_CONFIGURATIONS
            )

            // Populate input features
            input[0] = 0f // api_call (placeholder)
            input[1] = (packageInfo.permissions?.size ?: 0).toFloat() // Permission count
            input[2] = 0f // url (placeholder)
            input[3] = (packageInfo.providers?.size ?: 0).toFloat() // Provider count
            input[4] = (packageInfo.reqFeatures?.size ?: 0).toFloat() // Feature count
            input[5] = 0f // intent (placeholder)
            input[6] = (packageInfo.activities?.size ?: 0).toFloat() // Activity count
            input[7] = 0f // call (placeholder)
            // Service and receiver count (combined services and receivers)
            val serviceCount = packageInfo.services?.size ?: 0
            val receiverCount = packageInfo.receivers?.size ?: 0
            input[8] = (serviceCount + receiverCount).toFloat()
            // Real permission count (using requested permissions as an approximation)
            input[9] = (packageInfo.requestedPermissions?.size ?: 0).toFloat()

        } catch (e: PackageManager.NameNotFoundException) {
            // Handle the exception by logging the error and setting all inputs to 0
            e.printStackTrace()
            Arrays.fill(input, 0f)
        }
        return input
    }

    /**
     * Retrieves raw features for a given app package.
     *
     * This method is a wrapper around `prepareInputFeatures` and is used to expose the feature
     * extraction functionality outside the class.
     *
     * @param packageName The package name of the app.
     * @param context The application context.
     * @return A FloatArray containing the raw features extracted from the app's package information.
     */
    fun getRawFeatures(packageName: String, context: Context): FloatArray {
        val pm = context.packageManager
        return prepareInputFeatures(pm, packageName)
    }
}