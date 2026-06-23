package com.example.farmhelper.ui.auth.models

class RegisterResponse(
    val message: String,
    val data: RegisterData
)

data class RegisterData(
    val user_id: String,
)