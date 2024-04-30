package com.example.sg360.models

/**
 * Data class representing the body of a registration request.
 *
 * @property email The email of the user.
 * @property name The name of the user.
 * @property password The password of the user.
 * @property password2 The second password of the user, to validate it.
 * @property tc The terms and conditions accepted by the user.
 */
data class RegisterBod(
    val email: String, // The email of the user.
    val name: String,   // The name of the user.
    val password: String, // The password of the user.
    val password2: String, // The second password of the user, to validate it.
    val tc: String // The terms and conditions accepted by the user.
)
