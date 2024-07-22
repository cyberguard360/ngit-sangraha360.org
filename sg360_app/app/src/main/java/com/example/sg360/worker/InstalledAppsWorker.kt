package com.example.sg360.worker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        // Do the work here--in this case, upload the images.
        makeStatusNotification(
            "Worker Running",
            applicationContext
        )
        return withContext(Dispatchers.IO) {

            return@withContext try {
                val packageManager: PackageManager = applicationContext.packageManager
                val installedApps = packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES
                ).filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }


                makeStatusNotification(
                    "Worker Success",
                    applicationContext
                )

                // Indicate whether the work finished successfully with the Result
                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    "InstalledAppsWorker",
                    "Error applying work",
                    throwable
                )
                Result.failure()
            }
        }
    }
}
