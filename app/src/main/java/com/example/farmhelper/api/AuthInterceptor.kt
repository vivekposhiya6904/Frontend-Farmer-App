package com.example.farmhelper.api

import com.example.farmhelper.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        android.util.Log.d("AuthInterceptor", "intercept: Request path=$path")

        // Skip authorization header for public auth endpoints
        if (path.contains("api/auth/register") ||
            path.contains("api/auth/login") ||
            path.contains("api/auth/refresh")) {
            android.util.Log.d("AuthInterceptor", "intercept: skipping auth header for public endpoint $path")
            try {
                val res = chain.proceed(request)
                android.util.Log.d("AuthInterceptor", "intercept: proceed successful for $path with code=${res.code}")
                return res
            } catch (e: Exception) {
                android.util.Log.e("AuthInterceptor", "intercept: proceed threw exception for $path", e)
                throw e
            }
        }

        val lang = com.example.farmhelper.ui.localization.LanguageManager.currentLanguage
        val token = sessionManager.getAccessTokenSync()
        android.util.Log.d("AuthInterceptor", "intercept: retrieved token=$token, language=$lang")
        
        val builder = request.newBuilder()
        builder.header("Accept-Language", lang)
        
        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }
        
        val newRequest = builder.build()

        try {
            val res = chain.proceed(newRequest)
            android.util.Log.d("AuthInterceptor", "intercept: proceed successful for $path with code=${res.code}")
            return res
        } catch (e: Exception) {
            android.util.Log.e("AuthInterceptor", "intercept: proceed threw exception for $path", e)
            throw e
        }
    }
}
