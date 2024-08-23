package com.example.sg360.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sg360.models.AppItemState

@Composable
fun DashBoard(
    appItemStates: List<AppItemState>,
    scanAllApps: () -> Unit,
    refresh: () -> Unit,
    scanApp: (String) -> String
) {
    var selectedAppIndex by remember { mutableStateOf(-1) }
    var selectedCategory by remember { mutableStateOf("All") }

    // Function to filter apps based on selected category
    fun filterApps(category: String): List<AppItemState> {
        return when (category) {
            "All" -> appItemStates
            "Malware" -> appItemStates.filter { it.result == "Malware" }
            "Benign" -> appItemStates.filter { it.result == "Benign" }
            "Unknown" -> appItemStates.filter { it.result == "Unknown" }
            "Not Scanned" -> appItemStates.filter { it.result == null }
            else -> emptyList()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Slide with categories
            SlideCategories(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                }
            )

            val filteredApps = filterApps(selectedCategory)
            if (selectedAppIndex == -1) {
                // Show grid of apps
                LazyVerticalGrid(
                    contentPadding = PaddingValues(16.dp),
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredApps) { appItemState ->
                        AppItem(
                            appItemState = appItemState,
                            isSelected = appItemStates.indexOf(appItemState) == selectedAppIndex,
                            onSelect = {
                                selectedAppIndex = appItemStates.indexOf(appItemState)
                            },
                            scanApp = scanApp // Pass the scanApp function
                        )
                    }
                }
            } else {
                // Show selected app in the center
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AppItem(
                        appItemState = filteredApps[selectedAppIndex],
                        isSelected = true,
                        onSelect = {},
                        scanApp = scanApp
                    )
                }

                // Back button to deselect the app
                Button(
                    onClick = { selectedAppIndex = -1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Back")
                }
            }
        }

        // Button to trigger scan all apps
        if (selectedAppIndex == -1) {
            Button(
                onClick = {
                    refresh()
                    scanAllApps()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text("Scan All Apps")
            }
        }
    }
}

@Composable
fun SlideCategories(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Malware", "Unknown", "Benign", "Not Scanned")

    ScrollableTabRow(
        selectedTabIndex = categories.indexOf(selectedCategory),
    ) {
        categories.forEachIndexed { _, category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}