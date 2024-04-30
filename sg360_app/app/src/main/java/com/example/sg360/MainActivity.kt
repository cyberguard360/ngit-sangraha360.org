package com.example.sg360

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sg360.dashboard.staticFeatures
import com.example.sg360.models.StatsData
import com.example.sg360.ui.theme.SG360Theme

class MainActivity : ComponentActivity() {
    // Static list of installed apps to be accessed across the app
    companion object {
        lateinit var installedApps: List<ApplicationInfo>
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call the superclass's onCreate method and set the content of the activity
        super.onCreate(savedInstanceState)
        setContent {
            // Set the theme of the activity and display the main navigation host
            SG360Theme {
                // Retrieve the list of installed apps and store it in the companion object
                retrieveInstalledApps()
                // Display the main navigation host for the app
                Sg360NavHost(this@MainActivity)
            }
        }
    }
    
    // Retrieve the list of installed apps and store it in the companion object
    fun retrieveInstalledApps(): List<String> {
        // Get the package manager to access the installed apps
        val packageManager: PackageManager = packageManager
        // Retrieve the list of installed apps and store it in the companion object
        installedApps = packageManager.getInstalledApplications(
            PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES
        )
        // Return the list of app names
        return installedApps.map { it.loadLabel(packageManager).toString() }
    }

    // Refresh the stats for a specific app based on the selected app name and username
    fun appFeatures(username: String?, selectedAppName: String): StatsData {
        // Get the static features for the app
        val features = staticFeatures(this)
        // Refresh the stats for the selected app and return the updated stats
        return features.refreshStats(this, username, selectedAppName)
    }
}