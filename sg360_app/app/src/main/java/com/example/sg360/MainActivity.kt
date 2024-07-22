package com.example.sg360

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.sg360.models.App
import com.example.sg360.models.AppDetail
import com.example.sg360.models.ClusterRequest
import com.example.sg360.network.SgApi
import com.example.sg360.ui.theme.SG360Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main activity for the SG360 application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Retrieve installed apps when the activity is created
            retrieveInstalledApps()

            // Set up the app's theme and navigation
            SG360Theme {
                Sg360NavHost(this@MainActivity)
            }
        }
    }

    /**
     * Retrieves a list of non-system applications installed on the device.
     *
     * @return A list of AppDetail objects representing the installed apps.
     */
    fun retrieveInstalledApps(): List<AppDetail> {
        val packageManager: PackageManager = packageManager

        // Get all installed applications and filter out system apps
        val installedApps: List<ApplicationInfo> = packageManager.getInstalledApplications(
            PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES
        ).filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }

        // Convert ApplicationInfo objects to AppDetail objects
        val appDetails: List<AppDetail> = installedApps.map { appInfo ->
            AppDetail(
                appName = appInfo.loadLabel(packageManager).toString(),
                packageName = appInfo.packageName,
                icon = appInfo.loadIcon(packageManager).toBitmap().asImageBitmap()
            )
        }

        // Sort the list based on the length of the app name string
        return appDetails.sortedBy { it.appName.length }
    }
}
