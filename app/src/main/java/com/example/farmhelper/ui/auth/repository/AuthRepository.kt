package com.example.farmhelper.ui.auth.repository

import com.example.farmhelper.api.RetrofitClient
import com.example.farmhelper.ui.auth.models.LoginRequest
import com.example.farmhelper.ui.auth.models.LoginResponse
import com.example.farmhelper.ui.auth.models.RegisterRequest
import com.example.farmhelper.ui.auth.models.RegisterResponse
import retrofit2.Response

class AuthRepository {

    suspend fun registerUser(
        request: RegisterRequest
    ): Response<RegisterResponse> {

        return RetrofitClient
            .apiServices
            .registerUser(request)

    }

    suspend fun loginUser(
        request: LoginRequest
    ): Response<LoginResponse> {

        return RetrofitClient
            .apiServices
            .loginUser(request)

    }

}