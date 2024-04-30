package com.example.sg360.models

/**
 * Represents the response of a login request.
 *
 * @property accessToken The access token of the user.
 * @property refreshToken The refresh token of the user.
 */
data class loginres(
    val access: String, // The access token of the user.
    val refresh: String // The refresh token of the user.
)
