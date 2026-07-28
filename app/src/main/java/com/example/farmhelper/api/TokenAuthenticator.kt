package com.example.farmhelper.api

import com.example.farmhelper.session.SessionManager
import com.example.farmhelper.ui.auth.models.AuthStateManager
import com.example.farmhelper.ui.auth.models.RefreshTokenRequest
import com.example.farmhelper.ui.auth.models.RefreshTokenResponse
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(private val sessionManager: SessionManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        android.util.Log.d("TokenAuthenticator", "authenticate: Triggered for url=${response.request.url.encodedPath}")
        // Only retry if we haven't retried this request already
        if (response.priorResponse != null) {
            android.util.Log.w("TokenAuthenticator", "authenticate: already retried this request once, returning null")
            return null
        }

        val refreshToken = sessionManager.getRefreshTokenSync()
        android.util.Log.d("TokenAuthenticator", "authenticate: refresh token=$refreshToken")
        if (refreshToken.isNullOrEmpty()) {
            android.util.Log.w("TokenAuthenticator", "authenticate: refresh token is null/empty (user already logged out). Returning null.")
            return null
        }

        // Clean Retrofit instance to call refresh without interceptors
        val cleanRetrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = cleanRetrofit.create(ApiServices::class.java)

        return try {
            android.util.Log.d("TokenAuthenticator", "authenticate: sending refresh token API call")
            val refreshResponse = runBlocking {
                service.refreshToken(RefreshTokenRequest(refreshToken))
            }
            android.util.Log.d("TokenAuthenticator", "authenticate: refresh API call finished with code=${refreshResponse.code()}")

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val refreshData = refreshResponse.body()!!
                android.util.Log.d("TokenAuthenticator", "authenticate: refresh successful, updating access token")
                runBlocking {
                    sessionManager.updateAccessToken(refreshData.access_token)
                }

                // Retry original request with new access token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshData.access_token}")
                    .build()
            } else {
                // Refresh failed (refresh token expired/invalid)
                android.util.Log.w("TokenAuthenticator", "authenticate: refresh API call failed, triggering session expiry")
                triggerSessionExpiry()
                null
            }
        } catch (e: Exception) {
            // Network connection errors or other issues - let it fail without logging out immediately
            android.util.Log.e("TokenAuthenticator", "authenticate: caught exception during token refresh", e)
            null
        }
    }

    private fun triggerSessionExpiry() {
        runBlocking {
            sessionManager.logout()
        }
        AuthStateManager.setSessionExpired()
    }
}
