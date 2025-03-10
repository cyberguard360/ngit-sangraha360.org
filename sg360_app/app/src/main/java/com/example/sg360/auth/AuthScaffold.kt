package com.example.sg360.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A reusable scaffold for authentication-related screens.
 *
 * This composable provides a consistent layout for authentication screens, including a top app bar,
 * a centered content area, and optional back navigation. It ensures a uniform design across all
 * authentication-related screens (e.g., login, register, OTP verification).
 *
 * @param title The title displayed in the top app bar.
 * @param showBackButton Whether to display a back button in the top app bar. Defaults to `true`.
 * @param onBackPress The callback to handle back button presses. Required if `showBackButton` is `true`.
 * @param content The main content of the screen, provided as a composable lambda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScaffold(
    title: String, // Title displayed in the top app bar
    showBackButton: Boolean = true, // Whether to show the back button
    onBackPress: (() -> Unit)? = null, // Callback for back button press
    content: @Composable ColumnScope.() -> Unit // Main content of the screen
) {
    // Use Scaffold to provide a consistent structure for the screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontSize = 22.sp, // Set font size for the title
                        color = MaterialTheme.colorScheme.primary // Use primary color for the title
                    )
                },
                navigationIcon = {
                    // Show back button if enabled and a callback is provided
                    if (showBackButton && onBackPress != null) {
                        IconButton(onClick = onBackPress) {
                            Icon(
                                Icons.Filled.ArrowBack, // Back arrow icon
                                contentDescription = "Back" // Accessibility description
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        // Centered column for the main content
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the entire screen
                .background(MaterialTheme.colorScheme.surface) // Use surface color as background
                .padding(padding) // Apply padding from Scaffold
                .padding(horizontal = 20.dp), // Add horizontal padding for content
            verticalArrangement = Arrangement.Center, // Center content vertically
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            content = content // Pass the main content as a lambda
        )
    }
}