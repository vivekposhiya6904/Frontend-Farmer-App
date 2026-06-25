package com.example.farmhelper.ui.auth.models

data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expire_in: Int,
    val user: User
)