package com.example.sg360.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sg360.Routes

/**
 * Displays the login screen for user authentication.
 *
 * This composable provides a form for users to input their email and password.
 * It also includes a "Login" button to submit the login request and a clickable
 * text to navigate to the registration screen.
 *
 * @param navController The navigation controller used to navigate to other screens.
 * @param viewModel The [AuthViewModel] instance managing the login logic and state.
 */
@Composable
fun LoginScreen(navController: NavHostController, viewModel: AuthViewModel) {
    // Use AuthScaffold to provide a consistent layout for authentication screens
    AuthScaffold(
        title = "Login", // Title displayed in the top app bar
        showBackButton = false // Hide the back button on the login screen
    ) {
        // Email input field
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it }, // Update the email in the ViewModel
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, // Use email-specific keyboard
                imeAction = ImeAction.Next // Move focus to the next field
            ),
            singleLine = true, // Ensure the input stays on a single line
            shape = RoundedCornerShape(12.dp) // Rounded corners for the text field
        )
        Spacer(modifier = Modifier.height(8.dp)) // Add spacing between fields

        // Password input field
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it }, // Update the password in the ViewModel
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Submit on "Done"
            visualTransformation = PasswordVisualTransformation(), // Mask the password input
            singleLine = true, // Ensure the input stays on a single line
            shape = RoundedCornerShape(12.dp) // Rounded corners for the text field
        )
        Spacer(modifier = Modifier.height(16.dp)) // Add spacing before the button

        // Login button to submit the login request
        Button(
            onClick = { viewModel.login(navController) }, // Trigger login logic in the ViewModel
            modifier = Modifier.fillMaxWidth(), // Make the button span the full width
            shape = RoundedCornerShape(12.dp) // Rounded corners for the button
        ) {
            Text(
                "Login",
                color = MaterialTheme.colorScheme.onPrimary // Use theme colors for consistency
            )
        }
        Spacer(modifier = Modifier.height(8.dp)) // Add spacing before the registration link

        // Clickable text to navigate to the registration screen
        Text(
            "Don't have an account? Register",
            color = MaterialTheme.colorScheme.primary, // Highlight the text with the primary color
            modifier = Modifier.clickable {
                navController.navigate(Routes.register) // Navigate to the registration screen
            }
        )
    }
}