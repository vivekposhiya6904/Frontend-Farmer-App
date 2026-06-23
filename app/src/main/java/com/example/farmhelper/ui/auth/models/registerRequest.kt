package com.example.farmhelper.ui.auth.models

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val mobile: String,
    val password: String,
    val confirm_password: String
)