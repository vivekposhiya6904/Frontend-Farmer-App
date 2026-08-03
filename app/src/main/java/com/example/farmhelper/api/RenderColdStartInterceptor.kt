package com.example.farmhelper.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Interceptor designed to handle Render free tier cold starts.
 * Render backends spin down after inactivity. Cold starts can take 30-50 seconds or return
 * temporary 502/503/504 gateway errors while spinning up.
 * This interceptor catches timeouts and gateway errors and retries requests automatically.
 */
class RenderColdStartInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 2500L
) : Interceptor {

    companion object {
        private const val TAG = "RenderColdStart"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    Log.i(TAG, "Retrying request due to potential server wakeup: attempt $attempt/${maxRetries} for ${request.url.encodedPath}")
                    try {
                        Thread.sleep(initialDelayMs * attempt)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }

                val response = chain.proceed(request)

                // Check if response is a server-waking gateway error (502, 503, 504)
                if ((response.code == 502 || response.code == 503 || response.code == 504) && attempt < maxRetries) {
                    Log.w(TAG, "Received server gateway status ${response.code} for ${request.url.encodedPath}. Server might be waking up.")
                    response.close()
                    attempt++
                    continue
                }

                return response

            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "SocketTimeoutException on attempt $attempt for ${request.url.encodedPath}: ${e.message}")
                lastException = e
                attempt++
            } catch (e: ConnectException) {
                Log.w(TAG, "ConnectException on attempt $attempt for ${request.url.encodedPath}: ${e.message}")
                lastException = e
                attempt++
            } catch (e: UnknownHostException) {
                // Network DNS or connectivity issue - no point retrying multiple times if no host found
                Log.w(TAG, "UnknownHostException for ${request.url.encodedPath}: ${e.message}")
                throw e
            } catch (e: IOException) {
                Log.w(TAG, "IOException on attempt $attempt for ${request.url.encodedPath}: ${e.message}")
                lastException = e
                attempt++
            }
        }

        throw lastException ?: IOException("Server is taking too long to wake up. Please try again.")
    }
}
