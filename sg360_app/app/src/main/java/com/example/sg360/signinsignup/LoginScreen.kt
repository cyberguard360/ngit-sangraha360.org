package com.example.sg360.signinsignup

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
 * Composable function for the login screen.
 *
 * @param navigateToRegister Function to navigate to the registration screen.
 * @param navigateToDashBoard Function to navigate to the dashboard screen.
 * @param signInUiState The current state of the sign-in process.
 * @param signInCall Function to initiate the sign-in process.
 *
 * @return The rendered login screen.
 */
@Composable
fun LoginScreen(
    navigateToRegister: () -> Unit,  // Function to navigate to the registration screen
    navigateToDashBoard: () -> Unit,  // Function to navigate to the dashboard screen
    signInUiState: SignInUiState,  // The current state of the sign-in process
    signInCall: (email: String, password: String) -> Unit  // Function to initiate the sign-in process
) {

    var email by remember {  // The email entered by the user
        mutableStateOf("")
    }

    var password by remember {  // The password entered by the user
        mutableStateOf("")
    }

    var emailError by remember {  // Whether the entered email is invalid
        mutableStateOf(false)
    }

    var passError by remember {  // Whether the entered password is invalid
        mutableStateOf(false)
    }

    var showDialog by remember {  // Whether to show the error dialog
        mutableStateOf(false)
    }

    var title by remember {  // The title of the error dialog
        mutableStateOf("")
    }

    var message by remember {  // The message of the error dialog
        mutableStateOf("")
    }


    var answer by remember {  // The answer to the error dialog
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize(),  // Fill the parent size
        verticalArrangement = Arrangement.Center,  // Center the children vertically
        horizontalAlignment = Alignment.CenterHorizontally  // Center the children horizontally
    ) {
        Image( // Display the login image
            painter = painterResource(id = R.drawable.a),
            contentDescription = "Login image",
            modifier = Modifier.size(200.dp)
        )

        Text(text = "Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold)  // Display the welcome text

        Spacer(modifier = Modifier.height(4.dp))  // Add some spacing

        Text(text = "Login to your account")  // Display the login text

        Spacer(modifier = Modifier.height(16.dp))  // Add some spacing

        OutlinedTextField( // Display the email input field
            value = email, onValueChange = {
                email = it
                emailError = false
            }, label = {
                Text(text = "Email address")
            }, supportingText = {
                Text(text = "*required")
            }, keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ), isError =emailError,
            singleLine= true)

        Spacer(modifier = Modifier.height(16.dp))  // Add some spacing

        OutlinedTextField( // Display the password input field
            value = password, onValueChange = {
                password = it
                passError = false
            }, label = {
                Text(text = "Password")
            }, supportingText = {
                Text(text = "*required")
            }, keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ), singleLine= true ,
            isError =passError,
            visualTransformation = PasswordVisualTransformation())

        Spacer(modifier = Modifier.height(16.dp))  // Add some spacing

        Button(onClick = {
            if (!email.contains("@gmail.com")) { // Check if the entered email is valid
                emailError = true
                showDialog = true
                answer = "Okay"
                title = "Email invalid"
                message = "Enter a valid email address"
            } else if (password.length <= 8){ // Check if the entered password is valid
                passError = true
                showDialog = true
                answer = "Okay"
                title = "Password invalid"
                message = "Password should have more then 8 characters"
            }
            if (!emailError && !passError) { // Initiate the sign-in process if the entered information is valid
                signInCall(email, password)
            }
        }) {
            Text(text = "Login")  // Display the login button
        }

        Spacer(modifier = Modifier.height(32.dp))  // Add some spacing

        Text(text = "Forgot Password?", modifier = Modifier.clickable {
//            Unit//
        })  // Display the forgot password text

        Spacer(modifier = Modifier.height(32.dp))  // Add some spacing

        Text(text = "Don't have an account? Sign up", modifier = Modifier.clickable {
                navigateToRegister()
        })  // Display the sign up text

        Spacer(modifier = Modifier.height(32.dp))  // Add some spacing

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image( // Display the Google login button
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Gmail",
                modifier = Modifier
                    .size(60.dp)
                    .clickable {
                        // google clicked
                    }
            )
        }
        if (showDialog) { // Show the error dialog if it is needed
            ShowDialog(
                title = title,
                message = message,
                answer = answer,
                onDismiss = { showDialog = false }
            )
        }

        when (signInUiState){ // Display the appropriate screen based on the sign-in state
            is SignInUiState.Loading -> LoadingScreen()
            is SignInUiState.Success -> navigateToDashBoard()
            is SignInUiState.Error -> {
                ErrorScreen(signInUiState.errorMessage)
            }
        }
    }
}

// A Composable function to display a loading screen with the text "Loading".
// @param modifier The modifier to apply to the loading screen.
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Text("",modifier = modifier)
}

// Function to display an error screen with the provided error message and optional modifier.
@Composable
fun ErrorScreen(errorMessage: String, modifier: Modifier = Modifier) {
    Log.i("Error", errorMessage)
    Text(
        text = errorMessage,
        modifier = modifier
    )
}
