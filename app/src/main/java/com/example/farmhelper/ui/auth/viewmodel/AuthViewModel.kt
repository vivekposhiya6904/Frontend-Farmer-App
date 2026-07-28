package com.example.farmhelper.ui.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.session.SessionManager
import com.example.farmhelper.ui.auth.models.LoginRequest
import com.example.farmhelper.ui.auth.models.LoginResponse
import com.example.farmhelper.ui.auth.models.RegisterRequest
import com.example.farmhelper.ui.auth.models.RegisterResponse
import com.example.farmhelper.ui.auth.repository.AuthRepository
import com.example.farmhelper.ui.auth.models.AuthStateManager
import com.example.farmhelper.api.NetworkErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application
) : AndroidViewModel(application){

    private val repository = AuthRepository()

    private val sessionManager = SessionManager(getApplication())

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

                    response.body()?.let { registerResponse ->

                        sessionManager.saveSession(
                            access_token = registerResponse.access_token,
                            refresh_token = registerResponse.refresh_token,
                            token_type = registerResponse.token_type,
                            expires_in = registerResponse.expires_in,
                            user_id = registerResponse.user.id,
                            full_name = registerResponse.user.full_name,
                            email = registerResponse.user.email,
                            mobile = registerResponse.user.mobile,
                            is_active = registerResponse.user.is_active
                        )

                        AuthStateManager.setAuthenticated()
                        _registerResponse.value = registerResponse

                    }

                } else {
                    _error.value = NetworkErrorHandler.parseErrorResponse(response)
                }
            } catch (e: Exception) {
                _error.value = NetworkErrorHandler.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(
        email: String,
        password: String
    ) {
        android.util.Log.d("AuthViewModel", "loginUser: button click triggered with email=$email")

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {
                android.util.Log.d("AuthViewModel", "loginUser: calling repository.loginUser")
                val response = repository.loginUser(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )
                android.util.Log.d("AuthViewModel", "loginUser: repository call returned success=${response.isSuccessful}")

                if (response.isSuccessful) {

                    response.body()?.let { loginResponse ->
                        android.util.Log.d("AuthViewModel", "loginUser: saving session")
                        sessionManager.saveSession(
                            access_token = loginResponse.access_token,
                            refresh_token = loginResponse.refresh_token,
                            token_type = loginResponse.token_type,
                            expires_in = loginResponse.expires_in,
                            user_id = loginResponse.user.id,
                            full_name = loginResponse.user.full_name,
                            email = loginResponse.user.email,
                            mobile = loginResponse.user.mobile,
                            is_active = loginResponse.user.is_active
                        )

                        AuthStateManager.setAuthenticated()
                        _loginResponse.value = loginResponse
                        android.util.Log.d("AuthViewModel", "loginUser: authentication marked success")
                    }


                } else {
                    val errMsg = NetworkErrorHandler.parseErrorResponse(response)
                    android.util.Log.w("AuthViewModel", "loginUser: response unsuccessful: $errMsg")
                    _error.value = errMsg
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "loginUser: caught exception during execution", e)
                _error.value = NetworkErrorHandler.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }

        }

    }

}