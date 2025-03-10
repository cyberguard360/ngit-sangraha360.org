package com.example.sg360.dashboard

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.sg360.models.AppItemState
import com.example.sg360.models.UploadStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppItem(
    appItemState: AppItemState, // Represents the state of the app being displayed
    isSelected: Boolean, // Indicates whether this app item is currently selected
    onSelect: () -> Unit // Callback triggered when the app item is clicked
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var showAppDetailsDropdown by remember { mutableStateOf(false) } // Tracks whether app details are expanded

    // Fetch package info for the app
    val packageInfo by remember {
        mutableStateOf(
            packageManager.getPackageInfo(
                appItemState.packageName,
                PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_CONFIGURATIONS
            )
        )
    }

    /**
     * Displays an app item with its icon, name, and optional app details dropdown.
     */
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onSelect) // Trigger selection callback
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) // Highlight selected app
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), // Default background
                shape = RoundedCornerShape(16.dp) // Rounded corners
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp, // Add border for selected app
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the app icon
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(24.dp)) // Circular clipping for the icon
        ) {
            Image(
                bitmap = appItemState.icon,
                contentDescription = "Icon for ${appItemState.name}",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(if (isSelected) 1.1f else 1f) // Slightly enlarge icon when selected
            )
        }

        // Display the app name
        Text(
            text = appItemState.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold, // Bold for selected app
                color = if (isSelected) MaterialTheme.colorScheme.primary // Highlight text color
                else MaterialTheme.colorScheme.onSurface // Default text color
            ),
            modifier = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis // Handle long names gracefully
        )

        // Show app details dropdown if the app is selected
        if (isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppDetailsDropdown = !showAppDetailsDropdown } // Toggle dropdown visibility
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Details",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (showAppDetailsDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Animate the visibility of the app details dropdown
            AnimatedVisibility(visible = showAppDetailsDropdown) {
                AppDetails(appItemState, packageInfo)
            }
        }
    }
}

@Composable
fun AppDetails(
    selectedApp: AppItemState, // Represents the app whose details are being displayed
    packageInfo: PackageInfo // Contains detailed information about the app's package
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val applicationInfo = packageInfo.applicationInfo

    // Format install and update dates
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val firstInstallTime = dateFormat.format(Date(packageInfo.firstInstallTime))
    val lastUpdateTime = dateFormat.format(Date(packageInfo.lastUpdateTime))

    // Determine the installer source
    var installer = packageManager.getInstallerPackageName(selectedApp.packageName) ?: "Unknown"
    if (installer == "com.android.vending") installer = "Google Play Store"

    // Storage details
    val storageDir = applicationInfo?.dataDir ?: "Not Available"
    val cacheDir = "${storageDir}/cache"

    // Calculate the app size
    val appSizeBytes = applicationInfo?.sourceDir?.let { File(it).length() } ?: 0
    val formattedAppSize = formatFileSize(appSizeBytes)

    /**
     * Displays detailed information about the app, including metadata and expandable sections.
     */
    Column(
        modifier = Modifier.padding(6.dp)
    ) {
        // Display basic app details
        DetailRow("Package Name", selectedApp.packageName)
        DetailRow("Installed On", firstInstallTime)
        DetailRow("Last Updated", lastUpdateTime)
        DetailRow("Installed From", installer)
        DetailRow("App Size", formattedAppSize)
        DetailRow("Data Directory", storageDir)
        DetailRow("Cache Directory", cacheDir)

        // Expandable sections for permissions, activities, services, providers, and receivers
        ExpandableSection("Permissions", packageInfo.requestedPermissions)
        ExpandableSection("Activities", packageInfo.activities?.map { it.name }?.toTypedArray())
        ExpandableSection("Services", packageInfo.services?.map { it.name }?.toTypedArray())
        ExpandableSection("Providers", packageInfo.providers?.map { it.name }?.toTypedArray())
        ExpandableSection("Receivers", packageInfo.receivers?.map { it.name }?.toTypedArray())
    }
}

/**
 * Displays a row with a label and corresponding value.
 *
 * This composable is used to display detailed information in a structured format,
 * with the label on the left and the value on the right.
 *
 * @param label The descriptive label for the detail (e.g., "Package Name").
 * @param value The corresponding value for the label (e.g., "com.example.app").
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Adds vertical padding for spacing
        horizontalArrangement = Arrangement.SpaceBetween // Aligns label and value at opposite ends
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold // Makes the label bold for emphasis
        )
        Text(
            text = value,
            maxLines = 2, // Limits the value to two lines to prevent overflow
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Styles the value text
        )
    }
}

/**
 * Displays an expandable section for a list of items.
 *
 * This composable shows a title and an expand/collapse icon. When expanded, it displays
 * the list of items in a scrollable lazy column.
 *
 * @param title The title of the expandable section (e.g., "Permissions").
 * @param items An array of strings representing the items to display (e.g., permission names).
 */
@Composable
fun ExpandableSection(title: String, items: Array<String>?) {
    var expanded by remember { mutableStateOf(false) } // Tracks whether the section is expanded

    if (!items.isNullOrEmpty()) { // Only render the section if there are items to display
        Column(modifier = Modifier.fillMaxWidth()) {
            // Title row with clickable behavior to toggle expansion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded } // Toggles the expanded state
                    .padding(8.dp), // Adds padding for better touch targets
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold // Makes the title bold for emphasis
                )
                Spacer(modifier = Modifier.weight(1f)) // Pushes the icon to the far right
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand" // Accessibility description
                )
            }

            // Animated visibility for the list of items
            AnimatedVisibility(visible = expanded) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { // Limits the height to 200dp
                    items(items.size) { index ->
                        Text(
                            text = items[index],
                            style = MaterialTheme.typography.bodySmall, // Uses a smaller font size
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp) // Adds indentation and spacing
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats a file size into a human-readable string.
 *
 * This utility function converts a file size in bytes into a more readable format,
 * such as KB, MB, or GB, depending on the size.
 *
 * @param size The file size in bytes.
 * @return A formatted string representing the file size (e.g., "5.23 MB").
 */
fun formatFileSize(size: Long): String {
    return when {
        size >= 1_073_741_824 -> "%.2f GB".format(size / 1_073_741_824.0) // Converts to gigabytes
        size >= 1_048_576 -> "%.2f MB".format(size / 1_048_576.0) // Converts to megabytes
        size >= 1024 -> "%.2f KB".format(size / 1024.0) // Converts to kilobytes
        else -> "$size B" // Displays size in bytes
    }
}