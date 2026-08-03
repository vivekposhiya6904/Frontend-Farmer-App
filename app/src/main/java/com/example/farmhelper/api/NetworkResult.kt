package com.example.farmhelper.api

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val isServerWaking: Boolean = false
    ) : NetworkResult<Nothing>()
    
    object Loading : NetworkResult<Nothing>()
    data class ServerWaking(
        val message: String = "Starting server... Please wait while we connect."
    ) : NetworkResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
}
