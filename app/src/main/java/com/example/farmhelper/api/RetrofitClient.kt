package com.example.farmhelper.api

import android.content.Context
import com.example.farmhelper.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.28.61.155:8000/"
    private lateinit var sessionManager: SessionManager

    @Volatile
    private var _apiServices: ApiServices? = null

    val apiServices: ApiServices
        get() = _apiServices ?: synchronized(this) {
            _apiServices ?: createDefaultServices()
        }

    private fun createDefaultServices(): ApiServices {
        val client = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val services = client.create(ApiServices::class.java)
        _apiServices = services
        return services
    }

    fun initialize(context: Context) {
        synchronized(this) {
            sessionManager = SessionManager(context.applicationContext)

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(sessionManager))
                .authenticator(TokenAuthenticator(sessionManager))
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            _apiServices = retrofit.create(ApiServices::class.java)
        }
    }
}