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
    companion object {
        lateinit var installedApps: List<ApplicationInfo>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SG360Theme {
                retrieveInstalledApps()
                Sg360NavHost(this@MainActivity)
            }
        }
    }
    fun retrieveInstalledApps(): List<String> {
        val packageManager: PackageManager = packageManager
        installedApps = packageManager.getInstalledApplications(
            PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES
        )
        return installedApps.map { it.loadLabel(packageManager).toString() }
    }

    fun appFeatures(username: String?, selectedAppName: String): StatsData {
        val features = staticFeatures(this)
        return features.refreshStats(this, username, selectedAppName)
    }
}