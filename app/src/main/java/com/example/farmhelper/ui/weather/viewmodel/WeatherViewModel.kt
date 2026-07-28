package com.example.farmhelper.ui.weather.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.home.models.AlertSeverity
import com.example.farmhelper.ui.home.models.WeatherAlert
import com.example.farmhelper.ui.weather.location.AppLocationProvider
import com.example.farmhelper.ui.weather.models.*
import com.example.farmhelper.ui.weather.preferences.WeatherPreferencesManager
import com.example.farmhelper.ui.weather.repository.ResponseDataResult
import com.example.farmhelper.ui.weather.repository.WeatherRepository
import com.google.gson.Gson
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.farmhelper.R

@OptIn(FlowPreview::class)
class WeatherViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WeatherRepository()
    private val prefsManager = WeatherPreferencesManager(application)
    private val gson = Gson()

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Expanded suggestions: Villages, Cities, Districts, States, and Popular farming regions
    private val popularLocations = listOf(
        // Gujarat
        "Rajkot", "Gondal", "Junagadh", "Ahmedabad", "Morbi", "Jetpur", "Surat", "Vadodara",
        "Jamnagar", "Bhavnagar", "Bhuj", "Anand", "Mehsana", "Godhra", "Amreli", "Patan", "Bharuch",
        "Navsari", "Valsad", "Surendranagar", "Dahod", "Palanpur", "Porbandar", "Veraval", "Dwarka",
        // Maharashtra
        "Mumbai", "Pune", "Nashik", "Nagpur", "Thane", "Aurangabad", "Solapur", "Kolhapur", "Amravati",
        // North / Central / East India
        "Delhi", "Gorakhpur", "Amritsar", "Ludhiana", "Patna", "Indore", "Bhopal", "Gwalior", "Jabalpur",
        "Jaipur", "Jodhpur", "Udaipur", "Kota", "Bikaner", "Ajmer", "Alwar", "Sikar", "Sri Ganganagar",
        // States & Regions
        "Gujarat", "Maharashtra", "Rajasthan", "Punjab", "Haryana", "Madhya Pradesh", "Uttar Pradesh",
        "Saurashtra", "Kutch", "Malwa", "Vidarbha", "Marathwada", "Bundelkhand"
    )

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    val recentSearches: StateFlow<List<String>> = prefsManager.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteLocations: StateFlow<List<String>> = prefsManager.favoriteLocations
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _userCrops = MutableStateFlow<List<UserCropItem>>(emptyList())
    val userCrops: StateFlow<List<UserCropItem>> = _userCrops.asStateFlow()

    private val _savedLocations = MutableStateFlow<List<SavedLocationItem>>(emptyList())
    val savedLocations: StateFlow<List<SavedLocationItem>> = _savedLocations.asStateFlow()

    private val _temperatureUnit = MutableStateFlow("C")
    val temperatureUnit: StateFlow<String> = _temperatureUnit.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notificationHistory = MutableStateFlow<List<NotificationHistoryItem>>(emptyList())
    val notificationHistory: StateFlow<List<NotificationHistoryItem>> = _notificationHistory.asStateFlow()

    private val _isCrudLoading = MutableStateFlow(false)
    val isCrudLoading: StateFlow<Boolean> = _isCrudLoading.asStateFlow()

    private var weatherFetchJob: Job? = null
    private var currentFetchedLocation: String? = null

    fun fetchUserCrops() {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.getUserCrops()
                if (res.isSuccessful && res.body()?.success == true) {
                    val rawData = res.body()?.data
                    if (rawData is List<*>) {
                        val json = gson.toJson(rawData)
                        val items = gson.fromJson(json, Array<UserCropItem>::class.java).toList()
                        _userCrops.value = items
                    }
                }
            } catch (e: Exception) {
                // Ignore API error fallback
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun addUserCrop(crop: UserCropCreateRequest) {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.addUserCrop(crop)
                if (res.isSuccessful) {
                    fetchUserCrops()
                    currentFetchedLocation?.let { fetchWeather(it) }
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun deleteUserCrop(cropId: String) {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.deleteUserCrop(cropId)
                if (res.isSuccessful) {
                    fetchUserCrops()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun fetchSavedLocations() {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.getSavedLocations()
                if (res.isSuccessful && res.body()?.success == true) {
                    val rawData = res.body()?.data
                    if (rawData is List<*>) {
                        val json = gson.toJson(rawData)
                        val items = gson.fromJson(json, Array<SavedLocationItem>::class.java).toList()
                        _savedLocations.value = items
                    }
                }
            } catch (e: Exception) {
                // Ignore fallback
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun addSavedLocation(loc: SavedLocationCreateRequest) {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.addSavedLocation(loc)
                if (res.isSuccessful) {
                    fetchSavedLocations()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun deleteSavedLocation(locationId: String) {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.deleteSavedLocation(locationId)
                if (res.isSuccessful) {
                    fetchSavedLocations()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun setDefaultLocation(locationId: String) {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.setDefaultLocation(locationId)
                if (res.isSuccessful) {
                    fetchSavedLocations()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun updateTemperatureUnit(unit: String) {
        _temperatureUnit.value = unit
    }

    fun toggleNotificationEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun fetchNotificationHistory() {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.getNotificationHistory()
                if (res.isSuccessful && res.body()?.success == true) {
                    val rawData = res.body()?.data
                    if (rawData is List<*>) {
                        val json = gson.toJson(rawData)
                        val items = gson.fromJson(json, Array<NotificationHistoryItem>::class.java).toList()
                        _notificationHistory.value = items
                    }
                }
            } catch (e: Exception) {
                // Ignore fallback
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun deleteNotificationHistoryItem(notificationId: String) {
        viewModelScope.launch {
            _isCrudLoading.value = true
            try {
                val res = repository.deleteNotificationHistoryItem(notificationId)
                if (res.isSuccessful) {
                    fetchNotificationHistory()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isCrudLoading.value = false
            }
        }
    }

    fun syncLanguagePreference(langCode: String) {
        viewModelScope.launch {
            try {
                repository.updatePreferences(
                    WeatherPreferencesUpdateRequest(
                        preferred_language = langCode
                    )
                )
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    init {
        // Monitor query flows with a debounce and trigger search filtering
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest { query ->
                    if (query.trim().length >= 2) {
                        _suggestions.value = popularLocations.filter {
                            it.contains(query, ignoreCase = true)
                        }
                    } else {
                        _suggestions.value = emptyList()
                    }
                }
        }

        // Fetch selected location dynamically on startup and whenever preferences change
        viewModelScope.launch {
            prefsManager.lastSelectedLocation
                .distinctUntilChanged()
                .collect { location ->
                    if (location != currentFetchedLocation) {
                        currentFetchedLocation = location
                        fetchWeather(location)
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchWeather(location: String) {
        weatherFetchJob?.cancel()
        weatherFetchJob = viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            
            val currentLang = com.example.farmhelper.ui.localization.LanguageManager.currentLanguage
            val result = repository.fetchWeather(location, currentLang)
            
            when (result) {
                is ResponseDataResult.Success -> {
                    val dashboardData = mapToDashboardData(location, result)
                    
                    // Set current fetched location BEFORE saving to prefs to block the loop
                    currentFetchedLocation = dashboardData.locationName
                    
                    _weatherState.value = WeatherState.Success(dashboardData)
                    
                    // Update cache and preferences
                    prefsManager.saveLastSelectedLocation(dashboardData.locationName)
                    prefsManager.addRecentSearch(dashboardData.locationName)
                    
                    try {
                        val jsonStr = gson.toJson(dashboardData)
                        prefsManager.saveCachedWeatherData(jsonStr)
                    } catch (e: Exception) {
                        // ignore serialization errors
                    }
                }
                is ResponseDataResult.Error -> {
                    loadFromOfflineCache(result.message)
                }
                is ResponseDataResult.ExceptionError -> {
                    loadFromOfflineCache(getApplication<Application>().getString(R.string.showing_last_updated))
                }
            }
        }
    }

    private suspend fun loadFromOfflineCache(errorMessage: String) {
        val cachedJson = prefsManager.cachedWeatherData.firstOrNull()
        if (!cachedJson.isNullOrEmpty()) {
            try {
                val data = gson.fromJson(cachedJson, WeatherDashboardData::class.java)
                val offlineData = data.copy(
                    isFromCache = true
                )
                _weatherState.value = WeatherState.Error(errorMessage, offlineData)
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(errorMessage)
            }
        } else {
            _weatherState.value = WeatherState.Error(errorMessage)
        }
    }

    fun fetchWeatherWithGPS() {
        weatherFetchJob?.cancel()
        weatherFetchJob = viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            val location = AppLocationProvider.getCurrentLocation(getApplication())
            if (location != null) {
                android.util.Log.d("WeatherViewModel", "Received GPS coordinates: Lat=${location.latitude}, Lng=${location.longitude}")
                
                var resolvedCity: String? = null
                val geocoder = android.location.Geocoder(getApplication(), Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val locality = address.locality
                        val subAdmin = address.subAdminArea
                        val admin = address.adminArea
                        
                        android.util.Log.d("WeatherViewModel", "Android Geocoder returned Address: $address")
                        android.util.Log.d("WeatherViewModel", "Android Geocoder Locality: $locality, SubAdmin: $subAdmin, Admin: $admin")
                        
                        resolvedCity = when {
                            locality?.lowercase() == "satej" || subAdmin?.lowercase() == "ahmedabad" || locality?.lowercase() == "ahmedabad" -> "Ahmedabad"
                            !locality.isNullOrEmpty() -> locality
                            !subAdmin.isNullOrEmpty() -> subAdmin
                            else -> admin
                        }
                        
                        android.util.Log.d("WeatherViewModel", "Mapped Geocoder city: $resolvedCity")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WeatherViewModel", "Android Geocoder failed: ${e.message}", e)
                }

                val queryStr = resolvedCity ?: "${location.latitude},${location.longitude}"
                android.util.Log.d("WeatherViewModel", "Fetching weather with query string: $queryStr")
                fetchWeather(queryStr)
            } else {
                loadFromOfflineCache(getApplication<Application>().getString(R.string.gps_error))
            }
        }
    }

    fun toggleFavorite(location: String) {
        viewModelScope.launch {
            prefsManager.toggleFavorite(location)
        }
    }

    fun removeRecentSearch(location: String) {
        viewModelScope.launch {
            prefsManager.removeRecentSearch(location)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            prefsManager.clearRecentSearches()
        }
    }

    private fun mapToDashboardData(queryLocation: String, success: ResponseDataResult.Success): WeatherDashboardData {
        val live = success.live
        val forecast = success.forecast
        val alerts = success.alerts
        val timelineData = success.timeline?.timeline

        val resolvedName = live?.location ?: forecast?.location ?: queryLocation
        
        // Parse Alerts
        val mappedAlerts = alerts?.alerts?.map { alert ->
            val mappedSeverity = mapSeverity(alert.severity)
            if (_notificationsEnabled.value && (mappedSeverity == AlertSeverity.CRITICAL || mappedSeverity == AlertSeverity.WARNING)) {
                try {
                    com.example.farmhelper.service.WeatherNotificationManager.showNotification(
                        getApplication(),
                        alert.title,
                        alert.description
                    )
                } catch (e: Exception) {
                    // Ignore notification manager errors
                }
            }
            WeatherAlert(
                id = alert.id ?: UUID.randomUUID().toString(),
                title = alert.title,
                description = alert.description,
                severity = mappedSeverity,
                date = alert.date ?: "Active"
            )
        } ?: emptyList()

        val firstForecast = forecast?.forecast?.firstOrNull()

        // Parse Hourly Timeline dynamically using real backend Timeline API if available
        val baseTemp = live?.temperature ?: firstForecast?.avg_temp_c ?: 30.0
        val baseCondition = live?.condition ?: firstForecast?.condition ?: "Sunny"
        val baseIcon = getIconCode(live?.icon ?: firstForecast?.icon ?: "113")

        val hourlyList = if (!timelineData.isNullOrEmpty()) {
            listOf(
                "morning" to "06:00 AM",
                "afternoon" to "12:00 PM",
                "evening" to "06:00 PM",
                "night" to "09:00 PM"
            ).mapNotNull { (periodKey, timeLabel) ->
                val p = timelineData[periodKey]
                if (p != null) {
                    HourlyPoint(
                        time = timeLabel,
                        conditionText = com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(p.condition ?: baseCondition),
                        conditionIcon = getIconCode(p.icon ?: baseIcon),
                        temp = "${(p.temp ?: baseTemp).toInt()}°"
                    )
                } else null
            }.ifEmpty {
                listOf(
                    HourlyPoint("06:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 4).toInt()}°"),
                    HourlyPoint("09:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 1).toInt()}°"),
                    HourlyPoint("12:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp + 3).toInt()}°"),
                    HourlyPoint("15:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp + 2).toInt()}°"),
                    HourlyPoint("18:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 2).toInt()}°"),
                    HourlyPoint("21:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 3).toInt()}°")
                )
            }
        } else {
            listOf(
                HourlyPoint("06:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 4).toInt()}°"),
                HourlyPoint("09:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 1).toInt()}°"),
                HourlyPoint("12:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp + 3).toInt()}°"),
                HourlyPoint("15:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp + 2).toInt()}°"),
                HourlyPoint("18:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 2).toInt()}°"),
                HourlyPoint("21:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 3).toInt()}°")
            )
        }

        val bestDaysMap = success.insights?.best_farming_days?.associateBy { it.date } ?: emptyMap()
        val riskDaysMap = success.insights?.high_risk_days?.associateBy { it.date } ?: emptyMap()
        val nextRainTimeStr = success.insights?.next_rain_time

        // Parse Seven Day Forecast
        val weekList = forecast?.forecast?.map { day ->
            val dayName = getDayOfWeekName(day.forecast_date)
            val bestItem = bestDaysMap[day.forecast_date]
            val riskItem = riskDaysMap[day.forecast_date]
            
            val windowBadgeText = bestItem?.reason ?: bestItem?.activity ?: riskItem?.reason ?: riskItem?.activity
            val windowBadgeType = when {
                bestItem != null -> "best"
                riskItem != null -> "risk"
                else -> null
            }

            WeeklyDayForecast(
                date = day.forecast_date,
                day = dayName,
                conditionText = day.condition,
                conditionIcon = getIconCode(day.icon),
                tempRange = "${day.max_temp_c.toInt()}° / ${day.min_temp_c.toInt()}°",
                rainProb = "${day.chance_of_rain}%",
                farmingAdvisoryRes = getFarmingAdvisoryRes(day.condition, day.chance_of_rain),
                windowBadgeText = windowBadgeText,
                windowBadgeColorType = windowBadgeType
            )
        } ?: emptyList()

        // Dynamic Crop Advisories from Backend Rule Engines
        val rainChance = forecast?.forecast?.firstOrNull()?.chance_of_rain ?: 0
        val cropList = if (!success.cropAdvisories.isNullOrEmpty()) {
            success.cropAdvisories.map { item ->
                CropAdvisory(
                    cropNameText = item.crop_name ?: item.crop_type?.replaceFirstChar { it.uppercase() },
                    titleText = item.title,
                    descriptionText = item.description
                )
            }
        } else if (!success.recommendations.isNullOrEmpty()) {
            success.recommendations.map { item ->
                CropAdvisory(
                    cropNameText = item.crop_type?.replaceFirstChar { it.uppercase() } ?: "Farming Advice",
                    titleText = item.title ?: item.category?.replaceFirstChar { it.uppercase() },
                    descriptionText = item.recommendation
                )
            }
        } else {
            listOf(
                CropAdvisory(
                    cropNameRes = R.string.crop_cotton,
                    irrigationRes = if (rainChance > 50) R.string.advisory_irrigation_avoid_rain else R.string.advisory_irrigation_morning,
                    harvestRes = if (rainChance > 50) R.string.advisory_harvest_delay else R.string.advisory_harvest_safe,
                    pesticideRes = if (rainChance > 40) R.string.advisory_pesticide_avoid else R.string.advisory_pesticide_recommended
                ),
                CropAdvisory(
                    cropNameRes = R.string.crop_rice,
                    irrigationRes = R.string.advisory_rice_water,
                    harvestRes = R.string.advisory_rice_harvest_safe,
                    pesticideRes = R.string.advisory_rice_pesticide_safe
                ),
                CropAdvisory(
                    cropNameRes = R.string.crop_vegetables,
                    irrigationRes = R.string.advisory_veg_irrigation,
                    harvestRes = R.string.advisory_veg_harvest,
                    pesticideRes = R.string.advisory_veg_pesticide
                )
            )
        }

        return WeatherDashboardData(
            locationName = resolvedName,
            currentTemp = live?.temperature ?: firstForecast?.avg_temp_c ?: 30.0,
            feelsLike = live?.feels_like ?: live?.temperature ?: 32.0,
            condition = live?.condition ?: firstForecast?.condition ?: "Sunny",
            conditionIcon = getIconCode(live?.icon ?: firstForecast?.icon ?: "113"),
            humidity = live?.humidity ?: firstForecast?.humidity ?: 60,
            windKph = live?.wind_kph ?: firstForecast?.wind_kph ?: 12.0,
            windDir = live?.wind_dir ?: "NW",
            pressureHpa = live?.pressure_hpa ?: firstForecast?.pressure_hpa ?: 1012.0,
            visibilityKm = live?.visibility_km ?: firstForecast?.visibility_km ?: 10.0,
            uvIndex = live?.uv_index ?: firstForecast?.uv_index ?: 6.0,
            sunrise = firstForecast?.sunrise ?: "06:00 AM",
            sunset = firstForecast?.sunset ?: "07:00 PM",
            moonPhase = firstForecast?.moon_phase ?: "Waxing Gibbous",
            hourlyForecast = hourlyList,
            alerts = mappedAlerts,
            sevenDayForecast = weekList,
            cropAdvisories = cropList,
            isFromCache = false,
            lastUpdatedText = getApplication<Application>().getString(R.string.last_updated, SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())),
            weatherSummary = forecast?.weather_summary ?: "",
            nextRainTime = nextRainTimeStr
        )
    }

    private fun mapSeverity(severity: String?): AlertSeverity {
        return when (severity?.lowercase()) {
            "critical", "danger", "red" -> AlertSeverity.CRITICAL
            "warning", "orange" -> AlertSeverity.WARNING
            else -> AlertSeverity.INFO
        }
    }

    private fun getDayOfWeekName(dateStr: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = format.parse(dateStr)
            val outFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            outFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun getIconCode(url: String): String {
        // Extracts the weather icon name/code (e.g. "//cdn.../day/113.png" -> "113")
        return try {
            url.substringBeforeLast(".png").substringAfterLast("/")
        } catch (e: Exception) {
            "113"
        }
    }

    private fun getFarmingAdvisoryRes(condition: String, rainChance: Int): Int {
        return when {
            rainChance > 60 -> R.string.advisory_avoid_spraying
            condition.contains("storm", ignoreCase = true) -> R.string.advisory_avoid_fields
            condition.contains("rain", ignoreCase = true) -> R.string.advisory_avoid_pesticides
            else -> R.string.advisory_good_farming
        }
    }
}

