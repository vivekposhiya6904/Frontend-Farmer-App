package com.example.farmhelper.ui.auth.viewmodel

import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.auth.models.LoginRequest
import com.example.farmhelper.ui.auth.models.LoginResponse
import com.example.farmhelper.ui.auth.models.RegisterRequest
import com.example.farmhelper.ui.auth.models.RegisterResponse
import com.example.farmhelper.ui.auth.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _registerResponse =
        MutableStateFlow<RegisterResponse?>(null)
    val registerResponse : StateFlow<RegisterResponse?> = _registerResponse

    private val _loginResponse =
        MutableStateFlow<LoginResponse?>(null)
    val loginResponse : StateFlow<LoginResponse?> = _loginResponse

    fun registerUser(
        fullName: String,
        email: String,
        mobile: String,
        password: String,
        confirmPassword: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                val response = repository.registerUser(
                    RegisterRequest(
                        full_name = fullName,
                        email = email,
                        mobile = mobile,
                        password = password,
                        confirm_password = confirmPassword
                    )
                )

                if (response.isSuccessful) {

                    _registerResponse.value =
                        response.body()

                } else {

                    _error.value =
                        response.errorBody()?.string()?: "Register failed with unknown error"

                }
            }catch (e: Exception) {

                _error.value =
                    e.message ?: "Unknown error occurred during registration"

            }finally {

                _isLoading.value = false

            }
        }
    }

    fun loginUser(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                val response = repository.loginUser(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )

                if (response.isSuccessful) {

                    _loginResponse.value =
                        response.body()
                } else {

                    _error.value =
                        response.errorBody()?.string()?: "Login failed with unknown error"
                }
            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Unknown error occurred during login"

            } finally {

                _isLoading.value = false
            }

        }

    }

}