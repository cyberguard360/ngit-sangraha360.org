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

@Composable
fun RegisterScreen(
    navigateToLogin: () -> Unit,
    navigateToVerify: () -> Unit,
    signInUiState: SignInUiState,
    signInCall: (email: String, username:String, password: String, confirmPass: String, tc: String ) -> Unit
){

    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var password2 by remember { mutableStateOf("") }

    var username by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }

    var passError by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }

    var answer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.a), contentDescription = "Login image",
            modifier = Modifier.size(160.dp))

        Text(text = "Register Here", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "Register your account")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = {
            email = it
            emailError = false
        }, label = {
            Text(text = "Email address")
        }, supportingText = {
            Text(text = "*required")
        }, keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ), singleLine= true,
            isError = emailError)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = username, onValueChange = {
            username = it
        }, label = {
            Text(text = "Username")
        }, keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ), singleLine= true)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password, onValueChange = {
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
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password2, onValueChange = {
            password2 = it
            passError = false
        }, label = {
            Text(text = "Confirm Password")
        }, supportingText = {
            Text(text = "*required")
        }, keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ), singleLine= true,
            isError = password != password2,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (!email.contains("@gmail.com")) {
                // Set showDialog to true to show the error dialog
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
            if (!emailError && !passError) {
                // No errors, navigate to the desired route
                if (username.isEmpty()) {
                    username = email.split('@').getOrElse(0) { "" }
                }
                // Proceed with registration
                signInCall(email, username, password, password2, "True")
            }
        }) {
            Text(text = "Register")
        }

        if (showDialog) {
            ShowDialog(
                title = title,
                message = message,
                answer = answer,
                onDismiss = { showDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
