    package com.example.farmhelper.utils

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NoInternetException(message: String = "No internet connection. Please check your network settings.") : IOException(message)

class ServerException(message: String = "Server error occurred. Please try again later.") : IOException(message)

class SessionExpiredException(message: String = "Your session has expired. Please log in again.") : IOException(message)

object ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is NoInternetException -> throwable.message ?: "No internet connection."
            is ServerException -> throwable.message ?: "Server error."
            is SessionExpiredException -> throwable.message ?: "Session expired."
            is UnknownHostException, is ConnectException -> 
                "Cannot connect to the server. Please verify your connection."
            is SocketTimeoutException -> 
                "Request timed out. Please try again."
            else -> throwable.message ?: "An unexpected error occurred."
        }
    }

    fun <T> parseErrorResponse(response: retrofit2.Response<T>): String {
        val errorBody = response.errorBody()?.string()
        if (errorBody.isNullOrEmpty()) {
            return "Server error (Code: ${response.code()})"
        }
        return try {
            val jsonObject = org.json.JSONObject(errorBody)
            jsonObject.optString("detail", jsonObject.optString("message", "Server error occurred"))
        } catch (e: Exception) {
            "Server error (Code: ${response.code()})"
        }
    }
}
