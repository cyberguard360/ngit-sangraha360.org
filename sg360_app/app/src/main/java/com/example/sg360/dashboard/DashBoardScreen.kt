package com.example.sg360.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sg360.models.AppItemState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoard(
    appItemStates: List<AppItemState>, // List of installed apps to display
    viewModel: DashBoardViewModel, // ViewModel managing the dashboard state
    scanApp: (AppItemState) -> Unit, // Callback to initiate app scanning
    modifier: Modifier = Modifier // Optional modifier for custom styling
) {
    var searchText by remember { mutableStateOf("") } // Tracks the search input text
    var selectedAppIndex by remember { mutableIntStateOf(-1) } // Tracks the selected app index
    var isSearchActive by remember { mutableStateOf(false) } // Tracks whether search mode is active
    val uiState by viewModel.uiState.collectAsState() // Collects the current UI state from the ViewModel

    // Determine when we should show the back button
    val shouldShowBackButton = selectedAppIndex != -1 || uiState !is DashboardUiState.Idle

    /**
     * Handles back button presses based on the current state.
     */
    fun handleBackPress() {
        when {
            uiState !is DashboardUiState.Idle -> viewModel.resetState() // Reset analysis state
            selectedAppIndex != -1 -> selectedAppIndex = -1 // Deselect the app
            isSearchActive -> {
                isSearchActive = false // Exit search mode
                searchText = "" // Clear the search text
            }
        }
    }

    // Handle system back button presses
    BackHandler(enabled = shouldShowBackButton || isSearchActive) {
        handleBackPress()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            selectedAppIndex != -1 -> "App Details" // Show app details title
                            isSearchActive -> "Search Results" // Show search results title
                            else -> "SG360" // Default dashboard title
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    when {
                        shouldShowBackButton -> {
                            IconButton(onClick = {
                                if (uiState !is DashboardUiState.Idle) {
                                    viewModel.resetState() // Reset analysis state
                                }
                                if (selectedAppIndex != -1) {
                                    selectedAppIndex = -1 // Deselect the app
                                }
                            }) {
                                Icon(
                                    Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                        isSearchActive -> {
                            IconButton(onClick = {
                                isSearchActive = false // Exit search mode
                                searchText = "" // Clear the search text
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search")
                            }
                        }
                    }
                },
                actions = {
                    if (selectedAppIndex == -1 && uiState is DashboardUiState.Idle) {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Toggle Search"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar - extracted to a separate composable
                SearchBar(
                    isSearchActive = isSearchActive,
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it }
                )

                // Filter apps based on the search text
                val filteredApps by remember(searchText, appItemStates) {
                    derivedStateOf {
                        appItemStates.filter { it.name.contains(searchText, ignoreCase = true) }
                    }
                }

                if (selectedAppIndex != -1 || uiState !is DashboardUiState.Idle) {
                    // Show app details or processing state
                    AppDetailsContent(
                        appItemState = if (selectedAppIndex >= 0) appItemStates[selectedAppIndex] else null,
                        uiState = uiState,
                        onScanClick = {
                            if (selectedAppIndex >= 0) {
                                val app = appItemStates[selectedAppIndex]
                                scanApp(app) // Initiate app scanning
                            }
                        },
                        onRetryClick = { viewModel.resetState() } // Retry analysis
                    )
                } else {
                    // Show app grid
                    AppGrid(
                        apps = filteredApps,
                        onAppSelected = { app ->
                            selectedAppIndex = appItemStates.indexOf(app) // Select the app
                            isSearchActive = false // Exit search mode
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    isSearchActive: Boolean, // Indicates whether the search bar is currently visible/active
    searchText: String, // The current text entered in the search bar
    onSearchTextChanged: (String) -> Unit // Callback to handle changes in the search text
) {
    /**
     * Displays the search bar with an animated visibility transition.
     * The search bar expands vertically when activated and shrinks when deactivated.
     */
    AnimatedVisibility(
        visible = isSearchActive, // Controls the visibility of the search bar
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, // Adds a bouncy effect during expansion
                stiffness = Spring.StiffnessMedium // Controls the speed of the animation
            )
        ),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, // Adds a bouncy effect during shrinking
                stiffness = Spring.StiffnessMedium // Controls the speed of the animation
            )
        )
    ) {
        OutlinedTextField(
            value = searchText, // Binds the current search text to the text field
            onValueChange = onSearchTextChanged, // Updates the search text when the user types
            modifier = Modifier
                .fillMaxWidth() // Makes the search bar span the full width of its parent
                .padding(16.dp) // Adds padding around the search bar
                .graphicsLayer {
                    alpha = if (isSearchActive) 1f else 0f // Ensures smooth transitions for visibility
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Sets a semi-transparent background
                    shape = RoundedCornerShape(16.dp) // Rounds the corners of the search bar
                ),
            placeholder = {
                Text(
                    "Search apps...", // Placeholder text for the search bar
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Styles the placeholder text
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search, // Displays a search icon at the start of the text field
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary // Styles the search icon
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { onSearchTextChanged("") }) { // Clears the search text when clicked
                        Icon(
                            Icons.Default.Clear, // Displays a clear icon at the end of the text field
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.primary // Styles the clear icon
                        )
                    }
                }
            },
            singleLine = true, // Ensures the search bar remains a single line
            shape = RoundedCornerShape(16.dp) // Rounds the corners of the text field
        )
    }
}

@Composable
fun AppGrid(
    apps: List<AppItemState>, // List of apps to display in the grid
    onAppSelected: (AppItemState) -> Unit // Callback triggered when an app is selected
) {
    /**
     * Displays a grid of apps using a lazy vertical grid layout.
     * Each app is represented by an [AppItem] composable.
     */
    LazyVerticalGrid(
        contentPadding = PaddingValues(16.dp), // Adds padding around the grid
        columns = GridCells.Fixed(3), // Defines a fixed number of columns (3 in this case)
    ) {
        items(apps, key = { it.packageName }) { appItemState ->
            // Render each app item in the grid
            AppItem(
                appItemState = appItemState,
                isSelected = false, // No app is selected in the grid view
                onSelect = { onAppSelected(appItemState) } // Trigger selection callback
            )
        }
    }
}

@Composable
fun AppDetailsContent(
    appItemState: AppItemState?, // The selected app's state, or null if no app is selected
    uiState: DashboardUiState, // The current UI state for analysis
    onScanClick: () -> Unit, // Callback to initiate scanning
    onRetryClick: () -> Unit // Callback to retry dynamic analysis
) {
    /**
     * Displays detailed information about the selected app and handles analysis states.
     * This includes showing app details, a scan button, or analysis results based on the current state.
     */
    Column(
        modifier = Modifier
            .fillMaxWidth() // Ensures the column spans the full width of its parent
            .padding(16.dp), // Adds padding around the content
        horizontalAlignment = Alignment.CenterHorizontally // Centers content horizontally
    ) {
        // Display app info if available
        appItemState?.let {
            AppItem(
                appItemState = it,
                isSelected = true, // Highlights the selected app
                onSelect = {} // No action needed for selection in this context
            )
            Spacer(modifier = Modifier.height(16.dp)) // Adds spacing between app info and buttons
        }

        // Display different UI based on the current state
        when (uiState) {
            is DashboardUiState.Idle -> {
                // Show a "Scan App" button when no analysis is in progress
                Button(
                    onClick = onScanClick, // Triggers the scan process
                    modifier = Modifier.fillMaxWidth(), // Makes the button span the full width
                    shape = RoundedCornerShape(12.dp) // Rounds the corners of the button
                ) {
                    Text(
                        "Scan App",
                        fontWeight = FontWeight.Bold // Makes the text bold
                    )
                }
            }
            else -> {
                // All other states are handled by the AnalysisResultView composable
                AnalysisResultView(
                    uiState = uiState, // Passes the current UI state
                    onRetryDynamicAnalysis = onRetryClick // Provides a retry callback
                )
            }
        }
    }
}