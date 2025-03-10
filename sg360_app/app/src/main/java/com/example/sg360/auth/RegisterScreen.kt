package com.example.sg360.auth

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

/**
 * Displays the registration screen for user account creation.
 *
 * This composable provides a form for users to input their email, username, and password.
 * It also includes a "Register" button to submit the registration request.
 *
 * @param navController The navigation controller used to navigate back or to other screens.
 * @param viewModel The [AuthViewModel] instance managing the registration logic and state.
 */
@Composable
fun RegisterScreen(navController: NavHostController, viewModel: AuthViewModel) {
    // Use AuthScaffold to provide a consistent layout for authentication screens
    AuthScaffold(
        title = "Register", // Title displayed in the top app bar
        onBackPress = { navController.popBackStack() } // Navigate back when the back button is pressed
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

        // Username input field
        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it }, // Update the username in the ViewModel
            label = { Text("Username") },
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

        // Register button to submit the registration request
        Button(
            onClick = { viewModel.register(navController) }, // Trigger registration logic in the ViewModel
            modifier = Modifier.fillMaxWidth(), // Make the button span the full width
            shape = RoundedCornerShape(12.dp) // Rounded corners for the button
        ) {
            Text(
                "Register",
                color = MaterialTheme.colorScheme.onPrimary // Use theme colors for consistency
            )
        }
    }
}