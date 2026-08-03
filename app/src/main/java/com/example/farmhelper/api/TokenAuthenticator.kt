package com.example.farmhelper.api

import android.util.Log
import com.example.farmhelper.session.SessionManager
import com.example.farmhelper.ui.auth.models.AuthStateManager
import com.example.farmhelper.ui.auth.models.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TokenAuthenticator(private val sessionManager: SessionManager) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "authenticate: Triggered for url=${response.request.url.encodedPath}")
        
        // Only retry if we haven't retried this request already
        if (response.priorResponse != null) {
            Log.w(TAG, "authenticate: already retried this request once, returning null")
            return null
        }

        val refreshToken = sessionManager.getRefreshTokenSync()
        Log.d(TAG, "authenticate: refresh token presence=${!refreshToken.isNullOrEmpty()}")
        if (refreshToken.isNullOrEmpty()) {
            Log.w(TAG, "authenticate: refresh token is null/empty (user already logged out). Returning null.")
            return null
        }

        val cleanOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(RenderColdStartInterceptor())
            .build()

        // Clean Retrofit instance to call refresh without AuthInterceptor
        val cleanRetrofit = Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .client(cleanOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = cleanRetrofit.create(ApiServices::class.java)

        return try {
            Log.d(TAG, "authenticate: sending refresh token API call")
            val refreshResponse = runBlocking {
                service.refreshToken(RefreshTokenRequest(refreshToken))
            }
            Log.d(TAG, "authenticate: refresh API call finished with code=${refreshResponse.code()}")

            val body = refreshResponse.body()
            if (refreshResponse.isSuccessful && body != null && !body.access_token.isNullOrEmpty()) {
                Log.d(TAG, "authenticate: refresh successful, updating access token")
                runBlocking {
                    sessionManager.updateAccessToken(body.access_token)
                }

                // Retry original request with new access token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${body.access_token}")
                    .build()
            } else {
                // Refresh failed (refresh token expired/invalid)
                Log.w(TAG, "authenticate: refresh API call failed with code=${refreshResponse.code()}, triggering session expiry")
                triggerSessionExpiry()
                null
            }
        } catch (e: Exception) {
            // Network connection errors or other issues - let it fail without logging out immediately
            Log.e(TAG, "authenticate: caught exception during token refresh", e)
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
