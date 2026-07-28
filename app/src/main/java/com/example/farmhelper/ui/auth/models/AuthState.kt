package com.example.farmhelper.ui.auth.models

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object SessionExpired : AuthState()
}
