package com.example.bikefm2.data

import com.example.bikefm2.data.model.User

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: User? = null,
    val error: String? = null
)