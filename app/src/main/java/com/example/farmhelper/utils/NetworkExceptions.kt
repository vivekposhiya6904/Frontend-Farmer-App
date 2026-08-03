package com.example.farmhelper.utils

import com.example.farmhelper.api.NetworkErrorHandler
import java.io.IOException

class NoInternetException(message: String = "No internet connection. Please check your network settings.") : IOException(message)

class ServerException(message: String = "Server error occurred. Please try again later.") : IOException(message)

class SessionExpiredException(message: String = "Your session has expired. Please log in again.") : IOException(message)

object ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String {
        return NetworkErrorHandler.getErrorMessage(throwable)
    }

    fun <T> parseErrorResponse(response: retrofit2.Response<T>): String {
        return NetworkErrorHandler.parseErrorResponse(response)
    }
}
