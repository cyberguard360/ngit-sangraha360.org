package com.example.sg360.models

/**
 * A data class representing the body of a verify request.
 *
 * @property email The email of the user.
 * @property otp The OTP (One-Time Password) of the user.
 */
data class VerifyBod(
    // The email of the user.
    val email: String,
    // The OTP (One-Time Password) of the user.
    val otp: String
)
