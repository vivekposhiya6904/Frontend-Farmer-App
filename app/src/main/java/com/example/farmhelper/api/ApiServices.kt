package com.example.farmhelper.api

import com.example.farmhelper.ui.auth.models.LoginRequest
import com.example.farmhelper.ui.auth.models.LoginResponse
import com.example.farmhelper.ui.auth.models.RegisterRequest
import com.example.farmhelper.ui.auth.models.RegisterResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServices {

    @POST("auth/register")
    suspend fun registerUser(
        @retrofit2.http.Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun loginUser(
        @retrofit2.http.Body request: LoginRequest
    ): Response<LoginResponse>

}