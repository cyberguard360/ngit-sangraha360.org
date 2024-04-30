package com.example.sg360.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sg360.MainActivity
import com.example.sg360.models.PredictResponse
import com.example.sg360.network.SgApi
import com.example.sg360.signinsignup.ErrorScreen
import com.example.sg360.signinsignup.LoadingScreen


/**
 * Renders the Dashboard screen with a list of APK names, a button to select an APK, and a loading/result screen based on the current dashboard UI state.
 *
 * @param apkNames A list of strings representing the names of the APKs.
 * @param dashBoardUi The current UI state of the dashboard.
 * @param sendData A function that takes a string and sends data based on the selected APK.
 * @return A Composable function that renders the Dashboard screen.
 */
@Composable
fun DashBoard(
    apkNames: List<String>,
    dashBoardUi: DashBoardUiState,
    sendData: (String) -> Unit
) {
    var selectedApp: String by remember { mutableStateOf("Select Application") }

    Column {
        AppList(apkNames) { selectedAppName ->
            selectedApp = selectedAppName
        }
        Button(
            onClick = {
                if (selectedApp != "Select Application") {
                    sendData(selectedApp)
                } else {
                    // Handle case when nothing is selected
                    // For example, show a toast or display an error message
                }

            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Scan")
        }
        // Display UI based on dashboard UI state
        when (dashBoardUi) {
            is DashBoardUiState.Loading -> LoadingScreen()
            is DashBoardUiState.Success -> ResultScreen(
                photos = dashBoardUi.photos,
            )
            is DashBoardUiState.Error -> {}
        }
    }
}

/**
 * ResultScreen displaying number of photos retrieved.
 */
@Composable
fun ResultScreen(photos: PredictResponse, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = photos.label)
        SliderAdvancedExample(photos.confidenceLevel)
    }
}

@Composable
fun SliderAdvancedExample(sliderPosition: Int) {
    Column {
        Slider(
            value = sliderPosition.toFloat(),
            onValueChange = {},
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            valueRange = 0f..100f
        )
        Text(text = sliderPosition.toString())
    }
}