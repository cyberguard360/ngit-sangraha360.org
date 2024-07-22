package com.example.sg360.signinsignup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sg360.R


/**
 * A Composable function that displays a verification screen.
 *
 * @param signInUiState The current state of the sign-in process.
 * @param signInCall A function that signs in the user.
 */
@Composable
fun VerificationScreen(
    signInUiState: SignInUiState,
    signInCall: ( otpText: String) -> Unit
) {
    var otpText by remember {
        mutableStateOf("")
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Pass parameters to the OtpTextField
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = R.drawable.a), contentDescription = "Hero image",
                modifier = Modifier.size(200.dp))

            Spacer(modifier = Modifier.height(60.dp))

            Text(text = " Verification ", fontSize = 36.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Verify your account",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            BasicTextField(value = otpText, onValueChange = {
                if (it.length <=4) {
                    otpText = it
                }
            }) {
                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(4) { index ->
                        val number = when {
                            index >= otpText.length -> ""
                            else -> otpText[index]
                        }

                        Column (
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text ( text = number.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 20.sp)

                            Box(modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(Color.Black))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(onClick = {
                signInCall(otpText)
            }) {
                Text(text = "Verify", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Didn't receive the otp? Resend it", fontSize= 16.sp, modifier = Modifier.clickable {
                // resend otp
            })

            when (signInUiState){
                is SignInUiState.Loading -> LoadingScreen()
                is SignInUiState.Success -> Unit
                is SignInUiState.Error -> {
                    ErrorScreen(signInUiState.errorMessage)
                }
            }
        }
    }
}