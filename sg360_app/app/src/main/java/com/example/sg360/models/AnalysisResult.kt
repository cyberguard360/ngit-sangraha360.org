package com.example.sg360.models

import kotlinx.serialization.Serializable

/**
 * Data class representing the result of an app analysis.
 *
 * This class is used to deserialize JSON responses from the server into Kotlin objects.
 * It contains information about the analysis status and details of system calls.
 */
@Serializable
data class AnalysisResult(
    /**
     * Indicates whether the analysis was successful.
     *
     * - `true` if the analysis completed successfully.
     * - `false` if the analysis failed or encountered an error.
     */
    val status: Boolean,

    /**
     * A list of top system calls identified during the analysis.
     *
     * Each entry in the list is itself a list of strings, where each string represents
     * a system call or related metadata. Defaults to an empty list if no data is available.
     */
    val topSyscalls: List<List<String>> = emptyList(),

    /**
     * A list of top "red zone" system calls identified during the analysis.
     *
     * These system calls are flagged as potentially risky or suspicious.
     * Each entry in the list is itself a list of strings, where each string represents
     * a system call or related metadata. Defaults to an empty list if no data is available.
     */
    val topRedZonesyscalls: List<List<String>> = emptyList()
)