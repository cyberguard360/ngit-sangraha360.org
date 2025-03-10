package com.example.sg360.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sg360.R

/**
 * Displays a status notification to inform the user about background work progress.
 *
 * This function creates and displays a notification using the Android Notification system.
 * It ensures compatibility with different API levels by creating a notification channel
 * for devices running Android O (API 26) or higher. Additionally, it checks for the
 * `POST_NOTIFICATIONS` permission before displaying the notification.
 *
 * @param message The message to display in the notification.
 * @param context The application context used to create and display the notification.
 */
fun makeStatusNotification(message: String, context: Context) {

    // Create a notification channel for Android O (API 26) and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Define the notification channel properties
        val name = "SG360 WorkManager Notifications" // Name of the channel
        val description = "Shows notifications whenever work starts" // Description of the channel
        val importance = NotificationManager.IMPORTANCE_HIGH // Importance level of the channel
        val channel = NotificationChannel("SG360_NOTIFICATION", name, importance).apply {
            this.description = description // Set the description for the channel
        }

        // Register the notification channel with the system
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    // Build the notification
    val builder = NotificationCompat.Builder(context, "SG360_NOTIFICATION")
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Icon to display in the notification
        .setContentTitle("WorkRequest Starting") // Title of the notification
        .setContentText(message) // Content text of the notification
        .setPriority(NotificationCompat.PRIORITY_LOW) // Priority of the notification
        .setVibrate(LongArray(0)) // Disable vibration for the notification

    // Check for POST_NOTIFICATIONS permission (required for Android 13+)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // If permission is not granted, exit without showing the notification
        return
    }

    // Display the notification
    NotificationManagerCompat.from(context).notify(1, builder.build())
}