package com.example.farmhelper.ui.weather.repository

import retrofit2.Response
import com.example.farmhelper.api.RetrofitClient
import com.example.farmhelper.ui.weather.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class WeatherRepository {

    suspend fun fetchWeather(location: String, language: String? = null): ResponseDataResult = withContext(Dispatchers.IO) {
        val liveDeferred = async { RetrofitClient.apiServices.getCurrentWeather(location, language) }
        val forecastDeferred = async { RetrofitClient.apiServices.getWeatherForecast(location, days = 7, acceptLanguage = language) }
        val alertsDeferred = async { RetrofitClient.apiServices.getWeatherAlerts(location, language) }
        val timelineDeferred = async { 
            try { RetrofitClient.apiServices.getWeatherTimeline(location, language) } catch (e: Exception) { null }
        }
        val recommendationsDeferred = async {
            try { RetrofitClient.apiServices.getFarmingRecommendations(location, acceptLanguage = language) } catch (e: Exception) { null }
        }
        val cropInsightsDeferred = async {
            try { RetrofitClient.apiServices.getWeatherCropInsights(language) } catch (e: Exception) { null }
        }
        val insightsDeferred = async {
            try { RetrofitClient.apiServices.getWeatherInsights(location, language) } catch (e: Exception) { null }
        }

        try {
            val liveResponse = liveDeferred.await()
            val forecastResponse = forecastDeferred.await()
            val alertsResponse = alertsDeferred.await()
            val timelineResponse = timelineDeferred.await()
            val recommendationsResponse = recommendationsDeferred.await()
            val cropInsightsResponse = cropInsightsDeferred.await()
            val insightsResponse = insightsDeferred.await()

            if (liveResponse.isSuccessful && forecastResponse.isSuccessful && alertsResponse.isSuccessful) {
                ResponseDataResult.Success(
                    live = liveResponse.body()?.data,
                    forecast = forecastResponse.body()?.data,
                    alerts = alertsResponse.body()?.data,
                    timeline = if (timelineResponse?.isSuccessful == true) timelineResponse.body()?.data else null,
                    recommendations = if (recommendationsResponse?.isSuccessful == true) recommendationsResponse.body()?.data?.recommendations else null,
                    cropAdvisories = if (cropInsightsResponse?.isSuccessful == true) cropInsightsResponse.body()?.data?.crop_advisories else null,
                    insights = if (insightsResponse?.isSuccessful == true) insightsResponse.body()?.data else null
                )
            } else {
                val err = liveResponse.errorBody()?.string()
                    ?: forecastResponse.errorBody()?.string()
                    ?: alertsResponse.errorBody()?.string()
                    ?: "Error fetching weather data"
                ResponseDataResult.Error(err)
            }
        } catch (e: Exception) {
            ResponseDataResult.ExceptionError(e)
        }
    }

    // User Crops CRUD
    suspend fun getUserCrops(): Response<UserCropsResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.getUserCrops()
    }

    suspend fun addUserCrop(crop: UserCropCreateRequest): Response<UserCropsResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.addUserCrop(crop)
    }

    suspend fun deleteUserCrop(cropId: String): Response<UserCropsResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.deleteUserCrop(cropId)
    }

    // Saved Locations CRUD
    suspend fun getSavedLocations(): Response<SavedLocationsResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.getSavedLocations()
    }

    suspend fun addSavedLocation(location: SavedLocationCreateRequest): Response<SavedLocationsResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.addSavedLocation(location)
    }

    suspend fun deleteSavedLocation(locationId: String): Response<SavedLocationsResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.deleteSavedLocation(locationId)
    }

    suspend fun setDefaultLocation(locationId: String): Response<SavedLocationsResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.setDefaultLocation(locationId)
    }

    // Preferences CRUD
    suspend fun getPreferences(): Response<WeatherPreferencesResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.getPreferences()
    }

    suspend fun updatePreferences(prefs: WeatherPreferencesUpdateRequest): Response<WeatherPreferencesResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.updatePreferences(prefs)
    }

    // Notification History
    suspend fun getNotificationHistory(): Response<NotificationHistoryResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.getNotificationHistory()
    }

    suspend fun deleteNotificationHistoryItem(notificationId: String): Response<NotificationHistoryResponse> = withContext(Dispatchers.IO) {
        RetrofitClient.apiServices.deleteNotificationHistoryItem(notificationId)
    }
}

sealed class ResponseDataResult {
    data class Success(
        val live: WeatherLiveData?,
        val forecast: WeatherForecastData?,
        val alerts: WeatherAlertsData?,
        val timeline: WeatherTimelineData? = null,
        val recommendations: List<BackendRecommendation>? = null,
        val cropAdvisories: List<BackendCropAdvisoryItem>? = null,
        val insights: WeatherInsightsDataPayload? = null
    ) : ResponseDataResult()
    data class Error(val message: String) : ResponseDataResult()
    data class ExceptionError(val exception: Exception) : ResponseDataResult()
}
