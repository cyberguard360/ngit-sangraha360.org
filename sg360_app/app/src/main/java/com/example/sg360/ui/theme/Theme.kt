package com.example.sg360.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Defines the dark color scheme for the SG360 app.
 *
 * This color scheme is used when the app is in dark mode or when dynamic colors are unavailable.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Defines the light color scheme for the SG360 app.
 *
 * This color scheme is used when the app is in light mode or when dynamic colors are unavailable.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Composable function to apply the SG360 app's theme.
 *
 * This function sets up the app's theme by selecting an appropriate color scheme based on:
 * - Whether the system is in dark mode.
 * - Whether dynamic colors are supported (available on Android 12+).
 *
 * It also adjusts the status bar appearance to match the selected theme.
 *
 * @param darkTheme Boolean indicating whether to use the dark theme. Defaults to the system setting.
 * @param dynamicColor Boolean indicating whether to use dynamic colors (if supported). Defaults to true.
 * @param content The content of the app to be displayed within the theme.
 */
@Composable
fun SG360Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Determine the appropriate color scheme based on the conditions
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Use dynamic colors if supported and enabled
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme // Use predefined dark color scheme
        else -> LightColorScheme // Use predefined light color scheme
    }

    // Get the current view to adjust the status bar appearance
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Adjust the status bar color and appearance to match the theme
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Apply the MaterialTheme with the selected color scheme and typography
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}