package com.example.sg360.models

/**
 * Data class representing the response for an analysis status check.
 *
 * This class is used to deserialize JSON responses from the server when checking
 * whether an app has already been analyzed. It provides information about the
 * analysis status and any previously stored results.
 */
data class AnalysisStatusResponse(
    /**
     * Indicates whether the app has already been analyzed.
     *
     * - `true` if the app has been analyzed before.
     * - `false` if the app has not been analyzed or no results are available.
     */
    val alreadyAnalyzed: Boolean,

    /**
     * Contains the previous analysis results if the app has already been analyzed.
     *
     * - If `alreadyAnalyzed` is `true`, this property holds the [AnalysisResult] object
     *   containing details of the previous analysis.
     * - If `alreadyAnalyzed` is `false`, this property will be `null`.
     */
    val previousResults: AnalysisResult?
)