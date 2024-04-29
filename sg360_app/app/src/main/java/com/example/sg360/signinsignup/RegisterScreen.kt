package com.example.sg360.signinsignup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sg360.R

/**
 * RegisterScreen is a Composable function that displays a screen for registering a new account.
 *
 * @param navigateToLogin A lambda function that navigates to the login screen when called.
 * @param navigateToVerify A lambda function that navigates to the verification screen when called.
 * @param signInUiState The current state of the sign-in process.
 * @param signInCall A lambda function that performs the sign-in process.
 *
 * @return Unit.
 */
@Composable
fun RegisterScreen(
    // Variables to hold user input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    
    // Variables to track errors
    var emailError by remember { mutableStateOf(false) }
    var passError by remember { mutableStateOf(false) }
    
    // Variables to control dialog behavior
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    
    // Main layout
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image and greeting
        Image(painter = painterResource(id = R.drawable.a), 
            contentDescription = "Login image",
            modifier = Modifier.size(160.dp))
        Text(text = "Register Here", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        
        // Input fields
        OutlinedTextField(value = email, 
            onValueChange = { email = it; emailError = false }, 
            label = { Text(text = "Email address") }, 
            supportingText = { Text(text = "*required") }, 
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ), singleLine= true,
            isError = emailError)
        
        // Button to trigger registration
        Button(onClick = {
            // Check input validity and set errors if needed
            if (!email.contains("@gmail.com")) {
                emailError = true
                showDialog = true
                answer = "Okay"
                title = "Email invalid"
                message = "Enter a valid email address"
            } else if (password.length <= 8){
                passError = true
                showDialog = true
                answer = "Okay"
                title = "Password invalid"
                message = "Password should have more then 8 characters"
            } else if (password != password2){
                passError = true
                showDialog = true
                answer = "Okay"
                title = "Password invalid"
                message = "Password should be same"
            }
            // Proceed with registration if no errors
            if (!emailError && !passError) {
                if (username.isEmpty()) {
                    username = email.split('@').getOrElse(0) { "" }
                }
                signInCall(email, username, password, password2, "True")
            }
        }) {
            Text(text = "Register")
        }
        
        // Display error dialog if needed
        if (showDialog) {
            ShowDialog(
                title = title,
                message = message,
                answer = answer,
                onDismiss = { showDialog = false }
            )
        }
        
        // Navigation options
        Text(text = "Already have an account? Sign up", modifier = Modifier.clickable {
            navigateToLogin()
        })
        when (signInUiState){
            is SignInUiState.Loading -> LoadingScreen()
            is SignInUiState.Success -> navigateToVerify()
            is SignInUiState.Error -> {
                ErrorScreen(signInUiState.errorMessage)
            }
        }
    }
}

// Composable function to display an AlertDialog with the specified title, message, answer, and onDismiss action.
@Composable
fun ShowDialog(title: String, message: String, answer: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = answer )
            }
        }
    )
}
