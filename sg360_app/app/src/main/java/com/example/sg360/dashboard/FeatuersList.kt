package com.example.sg360.dashboard

import android.app.ActivityManager
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.Debug
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import com.example.sg360.MainActivity
import com.example.sg360.models.StatsData
import java.io.File


class staticFeatures(context: Context) {
    val utilityFunctions = UtilityFunctions()
    val packageManager: PackageManager = context.packageManager


    /**
     * Refreshes the statistics for the given context, username, and selected app.
     *
     * @param context The context to use for retrieving system services.
     * @param username The username to associate with the statistics.
     * @param selectedApp The selected app to retrieve statistics for.
     * @return The StatsData object containing the refreshed statistics.
     */

     
    fun refreshStats(context : Context, username: String?, selectedApp: String): StatsData{

        val activityManager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager

        val processCount = activityManager.runningAppProcesses.size

        val totalThreadCount = Thread.getAllStackTraces().size


        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        var proc64BitCount = 0

        for (packageInfo in packages) {
            val appInfo = packageInfo.applicationInfo
            if (appInfo.nativeLibraryDir.contains("64")) {
                proc64BitCount++
            }
        }
        val ldrModuleStats = utilityFunctions.getLdrModuleStats(context)
        val maliciousFindStats = utilityFunctions.getMaliciousFindStats(context)
        val serviceScanStats = utilityFunctions.getServiceScanStats(context)
        val callbackStats = utilityFunctions.getCallbackStats()

        val memoryStats = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryStats)

        val statsData = StatsData(
            username = username,
            APICall = utilityFunctions.getApiCall(),
            Activity = getActivity(selectedApp),
            Call = getCall(selectedApp),
            Feature = getFeature(selectedApp),
            MemoryHeapAlloc = (memoryStats.dalvikPrivateDirty + memoryStats.nativePrivateDirty + memoryStats.otherPrivateDirty).toDouble(),
            MemoryHeapFree = (memoryStats.dalvikPss + memoryStats.nativePss + memoryStats.otherPss - memoryStats.dalvikPrivateDirty - memoryStats.nativePrivateDirty - memoryStats.otherPrivateDirty).toDouble(),
            MemoryHeapSize = (memoryStats.dalvikPss + memoryStats.nativePss + memoryStats.otherPss).toDouble(),
            MemoryParcelMemory = memoryStats.nativePss.toDouble(),
            MemoryPrivateClean = memoryStats.totalPrivateClean.toDouble(),
            MemoryPrivateDirty = memoryStats.dalvikPrivateDirty.toDouble(),
            Memory_PssTotal = memoryStats.totalPss.toDouble(),
            MemorySharedClean = memoryStats.totalSharedClean.toDouble(),
            MemoryPssClean = memoryStats.dalvikPss.toDouble(),
            MemorySharedDirty = memoryStats.dalvikSharedDirty.toDouble(),
            Permission = getPermission(selectedApp),
            Provider =  getProvider(selectedApp),
            RealPermission = utilityFunctions.getRealPermission(),
            ServiceReceiver = getServiceReceiver(selectedApp),
            URL = getUrl(selectedApp),
            callbacks_nanonymous = callbackStats["callbacks.ncallbacks"]?.toDouble() ?: 0.0,
            callbacks_ncallbacks = callbackStats["callbacks.ncallbacks"]?.toDouble() ?: 0.0,
            callbacks_ngeneric = callbackStats["callbacks.ngeneric"]?.toDouble() ?: 0.0,
            handles_nfile = (File("/proc/self/fd").listFiles()?.size ?: 0).toDouble(),
            handles_nthread = (Thread.getAllStackTraces().keys.size).toDouble(),
            ldrmodules_not_in_init = ldrModuleStats["ldrmodules.not_in_init_avg"]?.toDouble() ?: 0.0,
            ldrmodules_not_in_init_avg = ldrModuleStats["ldrmodules.not_in_init_avg"]?.toDouble() ?: 0.0,
            ldrmodules_not_in_load = ldrModuleStats["ldrmodules.not_in_load_avg"]?.toDouble() ?: 0.0,
            ldrmodules_not_in_load_avg = ldrModuleStats["ldrmodules.not_in_load_avg"]?.toDouble() ?: 0.0,
            ldrmodules_not_in_mem = ldrModuleStats["ldrmodules.not_in_mem_avg"]?.toDouble() ?: 0.0,
            ldrmodules_not_in_mem_avg = ldrModuleStats["ldrmodules.not_in_mem_avg"]?.toDouble() ?: 0.0,
            malfind_commitCharge = maliciousFindStats["malfind.commitCharge"]?.toDouble() ?: 0.0,
            malfind_ninjections = maliciousFindStats["malfind.ninjections"]?.toDouble() ?: 0.0,
            malfind_protection = maliciousFindStats["malfind.protection"]?.toDouble() ?: 0.0,
            malfind_uniqueInjections = maliciousFindStats["malfind.uniqueInjections"]?.toDouble() ?: 0.0,
            pslist_nproc = activityManager.runningAppProcesses.size.toDouble(),
            pslist_avg_threads = (totalThreadCount.toDouble() / processCount.toDouble()),
            pslist_nprocs64bit = proc64BitCount.toDouble(),
            svcscan_fs_drivers = serviceScanStats["svcscan.fs_drivers"]?.toDouble() ?: 0.0,
            svcscan_interactive_process_services = serviceScanStats["svcscan.interactive_process_services"]?.toDouble() ?: 0.0,
            svcscan_kernel_drivers = serviceScanStats["svcscan.kernel_drivers"]?.toDouble() ?: 0.0,
            svcscan_nactive = serviceScanStats["svcscan.nactive"]?.toDouble() ?: 0.0,
            svcscan_nservices = serviceScanStats["svcscan.nservices"]?.toDouble() ?: 0.0,
            svcscan_process_services = serviceScanStats["svcscan.process_services"]?.toDouble() ?: 0.0,
            svcscan_shared_process_services =  serviceScanStats["svcscan.shared_process_services"]?.toDouble() ?: 0.0,
            totalReceivedBytes = TrafficStats.getTotalRxBytes().toDouble(),
            totalReceivedPackets = TrafficStats.getTotalRxPackets().toDouble(),
            totalTransmittedBytes = TrafficStats.getTotalTxBytes().toDouble(),
            totalTransmittedPackets = TrafficStats.getTotalTxPackets().toDouble()
        )
        return statsData
    }


    // Function that retrieves permissions for a selected app.
    private fun getPermission(selectedApp: String): String {
        val selectedAppInfo =
            MainActivity.installedApps.find { it.loadLabel(packageManager).toString() == selectedApp }

        selectedAppInfo?.let {
            val packageName = selectedAppInfo.packageName
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )
            val permissions = packageInfo.requestedPermissions
            return permissions?.joinToString("\n") ?: "No permissions found"
        }
        return "Permission information not available"
    }


    /**
     * Get the URL metadata of the selected app.
     *
     * @return A string containing the URL metadata or a message if not found.
     */
    private fun getUrl(selectedApp: String): String {
        val selectedAppInfo =
            MainActivity.installedApps.find { it.loadLabel(packageManager).toString() == selectedApp }

        selectedAppInfo?.let {
            val packageName = selectedAppInfo.packageName
            val appInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val url = appInfo.metaData?.getString("url")
            return url ?: "URL not found"
        }
        return "URL information not available"
    }


    /**
     * Get the content providers of the selected app.
     *
     * @return A string containing the content providers or a message if none found.
     */

    private fun getProvider(selectedApp: String): String {
        val selectedAppInfo =
            MainActivity.installedApps.find { it.loadLabel(packageManager).toString() == selectedApp }

        selectedAppInfo?.let {
            val packageName = selectedAppInfo.packageName
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_PROVIDERS)
            val providers = packageInfo.providers
            return providers?.joinToString("\n") ?: "No providers found"
        }
        return "Provider information not available"
    }


    /**
     * Get the features required by the selected app.
     *
     * @return A string containing the required features or a message if none found.
     */

    private fun getFeature(selectedApp: String): String {
        val selectedAppInfo =
            MainActivity.installedApps.find { it.loadLabel(packageManager).toString() == selectedApp }

        selectedAppInfo?.let {
            val packageName = selectedAppInfo.packageName
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS)
            val features = packageInfo.reqFeatures
            return features?.joinToString("\n") ?: "No features found"
        }
        return "Feature information not available"
    }


    /**
     * Get the activities provided by the selected app.
     *
     * @return A string containing the activities or a message if none found.
     */

    private fun getActivity(selectedApp: String): String {
        val selectedAppInfo =
            MainActivity.installedApps.find { it.loadLabel(packageManager).toString() == selectedApp }

        selectedAppInfo?.let {
            val packageName = selectedAppInfo.packageName
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            val activities = packageInfo.activities
            return activities?.joinToString("\n") ?: "No activities found"
        }
        return "Activity information not available"
    }


    /**
     * Get the call-related permissions of the selected app.
     *
     * @return A string containing the call-related permissions or a message if none found.
     */

    private fun getCall(selectedApp: String): String {
        val selectedAppInfo =
            MainActivity.installedApps.find { it.loadLabel(packageManager).toString() == selectedApp }

        selectedAppInfo?.let {
            val packageName = selectedAppInfo.packageName
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val permissions = packageInfo.requestedPermissions
            val callPermissions =
                permissions?.filter { it.startsWith("android.permission.CALL") }
            return callPermissions?.joinToString("\n") ?: "No call permissions found"
        }
        return "Call information not available"
    }


    /**
     * Get the service receivers used by the selected app.
     *
     * @return A string containing the service receivers or a message if none found.
     */

    private fun getServiceReceiver(selectedApp: String): String {

        val selectedAppInfo =
            MainActivity.installedApps.find { it.loadLabel(packageManager).toString() == selectedApp }

        selectedAppInfo?.let {
            val packageName = selectedAppInfo.packageName
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_RECEIVERS)
            val receivers = packageInfo.receivers
            return receivers?.joinToString("\n") ?: "No service receivers found"
        }
        return "Service receiver information not available"
    }
}



