package com.example.farmhelper.api

import android.util.Log
import com.example.farmhelper.utils.NoInternetException
import com.example.farmhelper.utils.ServerException
import com.example.farmhelper.utils.SessionExpiredException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorHandler {

    private const val TAG = "NetworkErrorHandler"

    // Friendly User-Facing Messages
    const val MSG_NO_INTERNET = "No internet connection. Please check your network."
    const val MSG_SERVER_WAKING = "Starting server... Please wait a moment."
    const val MSG_TIMEOUT = "The server is taking longer than expected."
    const val MSG_UNAUTHORIZED = "Your session has expired. Please login again."
    const val MSG_FORBIDDEN = "You do not have permission."
    const val MSG_NOT_FOUND = "Requested information was not found."
    const val MSG_CONFLICT = "This account already exists."
    const val MSG_UNPROCESSABLE = "Please check the entered information."
    const val MSG_SERVER_ERROR = "Something went wrong on our side."
    const val MSG_UNKNOWN = "Something went wrong. Please try again."

    /**
     * Map any Throwable to a user-friendly error message.
     * Technical details are logged to Logcat only.
     */
    fun getErrorMessage(throwable: Throwable): String {
        Log.e(TAG, "Network exception captured: ${throwable.javaClass.simpleName} - ${throwable.message}", throwable)

        return when (throwable) {
            is NoInternetException, is UnknownHostException, is ConnectException -> {
                MSG_NO_INTERNET
            }
            is SocketTimeoutException -> {
                MSG_TIMEOUT
            }
            is SessionExpiredException -> {
                MSG_UNAUTHORIZED
            }
            is ServerException -> {
                MSG_SERVER_ERROR
            }
            is IOException -> {
                // Catch any generic IO/gateway timeout issue from Render cold start
                if (throwable.message?.contains("wake up", ignoreCase = true) == true) {
                    MSG_SERVER_WAKING
                } else {
                    MSG_NO_INTERNET
                }
            }
            else -> {
                MSG_UNKNOWN
            }
        }
    }

    /**
     * Parse Retrofit Response and map status codes into clean user-friendly messages.
     * Technical backend payload is logged to Logcat and never shown to the user.
     */
    fun <T> parseErrorResponse(response: Response<T>): String {
        val code = response.code()
        val errorBodyString = try {
            response.errorBody()?.string()
        } catch (e: Exception) {
            "Unable to read error body: ${e.message}"
        }

        Log.e(TAG, "HTTP Error response code=$code, body=$errorBodyString")

        return when (code) {
            401 -> MSG_UNAUTHORIZED
            403 -> MSG_FORBIDDEN
            404 -> MSG_NOT_FOUND
            409 -> MSG_CONFLICT
            422 -> MSG_UNPROCESSABLE
            500 -> MSG_SERVER_ERROR
            502, 503, 504 -> MSG_SERVER_WAKING
            else -> MSG_UNKNOWN
        }
    }

    /**
     * Build a complete NetworkResult.Error from an HTTP Response or Throwable
     */
    fun <T> handleResponseError(response: Response<T>): NetworkResult.Error {
        val code = response.code()
        val friendlyMsg = parseErrorResponse(response)
        val isWaking = code in listOf(502, 503, 504)
        return NetworkResult.Error(message = friendlyMsg, code = code, isServerWaking = isWaking)
    }

    fun handleThrowable(throwable: Throwable): NetworkResult.Error {
        val friendlyMsg = getErrorMessage(throwable)
        val isWaking = throwable is SocketTimeoutException || 
                       (throwable is IOException && throwable.message?.contains("wake up", ignoreCase = true) == true)
        return NetworkResult.Error(message = friendlyMsg, isServerWaking = isWaking)
    }
}
