package com.example.farmhelper.api

import android.content.Context
import com.example.farmhelper.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://farmer-backend-72ms.onrender.com/"
    
    @Volatile
    private var sessionManager: SessionManager? = null

    @Volatile
    private var _apiServices: ApiServices? = null

    val apiServices: ApiServices
        get() = _apiServices ?: synchronized(this) {
            _apiServices ?: createServices(null)
        }

    fun initialize(context: Context) {
        synchronized(this) {
            val appContext = context.applicationContext
            if (sessionManager == null) {
                sessionManager = SessionManager(appContext)
            }
            if (_apiServices == null) {
                _apiServices = createServices(appContext)
            }
        }
    }

    private fun createServices(context: Context?): ApiServices {
        val sm = sessionManager ?: if (context != null) {
            SessionManager(context.applicationContext).also { sessionManager = it }
        } else null

        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(RenderColdStartInterceptor())

        if (sm != null) {
            okHttpClientBuilder
                .addInterceptor(AuthInterceptor(sm))
                .authenticator(TokenAuthenticator(sm))
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val services = retrofit.create(ApiServices::class.java)
        _apiServices = services
        return services
    }
}