package com.example.sg360.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sg360.models.AnalysisResult
import com.example.sg360.models.StaticAnalysisResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Displays the analysis result view based on the current UI state.
 *
 * This composable dynamically renders different views depending on the state of the analysis process.
 * It handles static analysis, dynamic analysis, loading states, and error scenarios.
 *
 * @param uiState The current state of the dashboard UI, which determines what to display.
 * @param onRetryDynamicAnalysis A callback function to retry dynamic analysis in case of failure.
 */
@Composable
fun AnalysisResultView(
    uiState: DashboardUiState,
    onRetryDynamicAnalysis: () -> Unit
) {
    when (uiState) {
        is DashboardUiState.Idle -> {
            // Idle state is handled in the parent DashboardScreen composable
        }

        is DashboardUiState.StaticAnalysisInProgress -> {
            // Display a loading view while static analysis is in progress
            LoadingView(message = "Static Analysis: ${uiState.stage}")
        }

        is DashboardUiState.StaticResult -> {
            if (uiState.dynamicError != null) {
                // Show analysis results with an error message for dynamic analysis
                AnalysisResultTabs(
                    staticResult = uiState.result,
                    dynamicResult = null,
                    dynamicError = uiState.dynamicError,
                    isDynamicInProgress = false,
                    onRetryDynamicAnalysis = onRetryDynamicAnalysis
                )
            } else {
                // Show analysis results without any dynamic analysis errors
                AnalysisResultTabs(
                    staticResult = uiState.result,
                    dynamicResult = null,
                    isDynamicInProgress = false,
                    onRetryDynamicAnalysis = onRetryDynamicAnalysis
                )
            }
        }

        is DashboardUiState.DynamicAnalysisInProgress -> {
            // Display analysis results while dynamic analysis is in progress
            AnalysisResultTabs(
                staticResult = uiState.staticAnalysisResult,
                dynamicResult = null,
                isDynamicInProgress = true,
                dynamicStage = uiState.stage,
                onRetryDynamicAnalysis = onRetryDynamicAnalysis
            )
        }

        is DashboardUiState.DynamicResult -> {
            // Display both static and dynamic analysis results
            AnalysisResultTabs(
                staticResult = uiState.staticAnalysisResult,
                dynamicResult = uiState.dynamicAnalysisResult,
                isDynamicInProgress = false,
                onRetryDynamicAnalysis = onRetryDynamicAnalysis
            )
        }
    }
}

/**
 * Displays an error card for failed dynamic analysis.
 *
 * This composable provides a user-friendly interface to inform the user about the failure
 * and allows them to retry the dynamic analysis process.
 *
 * @param errorMessage The error message describing why the dynamic analysis failed.
 * @param onRetryClick A callback function to retry the dynamic analysis.
 */
@Composable
fun DynamicAnalysisErrorCard(errorMessage: String, onRetryClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Warning icon to indicate an error
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title indicating that dynamic analysis has failed
            Text(
                text = "Dynamic Analysis Failed",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Detailed error message
            Text(
                text = errorMessage,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Retry button to attempt dynamic analysis again
            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E), // Dark background
                    contentColor = Color(0xFF00E676)   // Green text
                )
            ) {
                Text("Retry Dynamic Analysis")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalysisResultTabs(
    staticResult: StaticAnalysisResult,
    dynamicResult: AnalysisResult?,
    isDynamicInProgress: Boolean = false,
    dynamicStage: String = "",
    dynamicError: String? = null,
    onRetryDynamicAnalysis: () -> Unit = {}
) {
    // State for managing the pager (tab navigation)
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    // Save the current page to prevent automatic navigation
    var currentPage by remember { mutableIntStateOf(pagerState.currentPage) }

    // Update the current page state whenever the pager's page changes
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF121212)
                    )
                )
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title for the analysis results section
        Text(
            text = "Analysis Results",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E676),
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xFF00E676).copy(alpha = 0.3f),
                    offset = Offset(0f, 2f),
                    blurRadius = 4f
                )
            ),
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Tab Row for switching between Static and Dynamic Analysis
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF00E676),
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        height = 3.dp,
                        color = Color(0xFF00E676)
                    )
                }
            }
        ) {
            // Static Analysis Tab
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                text = {
                    Text(
                        "Static Analysis",
                        fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Static Analysis")
                }
            )

            // Dynamic Analysis Tab
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                text = {
                    Text(
                        "Dynamic Analysis",
                        fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    when {
                        dynamicResult != null -> {
                            // Dynamic analysis is complete
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Dynamic Analysis Complete")
                        }
                        isDynamicInProgress -> {
                            // Dynamic analysis is in progress
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF00E676)
                            )
                        }
                        else -> {
                            // Dynamic analysis has not started
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Dynamic Analysis Not Started")
                        }
                    }
                }
            )
        }

        // Horizontal Pager for displaying Static and Dynamic Analysis content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> StaticAnalysisCard(staticResult) // Display static analysis results
                1 -> {
                    when {
                        dynamicResult != null -> {
                            // Dynamic analysis is complete
                            DynamicAnalysisCard(dynamicResult, "No Error")
                        }
                        isDynamicInProgress -> {
                            // Dynamic analysis is in progress
                            DynamicAnalysisInProgressCard(dynamicStage)
                        }
                        dynamicError != null -> {
                            // Dynamic analysis encountered an error
                            DynamicAnalysisErrorCard(dynamicError, onRetryDynamicAnalysis)
                        }
                        else -> {
                            // Dynamic analysis has not started
                            DynamicAnalysisNotStartedCard()
                        }
                    }
                }
            }
        }

        // Navigation controls for switching between tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Button to navigate to the Static Analysis tab
            Button(
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                enabled = pagerState.currentPage > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color(0xFF00E676)
                ),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    tint = Color(0xFF00E676)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Static")
            }

            // Button to navigate to the Dynamic Analysis tab
            Button(
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                enabled = pagerState.currentPage < 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color(0xFF00E676)
                ),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text("Dynamic")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = Color(0xFF00E676)
                )
            }
        }
    }
}

/**
 * Displays a card indicating that dynamic analysis is currently in progress.
 *
 * This composable provides a user-friendly interface to inform the user that dynamic analysis
 * is being performed. It includes a progress indicator, the current stage of the process,
 * and additional explanatory text.
 *
 * @param stage A string describing the current stage of the dynamic analysis process.
 */
@Composable
fun DynamicAnalysisInProgressCard(stage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize() // Ensures the card fills the available space
            .padding(16.dp), // Adds padding around the card for better spacing
        contentAlignment = Alignment.Center // Centers the content within the box
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Progress indicator to show ongoing activity
            CircularProgressIndicator(
                color = Color(0xFF00E676), // Green color for progress
                modifier = Modifier.size(64.dp), // Sets the size of the progress indicator
                strokeWidth = 5.dp // Defines the thickness of the progress circle
            )

            Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing

            // Displays the current stage of the dynamic analysis process
            Text(
                text = stage,
                color = Color.LightGray, // Light gray color for readability
                fontSize = 16.sp // Medium font size for clarity
            )

            Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing

            // Additional explanatory text
            Text(
                text = "This might take a few moments",
                color = Color.Gray, // Gray color for secondary text
                fontSize = 14.sp // Smaller font size for supplementary information
            )
        }
    }
}

/**
 * Displays a card indicating that dynamic analysis has not yet started.
 *
 * This composable informs the user that dynamic analysis cannot begin until static analysis
 * is complete. It includes a warning icon and explanatory text.
 */
@Composable
fun DynamicAnalysisNotStartedCard() {
    Box(
        modifier = Modifier
            .fillMaxSize() // Ensures the card fills the available space
            .padding(16.dp), // Adds padding around the card for better spacing
        contentAlignment = Alignment.Center // Centers the content within the box
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Warning icon to indicate that dynamic analysis has not started
            Icon(
                imageVector = Icons.Default.Warning, // Uses a warning icon
                contentDescription = "Not Started", // Accessibility description
                tint = Color.Yellow, // Yellow color for warning
                modifier = Modifier.size(64.dp) // Sets the size of the icon
            )

            Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing

            // Main message indicating that dynamic analysis has not started
            Text(
                text = "Dynamic Analysis Not Started",
                color = Color.LightGray, // Light gray color for readability
                textAlign = TextAlign.Center, // Centers the text horizontally
                fontSize = 16.sp // Medium font size for clarity
            )

            Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing

            // Additional explanatory text
            Text(
                text = "Wait for static analysis to complete",
                color = Color.Gray, // Gray color for secondary text
                textAlign = TextAlign.Center, // Centers the text horizontally
                fontSize = 14.sp // Smaller font size for supplementary information
            )
        }
    }
}

/**
 * Displays a loading view with a progress indicator and a message.
 *
 * This composable is used to indicate that a process is ongoing. It includes a circular progress
 * indicator and a text message to inform the user about the current status.
 *
 * @param message A string describing the current operation or status.
 */
@Composable
fun LoadingView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize() // Ensures the loading view fills the available space
            .padding(16.dp), // Adds padding around the content for better spacing
        contentAlignment = Alignment.Center // Centers the content within the box
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally // Aligns content horizontally
        ) {
            // Progress indicator to show ongoing activity
            CircularProgressIndicator(
                color = Color(0xFF00E676), // Green color for progress
                modifier = Modifier.size(64.dp), // Sets the size of the progress indicator
                strokeWidth = 5.dp // Defines the thickness of the progress circle
            )

            Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing

            // Text message to describe the current status
            Text(
                text = message,
                color = Color.LightGray, // Light gray color for readability
                fontSize = 16.sp, // Medium font size for clarity
                textAlign = TextAlign.Center // Centers the text horizontally
            )
        }
    }
}

/**
 * Displays a card summarizing the results of static analysis.
 *
 * This composable provides a detailed breakdown of the app's structure and malware prediction
 * based on the static analysis results. It uses a lazy column to display the information efficiently.
 *
 * @param result The [StaticAnalysisResult] object containing the static analysis data.
 */
@Composable
fun StaticAnalysisCard(result: StaticAnalysisResult) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize() // Ensures the card fills the available space
            .padding(8.dp) // Adds padding around the card for better spacing
    ) {
        item {
            // Displays the malware prediction section
            MalwarePredictionText(result.malwarePrediction)
            Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing
        }

        item {
            // Displays the "App Structure" section header
            SectionHeader("📱 App Structure")
            Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing

            // Card to display app structure details
            Card(
                modifier = Modifier.fillMaxWidth(), // Ensures the card spans the full width
                colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)), // Dark background color
                shape = RoundedCornerShape(12.dp) // Rounded corners for the card
            ) {
                Column(modifier = Modifier.padding(12.dp)) { // Adds padding inside the card
                    Row(
                        modifier = Modifier.fillMaxWidth(), // Ensures the row spans the full width
                        horizontalArrangement = Arrangement.SpaceBetween // Distributes content evenly
                    ) {
                        // Left column for permissions, activities, and services
                        Column(
                            modifier = Modifier
                                .weight(1f) // Takes up half the available width
                                .padding(end = 8.dp) // Adds spacing between columns
                        ) {
                            InfoText("🔒 Permissions: ", result.permissionCount)
                            InfoText("📱 Activities: ", result.activityCount)
                            InfoText("⚙️ Services: ", result.serviceCount)
                        }

                        // Right column for receivers, providers, and features
                        Column(
                            modifier = Modifier
                                .weight(1f) // Takes up half the available width
                                .padding(start = 8.dp) // Adds spacing between columns
                        ) {
                            InfoText("📡 Receivers: ", result.receiverCount)
                            InfoText("🗄️ Providers: ", result.providerCount)
                            InfoText("🧩 Features: ", result.featureCount)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing
        }
    }
}

/**
 * Displays a card showing the malware prediction result with a blinking cursor effect.
 *
 * This composable visually represents the prediction result ("Benign" or "Malicious") with
 * appropriate colors and an icon. It also includes a blinking cursor to simulate a dynamic
 * typing effect for better user engagement.
 *
 * @param prediction A string representing the malware prediction result (e.g., "Benign" or "Malicious").
 */
@Composable
fun MalwarePredictionText(prediction: String) {
    // State to manage the visibility of the blinking cursor
    var isCursorVisible by remember { mutableStateOf(true) }

    // Coroutine to toggle the cursor visibility every 500ms
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // Blinking effect interval
            isCursorVisible = !isCursorVisible // Toggle cursor visibility
        }
    }

    // Determine if the prediction is "Benign" (case-insensitive)
    val isBenign = prediction.equals("Benign", ignoreCase = true)

    // Set text and background colors based on the prediction result
    val predictionColor = if (isBenign) Color(0xFF00E676) else Color.Red // Green for benign, red for malicious
    val backgroundColor = if (isBenign) Color(0xFF003D20) else Color(0xFF3D0000) // Dark green or red background

    // Card to display the prediction result
    Card(
        modifier = Modifier
            .fillMaxWidth(), // Ensures the card spans the full width
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // Background color of the card
        shape = RoundedCornerShape(8.dp) // Rounded corners for the card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth() // Ensures the row spans the full width
                .padding(16.dp), // Adds padding inside the card
            verticalAlignment = Alignment.CenterVertically // Aligns content vertically
        ) {
            // Icon to indicate the prediction status
            Icon(
                imageVector = if (isBenign) Icons.Default.Check else Icons.Default.Warning, // Check for benign, warning for malicious
                contentDescription = "Prediction Status", // Accessibility description
                tint = predictionColor, // Icon color matches the prediction result
                modifier = Modifier.size(24.dp) // Sets the size of the icon
            )

            Spacer(modifier = Modifier.width(12.dp)) // Adds horizontal spacing between the icon and text

            // Text to display the prediction result with a blinking cursor
            Text(
                text = "Prediction: $prediction" + if (isCursorVisible) " _" else "", // Appends a blinking cursor
                fontSize = 18.sp, // Font size for clarity
                fontWeight = FontWeight.Bold, // Bold font for emphasis
                color = predictionColor // Text color matches the prediction result
            )
        }
    }
}

/**
 * Displays the results or status of dynamic analysis.
 *
 * This composable handles three possible states:
 * 1. **Error State**: Displays an error message if dynamic analysis has failed.
 * 2. **Loading State**: Shows a progress indicator while dynamic analysis is in progress.
 * 3. **Completed State**: Displays the results of dynamic analysis, including top syscalls and red zone syscalls.
 *
 * @param result The [AnalysisResult] object containing the results of dynamic analysis, or `null` if analysis is incomplete.
 * @param dynamicError A string describing the error encountered during dynamic analysis, or "No Error" if no error occurred.
 */
@Composable
fun DynamicAnalysisCard(result: AnalysisResult?, dynamicError: String) {
    if (result == null) {
        // Dynamic analysis is in progress or has failed
        Box(
            modifier = Modifier
                .fillMaxSize() // Ensures the content fills the available space
                .padding(16.dp), // Adds padding for better spacing
            contentAlignment = Alignment.Center // Centers the content within the box
        ) {
            if (dynamicError != "No Error") {
                // Error state
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning, // Warning icon to indicate an error
                        contentDescription = "Error", // Accessibility description
                        tint = Color.Red, // Red color for error indication
                        modifier = Modifier.size(64.dp) // Sets the size of the icon
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing

                    Text(
                        text = dynamicError, // Displays the error message
                        color = Color.Red, // Red color for error text
                        textAlign = TextAlign.Center, // Centers the text horizontally
                        fontSize = 16.sp // Medium font size for clarity
                    )
                }
            } else {
                // Loading state
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFF00E676), // Green color for progress
                        modifier = Modifier.size(64.dp), // Sets the size of the progress indicator
                        strokeWidth = 5.dp // Defines the thickness of the progress circle
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing

                    Text(
                        text = "Performing dynamic analysis...", // Descriptive text for ongoing process
                        color = Color.LightGray, // Light gray color for readability
                        fontSize = 16.sp // Medium font size for clarity
                    )

                    Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing

                    Text(
                        text = "This might take a few moments", // Additional explanatory text
                        color = Color.Gray, // Gray color for secondary text
                        fontSize = 14.sp // Smaller font size for supplementary information
                    )
                }
            }
        }
    } else {
        // Dynamic analysis is complete
        LazyColumn(
            modifier = Modifier
                .fillMaxSize() // Ensures the content fills the available space
                .padding(8.dp) // Adds padding for better spacing
        ) {
            item {
                SectionHeader("🔄 Top Syscalls:", color = Color(0xFF00E676)) // Header for top syscalls section
                Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing

                Card(
                    modifier = Modifier.fillMaxWidth(), // Ensures the card spans the full width
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)), // Dark background color
                    shape = RoundedCornerShape(12.dp) // Rounded corners for the card
                ) {
                    Column(modifier = Modifier.padding(12.dp)) { // Adds padding inside the card
                        if (result.topSyscalls.isNullOrEmpty()) {
                            Text(
                                text = "No syscalls detected", // Indicates no syscalls were found
                                color = Color.Gray, // Gray color for secondary text
                                modifier = Modifier.padding(8.dp) // Adds padding around the text
                            )
                        } else {
                            // Display syscalls in chunks of 3 for better layout
                            val chunks = result.topSyscalls.chunked(3)
                            chunks.forEachIndexed { chunkIndex, chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(), // Ensures the row spans the full width
                                    horizontalArrangement = Arrangement.SpaceBetween // Distributes content evenly
                                ) {
                                    chunk.forEachIndexed { _, syscall ->
                                        SyscallItem(
                                            syscall,
                                            Modifier.weight(1f) // Ensures equal spacing for each item
                                        )
                                    }

                                    // Add empty placeholders if needed to maintain grid alignment
                                    repeat(3 - chunk.size) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }

                                if (chunkIndex < chunks.size - 1) {
                                    Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing between rows
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp)) // Adds vertical spacing
            }

            item {
                SectionHeader("🚨 Top Red Zone Syscalls:", color = Color.Red) // Header for red zone syscalls section
                Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing

                Card(
                    modifier = Modifier.fillMaxWidth(), // Ensures the card spans the full width
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)), // Dark background color
                    shape = RoundedCornerShape(12.dp) // Rounded corners for the card
                ) {
                    Column(modifier = Modifier.padding(12.dp)) { // Adds padding inside the card
                        if (result.topRedZonesyscalls.isNullOrEmpty()) {
                            Text(
                                text = "No red zone syscalls detected", // Indicates no red zone syscalls were found
                                color = Color.Gray, // Gray color for secondary text
                                modifier = Modifier.padding(8.dp) // Adds padding around the text
                            )
                        } else {
                            // Display red zone syscalls in chunks of 3 for better layout
                            val chunks = result.topRedZonesyscalls.chunked(3)
                            chunks.forEachIndexed { chunkIndex, chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(), // Ensures the row spans the full width
                                    horizontalArrangement = Arrangement.SpaceBetween // Distributes content evenly
                                ) {
                                    chunk.forEachIndexed { _, syscall ->
                                        RedZoneSyscallItem(
                                            syscall,
                                            Modifier.weight(1f) // Ensures equal spacing for each item
                                        )
                                    }

                                    // Add empty placeholders if needed to maintain grid alignment
                                    repeat(3 - chunk.size) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }

                                if (chunkIndex < chunks.size - 1) {
                                    Spacer(modifier = Modifier.height(8.dp)) // Adds vertical spacing between rows
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays an individual syscall item in a card format.
 *
 * This composable parses and displays the details of a syscall, including its name, index,
 * and optional count. It uses a dark-themed card with centered alignment for better readability.
 *
 * @param syscall A list containing the syscall details. The first element is expected to be the
 *                syscall name (e.g., "Syscall_123"), and the second element (if present) is the count.
 * @param modifier Optional modifier to customize the layout or appearance of the card.
 */
@Composable
fun SyscallItem(syscall: List<Any>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(4.dp), // Adds padding around the card for spacing
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Dark background color
        shape = RoundedCornerShape(8.dp) // Rounded corners for the card
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp) // Adds internal padding for spacing
                .fillMaxWidth(), // Ensures the column spans the full width of the card
            horizontalAlignment = Alignment.CenterHorizontally // Centers content horizontally
        ) {
            // Parse the syscall to extract the name and index
            val syscallText = syscall.getOrNull(0)?.toString() ?: "Unknown" // Default to "Unknown" if null
            val parts = syscallText.split("_") // Split the text by underscore

            val name = parts.getOrNull(0) ?: "Syscall" // Extract the syscall name
            val syscallIndex = parts.getOrNull(1) ?: "?" // Extract the syscall index

            // Display the syscall name
            Text(
                text = name,
                color = Color.LightGray, // Light gray color for secondary text
                fontSize = 14.sp, // Medium font size for clarity
                fontWeight = FontWeight.Medium, // Medium weight for emphasis
                textAlign = TextAlign.Center // Centers the text horizontally
            )

            // Display the syscall index
            Text(
                text = syscallIndex,
                color = Color(0xFF00E676), // Green color for primary text
                fontSize = 18.sp, // Larger font size for emphasis
                fontWeight = FontWeight.Bold, // Bold weight for prominence
                textAlign = TextAlign.Center // Centers the text horizontally
            )

            // Display the syscall count if available
            syscall.getOrNull(1)?.let {
                Text(
                    text = it.toString(), // Convert the count to a string
                    color = Color.Gray, // Gray color for supplementary text
                    fontSize = 12.sp, // Smaller font size for less important information
                    textAlign = TextAlign.Center // Centers the text horizontally
                )
            }
        }
    }
}

/**
 * Displays an individual red zone syscall item in a card format.
 *
 * This composable is similar to [SyscallItem], but it uses a different color scheme to indicate
 * that the syscall belongs to the "red zone" (potentially malicious or critical).
 *
 * @param syscall A list containing the syscall details. The first element is expected to be the
 *                syscall name (e.g., "Syscall_123"), and the second element (if present) is the count.
 * @param modifier Optional modifier to customize the layout or appearance of the card.
 */
@Composable
fun RedZoneSyscallItem(syscall: List<Any>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(4.dp), // Adds padding around the card for spacing
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A1A)), // Darker red-themed background
        shape = RoundedCornerShape(8.dp) // Rounded corners for the card
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp) // Adds internal padding for spacing
                .fillMaxWidth(), // Ensures the column spans the full width of the card
            horizontalAlignment = Alignment.CenterHorizontally // Centers content horizontally
        ) {
            // Parse the syscall to extract the name and index
            val syscallText = syscall.getOrNull(0)?.toString() ?: "Unknown" // Default to "Unknown" if null
            val parts = syscallText.split("_") // Split the text by underscore

            val name = parts.getOrNull(0) ?: "Syscall" // Extract the syscall name
            val syscallIndex = parts.getOrNull(1) ?: "?" // Extract the syscall index

            // Display the syscall name
            Text(
                text = name,
                color = Color.LightGray, // Light gray color for secondary text
                fontSize = 14.sp, // Medium font size for clarity
                fontWeight = FontWeight.Medium, // Medium weight for emphasis
                textAlign = TextAlign.Center // Centers the text horizontally
            )

            // Display the syscall index
            Text(
                text = syscallIndex,
                color = Color.Red, // Red color to indicate critical or malicious nature
                fontSize = 18.sp, // Larger font size for emphasis
                fontWeight = FontWeight.Bold, // Bold weight for prominence
                textAlign = TextAlign.Center // Centers the text horizontally
            )

            // Display the syscall count if available
            syscall.getOrNull(1)?.let {
                Text(
                    text = it.toString(), // Convert the count to a string
                    color = Color.Gray, // Gray color for supplementary text
                    fontSize = 12.sp, // Smaller font size for less important information
                    textAlign = TextAlign.Center // Centers the text horizontally
                )
            }
        }
    }
}

/**
 * Displays a section header with bold styling and customizable color.
 *
 * This composable is used to create visually distinct headers for sections in the UI. It supports
 * custom colors while defaulting to the primary color from the Material Theme.
 *
 * @param text The text to display as the section header.
 * @param color The color of the text. Defaults to [MaterialTheme.colorScheme.primary].
 */
@Composable
fun SectionHeader(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text = text, // The header text
        fontSize = 18.sp, // Medium-large font size for emphasis
        fontWeight = FontWeight.Bold, // Bold weight for prominence
        color = color, // Customizable text color
        modifier = Modifier.padding(vertical = 4.dp) // Adds vertical padding for spacing
    )
}

/**
 * Displays a labeled value pair in a row layout.
 *
 * This composable is used to present key-value pairs in a clean and readable format. The label is
 * displayed on the left, and the value is displayed on the right with a contrasting color.
 *
 * @param label The descriptive label for the value (e.g., "Permissions").
 * @param value The value associated with the label (e.g., "5").
 */
@Composable
fun InfoText(label: String, value: Any) {
    Row(
        modifier = Modifier
            .fillMaxWidth() // Ensures the row spans the full width of the parent
            .padding(vertical = 4.dp), // Adds vertical padding for spacing
        horizontalArrangement = Arrangement.SpaceBetween, // Distributes content evenly
        verticalAlignment = Alignment.CenterVertically // Aligns content vertically
    ) {
        // Label text
        Text(
            text = label, // The descriptive label
            fontSize = 16.sp, // Medium font size for clarity
            fontWeight = FontWeight.Medium, // Medium weight for emphasis
            color = Color.LightGray // Light gray color for secondary text
        )

        // Value text
        Text(
            text = value.toString(), // Converts the value to a string for display
            fontSize = 16.sp, // Medium font size for clarity
            fontWeight = FontWeight.Bold, // Bold weight for prominence
            color = Color(0xFF00E676) // Green color for primary text
        )
    }
}