package com.example.farmhelper.ui.weather.models

import com.example.farmhelper.ui.home.models.WeatherAlert

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val data: WeatherDashboardData) : WeatherState()
    data class Error(val message: String, val lastCachedData: WeatherDashboardData? = null) : WeatherState()
}

data class WeatherDashboardData(
    val locationName: String,
    val currentTemp: Double,
    val feelsLike: Double,
    val condition: String,
    val conditionIcon: String,
    val humidity: Int,
    val windKph: Double,
    val windDir: String = "NW",
    val pressureHpa: Double,
    val visibilityKm: Double,
    val uvIndex: Double,
    val sunrise: String,
    val sunset: String,
    val moonPhase: String,
    val hourlyForecast: List<HourlyPoint>,
    val alerts: List<WeatherAlert>,
    val sevenDayForecast: List<WeeklyDayForecast>,
    val cropAdvisories: List<CropAdvisory>,
    val isFromCache: Boolean = false,
    val lastUpdatedText: String = "",
    val weatherSummary: String = "",
    val nextRainTime: String? = null
)

data class HourlyPoint(
    val time: String,
    val conditionText: String,
    val conditionIcon: String,
    val temp: String
)

data class WeeklyDayForecast(
    val date: String,
    val day: String,
    val conditionText: String,
    val conditionIcon: String,
    val tempRange: String,
    val rainProb: String,
    val farmingAdvisoryRes: Int,
    val windowBadgeText: String? = null,
    val windowBadgeColorType: String? = null // "best" or "risk"
)

data class CropAdvisory(
    val cropNameRes: Int? = null,
    val cropNameText: String? = null,
    val irrigationRes: Int? = null,
    val harvestRes: Int? = null,
    val pesticideRes: Int? = null,
    val titleText: String? = null,
    val descriptionText: String? = null
)


// API Network Models
data class WeatherLiveResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherLiveData?
)

data class WeatherLiveData(
    val location: String,
    val temperature: Double,
    val humidity: Int,
    val wind_kph: Double,
    val wind_dir: String? = "NW",
    val condition: String,
    val icon: String,
    val feels_like: Double,
    val uv_index: Double,
    val is_mock: Boolean,
    val pressure_hpa: Double? = 1013.0,
    val visibility_km: Double? = 10.0
)

data class WeatherForecastResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherForecastData?
)

data class WeatherForecastData(
    val location: String,
    val forecast: List<ForecastDay>,
    val is_tracked: Boolean,
    val last_updated: String? = null,
    val cached_at: String? = null,
    val weather_summary: String? = null
)

data class ForecastDay(
    val location: String,
    val forecast_date: String,
    val max_temp_c: Double,
    val min_temp_c: Double,
    val avg_temp_c: Double,
    val chance_of_rain: Int,
    val humidity: Int,
    val wind_kph: Double,
    val condition: String,
    val icon: String,
    val sunrise: String? = null,
    val sunset: String? = null,
    val uv_index: Double? = null,
    val moon_phase: String? = "Waxing Gibbous",
    val pressure_hpa: Double? = 1012.0,
    val visibility_km: Double? = 10.0
)

data class WeatherAlertsResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherAlertsData?
)

data class WeatherAlertsData(
    val location: String,
    val alerts: List<BackendWeatherAlert>,
    val count: Int,
    val has_pending: Boolean
)

data class BackendWeatherAlert(
    val id: String? = null,
    val title: String,
    val description: String,
    val severity: String? = null,
    val date: String? = null
)

// Timeline API Models
data class WeatherTimelineResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherTimelineData?
)

data class WeatherTimelineData(
    val location: String,
    val forecast_date: String? = null,
    val timeline: Map<String, PeriodDetail>? = null
)

data class PeriodDetail(
    val temp: Double? = 30.0,
    val condition: String? = "Sunny",
    val icon: String? = "113",
    val rain: Int? = 0
)

// Recommendations API Models
data class WeatherRecommendationsResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherRecommendationsData?
)

data class WeatherRecommendationsData(
    val location: String,
    val recommendations: List<BackendRecommendation>? = emptyList(),
    val count: Int? = 0
)

data class BackendRecommendation(
    val crop_type: String? = null,
    val category: String? = null,
    val recommendation: String? = null,
    val title: String? = null,
    val priority: String? = "medium",
    val severity: String? = "info"
)

// Crop Insights API Models
data class WeatherCropInsightsResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherCropInsightsData?
)

data class WeatherCropInsightsData(
    val location: String? = null,
    val current_weather: WeatherLiveData? = null,
    val crop_advisories: List<BackendCropAdvisoryItem>? = emptyList(),
    val alerts: List<BackendWeatherAlert>? = emptyList(),
    val recommended_actions: List<BackendRecommendation>? = emptyList()
)

data class BackendCropAdvisoryItem(
    val crop_type: String? = null,
    val crop_name: String? = null,
    val title: String? = null,
    val description: String? = null,
    val severity: String? = "info",
    val rule_id: String? = null
)

// Dashboard API Models
data class WeatherDashboardResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherDashboardDataPayload?
)

data class WeatherDashboardDataPayload(
    val current_weather: WeatherLiveData? = null,
    val forecast: List<ForecastDay>? = emptyList(),
    val today_alerts: List<BackendWeatherAlert>? = emptyList(),
    val today_advisories: List<BackendRecommendation>? = emptyList(),
    val weather_summary: String? = null,
    val next_rain_time: String? = null,
    val sunrise: String? = null,
    val sunset: String? = null
)

// User Crops DTO Models
data class UserCropCreateRequest(
    val crop_name: String,
    val season: String,
    val area: Double,
    val village: String,
    val district: String,
    val growth_stage: String = "any"
)

data class UserCropUpdateRequest(
    val crop_name: String? = null,
    val season: String? = null,
    val area: Double? = null,
    val village: String? = null,
    val district: String? = null,
    val growth_stage: String? = null
)

data class UserCropItem(
    val crop_id: String? = null,
    val crop_name: String,
    val season: String,
    val area: Double,
    val village: String,
    val district: String,
    val growth_stage: String? = "any"
)

data class UserCropsResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

// Saved Locations DTO Models
data class SavedLocationCreateRequest(
    val location_name: String,
    val latitude: Double,
    val longitude: Double,
    val district: String,
    val state: String = "Gujarat",
    val country: String = "India",
    val nickname: String,
    val is_default: Boolean = false
)

data class SavedLocationUpdateRequest(
    val location_name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val district: String? = null,
    val state: String? = null,
    val country: String? = null,
    val nickname: String? = null,
    val is_default: Boolean? = null
)

data class SavedLocationItem(
    val location_id: String? = null,
    val location_name: String,
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val district: String? = "",
    val state: String? = "Gujarat",
    val country: String? = "India",
    val nickname: String? = "",
    val is_default: Boolean = false
)

data class SavedLocationsResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

// Preferences DTO Models
data class GPSCoordinatesDto(
    val latitude: Double,
    val longitude: Double
)

data class WeatherPreferencesCreateRequest(
    val preferred_language: String = "en",
    val default_location: String,
    val district: String,
    val village: String? = null,
    val gps_coordinates: GPSCoordinatesDto,
    val temperature_unit: String = "C",
    val notification_enabled: Boolean = true,
    val preferred_notification_time: String = "08:00"
)

data class WeatherPreferencesUpdateRequest(
    val preferred_language: String? = null,
    val default_location: String? = null,
    val district: String? = null,
    val village: String? = null,
    val gps_coordinates: GPSCoordinatesDto? = null,
    val temperature_unit: String? = null,
    val notification_enabled: Boolean? = null,
    val preferred_notification_time: String? = null
)

data class WeatherPreferencesData(
    val user_id: String? = null,
    val preferred_language: String? = "en",
    val default_location: String? = "",
    val district: String? = "",
    val village: String? = null,
    val gps_coordinates: GPSCoordinatesDto? = null,
    val temperature_unit: String? = "C",
    val notification_enabled: Boolean? = true,
    val preferred_notification_time: String? = "08:00"
)

data class WeatherPreferencesResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherPreferencesData? = null
)

// Notification History DTO Models
data class NotificationHistoryItem(
    val id: String? = null,
    val notification_id: String? = null,
    val title: String,
    val description: String,
    val severity: String? = "info",
    val date: String? = null,
    val created_time: String? = null
)

data class NotificationHistoryResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

// Agri Weather Insights DTO Models
data class WeatherInsightsResponse(
    val success: Boolean,
    val message: String,
    val data: WeatherInsightsDataPayload? = null
)

data class WeatherInsightsDataPayload(
    val location: String? = null,
    val best_farming_days: List<FarmingWindowItem>? = emptyList(),
    val high_risk_days: List<FarmingWindowItem>? = emptyList(),
    val next_rain_time: String? = null
)

data class FarmingWindowItem(
    val date: String,
    val reason: String? = null,
    val activity: String? = null,
    val risk_level: String? = "low"
)
