import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sg360.Routes
import com.example.sg360.auth.AuthScaffold

/**
 * Displays the OTP verification screen.
 *
 * This composable provides a user interface for entering a one-time password (OTP) and navigating
 * to the dashboard upon successful verification. It includes an input field for the OTP and a
 * "Verify" button to proceed.
 *
 * @param navController The navigation controller used to navigate to the dashboard or other screens.
 */
@Composable
fun VerificationScreen(navController: NavHostController) {
    // Use AuthScaffold to provide a consistent layout for authentication screens
    AuthScaffold("Verify OTP") {
        var otp by remember { mutableStateOf("") } // Tracks the OTP entered by the user

        // Input field for OTP
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 4) otp = it }, // Restrict input to 4 characters
            label = { Text("Enter OTP") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, // Only allow numeric input
                imeAction = ImeAction.Done // Show "Done" action on the keyboard
            ),
            singleLine = true, // Ensure the input stays on a single line
            shape = RoundedCornerShape(12.dp) // Rounded corners for the text field
        )

        Spacer(modifier = Modifier.height(16.dp)) // Add spacing between the input field and button

        // Verify button to navigate to the dashboard
        Button(
            onClick = {
                // Navigate to the dashboard screen upon clicking the button
                navController.navigate(Routes.dashboard)
            },
            modifier = Modifier.fillMaxWidth(), // Make the button span the full width
            shape = RoundedCornerShape(12.dp) // Rounded corners for the button
        ) {
            Text("Verify") // Button label
        }
    }
}