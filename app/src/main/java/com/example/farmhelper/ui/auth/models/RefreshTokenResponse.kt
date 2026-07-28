package com.example.farmhelper.ui.auth.models

data class RefreshTokenResponse(
    val access_token: String,
    val token_type: String,
    val refresh_token: String? = null,
    val expires_in: Int? = null,
    val user: User? = null
)
