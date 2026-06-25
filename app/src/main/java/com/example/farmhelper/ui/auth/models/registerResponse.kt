package com.example.farmhelper.ui.auth.models

class RegisterResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expire_in: Int,
    val data: User
)

data class User(
    val id: String,
    val full_name: String,
    val email: String,
    val mobile: String,
    val is_active: Boolean
)