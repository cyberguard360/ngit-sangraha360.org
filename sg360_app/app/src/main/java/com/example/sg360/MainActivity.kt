package com.example.sg360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sg360.ui.theme.SG360Theme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the SG360 application.
 *
 * This is the entry point for the app's user interface. It is annotated with @AndroidEntryPoint
 * to enable dependency injection using Hilt. The activity uses Jetpack Compose for its UI framework.
 *
 * Responsibilities:
 * - Initialize the app's theme.
 * - Set up the navigation graph via [Sg360NavHost].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is created.
     *
     * This method initializes the activity by setting up the Jetpack Compose content.
     * The content includes the app's theme and navigation setup.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the Jetpack Compose content for the activity
        setContent {
            // Wrap the app's UI with the SG360Theme to apply consistent styling
            SG360Theme {
                // Set up the navigation host for the app
                Sg360NavHost()
            }
        }
    }
}