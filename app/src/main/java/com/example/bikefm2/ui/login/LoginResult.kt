package com.example.bikefm2.ui.login

import com.example.bikefm2.data.model.LoggedInUser

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUser? = null,
    val error: String? = null
)