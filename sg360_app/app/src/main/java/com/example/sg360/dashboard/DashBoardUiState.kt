package com.example.sg360.dashboard

import com.example.sg360.models.AnalysisResult
import com.example.sg360.models.StaticAnalysisResult

/**
 * Sealed class representing the possible states of the dashboard UI.
 *
 * This class is used to manage the state of the dashboard during static and dynamic analysis processes.
 * Each subclass represents a specific state, such as idle, in-progress analysis, or completed results.
 */
sealed class DashboardUiState {

    /**
     * Represents the idle state of the dashboard.
     *
     * This state indicates that no analysis is currently being performed, and the dashboard is waiting
     * for user interaction or input.
     */
    data object Idle : DashboardUiState()

    /**
     * Represents the state when static analysis is in progress.
     *
     * @param stage A descriptive message indicating the current stage of the static analysis process.
     */
    data class StaticAnalysisInProgress(val stage: String) : DashboardUiState()

    /**
     * Represents the state when static analysis has completed.
     *
     * @param result The [StaticAnalysisResult] containing the outcome of the static analysis.
     * @param timestamp The timestamp indicating when the static analysis was completed.
     * @param dynamicError An optional error message if dynamic analysis failed or was not performed.
     */
    data class StaticResult(
        val result: StaticAnalysisResult,
        val timestamp: Long,
        val dynamicError: String? = null
    ) : DashboardUiState()

    /**
     * Represents the state when dynamic analysis is in progress.
     *
     * @param stage A descriptive message indicating the current stage of the dynamic analysis process.
     * @param staticAnalysisResult The [StaticAnalysisResult] from the previously completed static analysis.
     */
    data class DynamicAnalysisInProgress(
        val stage: String,
        val staticAnalysisResult: StaticAnalysisResult
    ) : DashboardUiState()

    /**
     * Represents the state when both static and dynamic analysis have completed successfully.
     *
     * @param staticAnalysisResult The [StaticAnalysisResult] containing the outcome of the static analysis.
     * @param dynamicAnalysisResult The [AnalysisResult] containing the outcome of the dynamic analysis.
     * @param timestamp The timestamp indicating when the dynamic analysis was completed.
     */
    data class DynamicResult(
        val staticAnalysisResult: StaticAnalysisResult,
        val dynamicAnalysisResult: AnalysisResult,
        val timestamp: Long
    ) : DashboardUiState()
}