package com.example.sg360.models

import kotlinx.serialization.Serializable

/**
 * Data class representing the result of a static analysis performed on an app.
 *
 * This class is used to deserialize JSON responses from the server containing detailed
 * static analysis results for an app. It includes information about potential malware
 * predictions, component counts, and raw feature data.
 */
@Serializable
data class StaticAnalysisResult(
    /**
     * The predicted malware status of the app (e.g., "Malware" or "Benign").
     *
     * This value indicates whether the app is classified as malicious or safe based on
     * static analysis.
     */
    val malwarePrediction: String,

    /**
     * The total number of permissions declared by the app.
     */
    val permissionCount: Int,

    /**
     * The total number of activities declared by the app.
     */
    val activityCount: Int,

    /**
     * The total number of services declared by the app.
     */
    val serviceCount: Int,

    /**
     * The total number of broadcast receivers declared by the app.
     */
    val receiverCount: Int,

    /**
     * The total number of content providers declared by the app.
     */
    val providerCount: Int,

    /**
     * The total number of features extracted during static analysis.
     */
    val featureCount: Int,

    /**
     * A float array containing raw feature data extracted during static analysis.
     *
     * These features are typically used for machine learning models or further analysis.
     */
    val rawFeatures: FloatArray
) {
    /**
     * Overrides the `equals` method to compare two [StaticAnalysisResult] objects for equality.
     *
     * Two [StaticAnalysisResult] objects are considered equal if all their properties match,
     * including the content of the `rawFeatures` array.
     *
     * @param other The object to compare with this instance.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StaticAnalysisResult

        if (malwarePrediction != other.malwarePrediction) return false
        if (permissionCount != other.permissionCount) return false
        if (activityCount != other.activityCount) return false
        if (serviceCount != other.serviceCount) return false
        if (receiverCount != other.receiverCount) return false
        if (providerCount != other.providerCount) return false
        if (featureCount != other.featureCount) return false
        return rawFeatures.contentEquals(other.rawFeatures)
    }

    /**
     * Overrides the `hashCode` method to generate a hash code for this object.
     *
     * The hash code is calculated based on all properties of the class, including the
     * content of the `rawFeatures` array.
     *
     * @return A hash code value for this object.
     */
    override fun hashCode(): Int {
        var result = malwarePrediction.hashCode()
        result = 31 * result + permissionCount
        result = 31 * result + activityCount
        result = 31 * result + serviceCount
        result = 31 * result + receiverCount
        result = 31 * result + providerCount
        result = 31 * result + featureCount
        result = 31 * result + rawFeatures.contentHashCode()
        return result
    }
}