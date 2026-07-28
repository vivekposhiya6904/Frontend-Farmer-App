package com.example.farmhelper.ui.auth.models

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthStateManager {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun setAuthenticated() {
        _authState.value = AuthState.Authenticated
    }

    fun setUnauthenticated() {
        _authState.value = AuthState.Unauthenticated
    }

    fun setSessionExpired() {
        _authState.value = AuthState.SessionExpired
    }

    fun setLoading() {
        _authState.value = AuthState.Loading
    }
}
