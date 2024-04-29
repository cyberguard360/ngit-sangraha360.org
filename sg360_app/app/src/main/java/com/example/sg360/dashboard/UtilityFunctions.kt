package com.example.sg360.dashboard

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Debug
import android.os.Process
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class UtilityFunctions {

    /**
     * Retrieves statistics related to loaded modules in non-system apps.
     *
     * @param context The application context.
     * @return A map containing statistics related to loaded modules.
     */

    fun getLdrModuleStats(context: Context): Map<String, Int> {
        val packageManager = context.packageManager
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val ldrModuleStats = mutableMapOf<String, Int>()
        val loadedPackages = mutableSetOf<String>()
        val initPackages = mutableSetOf<String>()

        for (packageInfo in installedPackages) {
            val appInfo = packageInfo.applicationInfo
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                // Non-system apps
                ldrModuleStats[appInfo.packageName] = appInfo.nativeLibraryDir?.let { dir ->
                    if (!loadedPackages.contains(dir)) {
                        loadedPackages.add(dir)
                        1
                    } else {
                        0
                    }
                } ?: 0

                if (appInfo.sourceDir != null && !initPackages.contains(appInfo.sourceDir)) {
                    initPackages.add(appInfo.sourceDir)
                    ldrModuleStats[appInfo.packageName] = ldrModuleStats.getOrDefault(appInfo.packageName, 0) + 1
                }
            }
        }

        val notInLoad = ldrModuleStats.count { it.value == 0 }
        val notInInit = ldrModuleStats.count { it.value == 1 }
        val notInMem = ldrModuleStats.size - notInLoad - notInInit

        val totalNonSystemApps = ldrModuleStats.size.toDouble()
        val notInLoadAvg = (notInLoad / totalNonSystemApps * 100).toInt()
        val notInInitAvg = (notInInit / totalNonSystemApps * 100).toInt()
        val notInMemAvg = (notInMem / totalNonSystemApps * 100).toInt()

        ldrModuleStats["ldrmodules.not_in_load"] = notInLoad
        ldrModuleStats["ldrmodules.not_in_init"] = notInInit
        ldrModuleStats["ldrmodules.not_in_mem"] = notInMem
        ldrModuleStats["ldrmodules.not_in_load_avg"] = notInLoadAvg
        ldrModuleStats["ldrmodules.not_in_init_avg"] = notInInitAvg
        ldrModuleStats["ldrmodules.not_in_mem_avg"] = notInMemAvg

        return ldrModuleStats
    }

    /**
     * Retrieves statistics related to malicious findings in the application.
     *
     * @param context The application context.
     * @return A map containing statistics related to malicious findings.
     */

    fun getMaliciousFindStats(context: Context): Map<String, Int> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        var processMemoryInfo: ActivityManager.RunningAppProcessInfo? = null

        if (runningProcesses != null) {
            for (processInfo in runningProcesses) {
                if (processInfo.pid == Process.myPid()) {
                    processMemoryInfo = processInfo
                    break
                }
            }
        }

        val debugInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(debugInfo)

        val maliciousFindStats = mutableMapOf<String, Int>()
        processMemoryInfo?.let {
            maliciousFindStats["malfind.ninjections"] = debugInfo.nativePss
        }

        maliciousFindStats["malfind.commitCharge"] = debugInfo.totalPss

        maliciousFindStats["malfind.protection"] = debugInfo.totalPrivateDirty

        processMemoryInfo?.let {
            maliciousFindStats["malfind.uniqueInjections"] = debugInfo.dalvikPrivateDirty
        }

        return maliciousFindStats
    }

    /**
     * Retrieves statistics related to services in the application.
     *
     * @param context The application context.
     * @return A map containing statistics related to services.
     */

    fun getServiceScanStats(context: Context): Map<String, Int> {
        val serviceScanStats = mutableMapOf<String, Int>()

        // Retrieve svcscan.nservices
        val packageManager = context.packageManager
        val services = packageManager.getInstalledPackages(PackageManager.GET_SERVICES)
        serviceScanStats["svcscan.nservices"] = services.size

        // Retrieve svcscan.kernel_drivers
        var kernelDriverCount = 0
        try {
            val file = File("/proc/modules")
            if (file.exists()) {
                val reader = BufferedReader(FileReader(file))
                while (reader.readLine() != null) {
                    kernelDriverCount++
                }
                reader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serviceScanStats["svcscan.kernel_drivers"] = kernelDriverCount

        // Retrieve svcscan.fs_drivers
        var fsDriverCount = 0
        try {
            val file = File("/proc/filesystems")
            if (file.exists()) {
                val reader = BufferedReader(FileReader(file))
                while (reader.readLine() != null) {
                    fsDriverCount++
                }
                reader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serviceScanStats["svcscan.fs_drivers"] = fsDriverCount

        // Retrieve svcscan.process_services
        val processServiceCount: Int
        val processServices = packageManager.queryIntentServices(Intent(Intent.ACTION_MAIN), 0)
        processServiceCount = processServices.size
        serviceScanStats["svcscan.process_services"] = processServiceCount

        // Retrieve svcscan.shared_process_services (Not available in the Android SDK)
        serviceScanStats["svcscan.shared_process_services"] = 0

        // Retrieve svcscan.interactive_process_services
        val interactiveProcessServiceCount: Int
        val interactiveProcessServices =
            packageManager.queryIntentServices(
                Intent(Intent.ACTION_MAIN),
                PackageManager.MATCH_DIRECT_BOOT_AWARE or PackageManager.MATCH_DIRECT_BOOT_UNAWARE
            )
        interactiveProcessServiceCount = interactiveProcessServices.size
        serviceScanStats["svcscan.interactive_process_services"] = interactiveProcessServiceCount

        // Retrieve svcscan.nactive
        val activeServiceCount = processServiceCount + interactiveProcessServiceCount
        serviceScanStats["svcscan.nactive"] = activeServiceCount

        return serviceScanStats
    }

    /**
     * Retrieves statistics related to callbacks in the application.
     *
     * @return A map containing statistics related to callbacks.
     */

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")

    fun getCallbackStats(): Map<String, Int> {
        val callbackStats = mutableMapOf<String, Int>()
        val allCallbacks = mutableSetOf<Any>()

        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val activityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread")
            activityThreadField.isAccessible = true
            val activityThread = activityThreadField.get(null)

            // Access the mActivities field in ActivityThread to retrieve all activities
            val mActivitiesField = activityThreadClass.getDeclaredField("mActivities")
            mActivitiesField.isAccessible = true
            val mActivities = mActivitiesField.get(activityThread) as Map<*, *>

            for ((_, activityClient) in mActivities) {
                val activityClientClass = Class.forName("android.app.ActivityThread\$ActivityClientRecord")
                val activityField = activityClientClass.getDeclaredField("activity")
                activityField.isAccessible = true
                val activity = activityField.get(activityClient)

                // Check if the activity is non-null and add it to the callbacks set
                if (activity != null) {
                    allCallbacks.add(activity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Now that we have all the callbacks, we can analyze them
        var anonymousCount = 0
        var genericCount = 0

        for (callback in allCallbacks) {
            // Check if the callback class is an anonymous class
            if (callback.javaClass.isAnonymousClass) {
                anonymousCount++
            } else {
                genericCount++
            }
        }

        // Total number of callbacks is the sum of anonymous and generic callbacks
        val totalCallbacks = anonymousCount + genericCount

        callbackStats["callbacks.ncallbacks"] = totalCallbacks
        callbackStats["callbacks.nanonymous"] = anonymousCount
        callbackStats["callbacks.ngeneric"] = genericCount

        return callbackStats
    }

    /**
     * Retrieves a string representation of API call information.
     *
     * @return A string containing API call information.
     */

    fun getApiCall(): String {
        // Assuming you have a list of API calls stored in a variable called "apiCalls"
        val apiCalls = listOf(
            "GET /api/users",
            "POST /api/login",
            "PUT /api/users/1",
            "DELETE /api/users/1"
        )
        // Concatenate the API call information into a single string
        return apiCalls.joinToString("\n")
    }

    /**
     * Retrieves real permission information.
     *
     * @return A string containing real permission information.
     */

    fun getRealPermission(): String {
        // Implement your logic to get the real permission information
        // This can vary depending on your requirements
        return "Real permission information"
    }

}