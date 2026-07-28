package com.example.farmhelper.api

import com.example.farmhelper.utils.NoInternetException
import com.example.farmhelper.utils.ServerException
import com.example.farmhelper.utils.SessionExpiredException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorHandler {

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is NoInternetException -> {
                throwable.message ?: "No internet connection. Please check your network and try again."
            }
            is ServerException -> {
                throwable.message ?: "Server error occurred. Please try again later."
            }
            is SessionExpiredException -> {
                throwable.message ?: "Your session has expired. Please log in again."
            }
            is UnknownHostException, is ConnectException -> {
                "No internet connection. Please check your network and try again."
            }
            is SocketTimeoutException -> {
                "Connection timed out. Please try again later."
            }
            is IOException -> {
                "Network error occurred. Please try again."
            }
            else -> {
                throwable.localizedMessage ?: "An unknown error occurred"
            }
        }
    }

    fun <T> parseErrorResponse(response: Response<T>): String {
        val errorBody = response.errorBody()?.string()
        if (errorBody.isNullOrEmpty()) {
            return "An unknown server error occurred (Status: ${response.code()})"
        }
        return try {
            val jsonObject = JSONObject(errorBody)
            jsonObject.optString("detail", jsonObject.optString("message", "Server error occurred"))
        } catch (e: Exception) {
            "Server error occurred (Status: ${response.code()})"
        }
    }
}
