package com.example.sg360.dashboard

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

/**
 * TFLiteClassifier for running inference on a TensorFlow Lite model.
 *
 * This class encapsulates the logic for loading a TensorFlow Lite model, running predictions,
 * and managing resources. It is designed to be used for static analysis tasks within the app.
 *
 * @property context The application context used to load the model file from assets.
 */
class TFLiteClassifier(context: Context) {
    private var tflite: Interpreter? = null

    /**
     * Initializes the TensorFlow Lite interpreter by loading the model from the app's assets.
     *
     * If an error occurs during initialization, it logs the exception for debugging purposes.
     */
    init {
        try {
            // Load the TensorFlow Lite model from assets
            val tfliteModel = FileUtil.loadMappedFile(context, "staticModel.tflite")
            // Initialize the TensorFlow Lite interpreter with the loaded model
            tflite = Interpreter(tfliteModel)
        } catch (ex: Exception) {
            Log.e("TFLiteClassifier", "Error initializing interpreter", ex)
        }
    }

    /**
     * Makes predictions using the loaded TensorFlow Lite model.
     *
     * This method takes input features, runs them through the model, and returns the output.
     * It assumes the model outputs a batch size of 1 with two values (e.g., probabilities).
     *
     * @param input A FloatArray containing the input features for the model.
     * @return A FloatArray containing the model's output, or null if prediction fails.
     * @throws IllegalStateException if the interpreter is not initialized.
     */
    fun predict(input: FloatArray): FloatArray {
        if (tflite == null) {
            throw IllegalStateException("Interpreter not initialized")
        }

        // Create output array (assuming model outputs [1, 2])
        val output = Array(1) { FloatArray(2) }

        // Run inference using the TensorFlow Lite interpreter
        tflite!!.run(input, output)

        // Return the first row of the output (assuming batch size 1)
        return output[0]
    }

    /**
     * Closes the TensorFlow Lite interpreter and releases allocated resources.
     *
     * This method should be called when the classifier is no longer needed to prevent memory leaks.
     */
    fun close() {
        tflite?.close()
        tflite = null
    }
}