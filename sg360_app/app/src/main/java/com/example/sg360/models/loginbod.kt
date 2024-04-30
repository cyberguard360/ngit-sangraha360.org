package com.example.sg360.models

/**
 * A data class representing the body of a login request.
 *
 * @property email The email of the user.
 * @property password The password of the user.
 */
data class loginbod(
    // The email of the user.
    val email: String,
    // The password of the user.
    val password: String
)
