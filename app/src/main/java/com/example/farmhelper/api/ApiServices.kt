package com.example.farmhelper.api

import com.example.farmhelper.ui.auth.models.LoginRequest
import com.example.farmhelper.ui.auth.models.LoginResponse
import com.example.farmhelper.ui.auth.models.RegisterRequest
import com.example.farmhelper.ui.auth.models.RegisterResponse
import com.example.farmhelper.ui.auth.models.RefreshTokenRequest
import com.example.farmhelper.ui.auth.models.RefreshTokenResponse
import com.example.farmhelper.ui.weather.models.WeatherLiveResponse
import com.example.farmhelper.ui.weather.models.WeatherForecastResponse
import com.example.farmhelper.ui.weather.models.WeatherAlertsResponse

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.farmhelper.ui.weather.models.*

interface ApiServices {

    @POST("api/auth/register")
    suspend fun registerUser(
        @retrofit2.http.Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun loginUser(
        @retrofit2.http.Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @retrofit2.http.Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @GET("api/weather/live/{location}")
    suspend fun getCurrentWeather(
        @Path("location") location: String,
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherLiveResponse>

    @GET("api/weather/forecast/{location}")
    suspend fun getWeatherForecast(
        @Path("location") location: String,
        @Query("days") days: Int = 7,
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherForecastResponse>

    @GET("api/weather/alerts/{location}")
    suspend fun getWeatherAlerts(
        @Path("location") location: String,
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherAlertsResponse>

    @GET("api/weather/timeline")
    suspend fun getWeatherTimeline(
        @Query("location") location: String,
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherTimelineResponse>

    @GET("api/weather/recommendations")
    suspend fun getFarmingRecommendations(
        @Query("location") location: String,
        @Query("crop_type") cropType: String? = null,
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherRecommendationsResponse>

    @GET("api/weather/crop-insights")
    suspend fun getWeatherCropInsights(
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherCropInsightsResponse>

    @GET("api/weather/dashboard")
    suspend fun getPersonalizedDashboard(
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherDashboardResponse>

    @GET("api/weather/insights")
    suspend fun getWeatherInsights(
        @Query("location") location: String,
        @Header("Accept-Language") acceptLanguage: String? = null
    ): Response<WeatherInsightsResponse>

    // User Crops CRUD
    @POST("api/weather/crops")
    suspend fun addUserCrop(
        @Body request: UserCropCreateRequest
    ): Response<UserCropsResponse>

    @GET("api/weather/crops")
    suspend fun getUserCrops(): Response<UserCropsResponse>

    @PUT("api/weather/crops/{crop_id}")
    suspend fun updateUserCrop(
        @Path("crop_id") cropId: String,
        @Body request: UserCropUpdateRequest
    ): Response<UserCropsResponse>

    @DELETE("api/weather/crops/{crop_id}")
    suspend fun deleteUserCrop(
        @Path("crop_id") cropId: String
    ): Response<UserCropsResponse>

    // Saved Locations CRUD
    @POST("api/weather/locations")
    suspend fun addSavedLocation(
        @Body request: SavedLocationCreateRequest
    ): Response<SavedLocationsResponse>

    @GET("api/weather/locations")
    suspend fun getSavedLocations(): Response<SavedLocationsResponse>

    @PUT("api/weather/locations/{location_id}")
    suspend fun updateSavedLocation(
        @Path("location_id") locationId: String,
        @Body request: SavedLocationUpdateRequest
    ): Response<SavedLocationsResponse>

    @DELETE("api/weather/locations/{location_id}")
    suspend fun deleteSavedLocation(
        @Path("location_id") locationId: String
    ): Response<SavedLocationsResponse>

    @POST("api/weather/locations/{location_id}/default")
    suspend fun setDefaultLocation(
        @Path("location_id") locationId: String
    ): Response<SavedLocationsResponse>

    // Preferences CRUD
    @POST("api/weather/preferences")
    suspend fun createPreferences(
        @Body request: WeatherPreferencesCreateRequest
    ): Response<WeatherPreferencesResponse>

    @GET("api/weather/preferences")
    suspend fun getPreferences(): Response<WeatherPreferencesResponse>

    @PUT("api/weather/preferences")
    suspend fun updatePreferences(
        @Body request: WeatherPreferencesUpdateRequest
    ): Response<WeatherPreferencesResponse>

    // Notification History
    @GET("api/weather/notifications")
    suspend fun getNotificationHistory(): Response<NotificationHistoryResponse>

    @DELETE("api/weather/notifications/{notification_id}")
    suspend fun deleteNotificationHistoryItem(
        @Path("notification_id") notificationId: String
    ): Response<NotificationHistoryResponse>

    @GET("api/crops/districts")
    suspend fun getDistricts(): Response<com.example.farmhelper.ui.market.models.DistrictResponse>

    @GET("api/crops/commodities")
    suspend fun getCommodities(): Response<com.example.farmhelper.ui.market.models.CommodityResponse>

    @GET("api/crops/markets/{district}")
    suspend fun getMarketsByDistrict(
        @Path("district") district: String,
        @Query("commodity") commodity: String? = null
    ): Response<com.example.farmhelper.ui.market.models.MarketResponse>

    @GET("api/crops/search")
    suspend fun searchCropPrices(
        @Query("district") district: String? = null,
        @Query("commodity") commodity: String? = null,
        @Query("market") market: String? = null,
        @Query("variety") variety: String? = null,
        @Query("date") date: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<com.example.farmhelper.ui.market.models.CropPriceResponse>

    @GET("api/crops/history")
    suspend fun getPriceHistory(
        @Query("district") district: String,
        @Query("commodity") commodity: String,
        @Query("days") days: Int = 30
    ): Response<com.example.farmhelper.ui.market.models.PriceHistoryResponse>

    @GET("api/crops/latest")
    suspend fun getLatestPrices(
        @Query("district") district: String? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.example.farmhelper.ui.market.models.LatestPriceResponse>

    @GET("api/crops/insights")
    suspend fun getCropInsights(
        @Query("commodity") commodity: String,
        @Query("district") district: String
    ): Response<com.example.farmhelper.ui.market.models.CropInsightsResponse>

    @GET("api/crops/top-markets")
    suspend fun getTopMarkets(
        @Query("commodity") commodity: String,
        @Query("district") district: String? = null
    ): Response<com.example.farmhelper.ui.market.models.TopMarketsResponse>

    @GET("api/crops/varieties")
    suspend fun getVarieties(
        @Query("commodity") commodity: String? = null,
        @Query("district") district: String? = null
    ): Response<com.example.farmhelper.ui.market.models.VarietiesResponse>

    // Price Threshold Subscriptions
    @POST("api/crops/subscriptions")
    suspend fun createSubscription(
        @Body request: com.example.farmhelper.ui.market.models.SubscriptionRequest
    ): Response<com.example.farmhelper.ui.market.models.SubscriptionResponse>

    @GET("api/crops/subscriptions")
    suspend fun getSubscriptions(): Response<com.example.farmhelper.ui.market.models.SubscriptionsListResponse>

    @DELETE("api/crops/subscriptions/{sub_id}")
    suspend fun deleteSubscription(
        @Path("sub_id") subId: String
    ): Response<com.example.farmhelper.ui.market.models.SubscriptionResponse>

    // Favorite Watchlist Crops
    @POST("api/crops/favorites")
    suspend fun addFavorite(
        @Body request: com.example.farmhelper.ui.market.models.FavoriteRequest
    ): Response<com.example.farmhelper.ui.market.models.FavoriteResponse>

    @GET("api/crops/favorites")
    suspend fun getFavorites(): Response<com.example.farmhelper.ui.market.models.FavoritesListResponse>

    @DELETE("api/crops/favorites/{commodity}")
    suspend fun deleteFavorite(
        @Path("commodity") commodity: String
    ): Response<com.example.farmhelper.ui.market.models.FavoriteResponse>

    // Price Alerts
    @GET("api/crops/alerts")
    suspend fun getCropAlerts(
        @Query("limit") limit: Int = 20,
        @Query("is_read") isRead: Boolean? = null
    ): Response<com.example.farmhelper.ui.market.models.CropAlertsListResponse>

    @POST("api/crops/alerts/{alert_id}/read")
    suspend fun markCropAlertRead(
        @Path("alert_id") alertId: String
    ): Response<com.example.farmhelper.ui.market.models.CropAlertsListResponse>
}