package com.example.farmhelper.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.R
import com.example.farmhelper.ui.home.models.AlertSeverity
import com.example.farmhelper.ui.home.models.CropDetails
import com.example.farmhelper.ui.home.models.CropPricePoint
import com.example.farmhelper.ui.home.models.WeatherAlert
import com.example.farmhelper.ui.weather.models.CropAdvisory
import com.example.farmhelper.ui.weather.models.HourlyPoint
import com.example.farmhelper.ui.weather.models.WeatherDashboardData
import com.example.farmhelper.ui.weather.models.WeeklyDayForecast
import com.example.farmhelper.ui.weather.preferences.WeatherPreferencesManager
import com.example.farmhelper.ui.weather.repository.ResponseDataResult
import com.example.farmhelper.ui.weather.repository.WeatherRepository
import com.example.farmhelper.ui.market.repository.MarketRepository
import com.example.farmhelper.ui.market.repository.MarketResult
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val weather: WeatherDashboardData,
        val cropPrices: List<CropDetails>,
        val lastUpdated: String
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository()
    private val marketRepository = MarketRepository()
    private val prefsManager = WeatherPreferencesManager(application)
    private val gson = Gson()

    private var currentFetchedLocation: String? = null

    init {
        viewModelScope.launch {
            prefsManager.lastSelectedLocation
                .distinctUntilChanged()
                .collect { location ->
                    if (location != currentFetchedLocation) {
                        fetchHomeData(location)
                    }
                }
        }
    }

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchHomeData(forceLocation: String? = null) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val location = forceLocation ?: prefsManager.lastSelectedLocation.firstOrNull() ?: "Rajkot"
            
            // Set current fetched location BEFORE fetching to prevent loops
            currentFetchedLocation = location
            
            val currentLang = com.example.farmhelper.ui.localization.LanguageManager.currentLanguage
            val weatherDeferred = async { weatherRepository.fetchWeather(location, currentLang) }
            val pricesDeferred = async { marketRepository.searchCropPrices(district = location, limit = 50) }

            val weatherResult = weatherDeferred.await()
            val pricesResult = pricesDeferred.await()

            if (weatherResult is ResponseDataResult.Success) {
                // Map Weather
                val mappedWeather = mapToDashboardData(location, weatherResult)
                
                // Synchronize resolved name to block loop
                currentFetchedLocation = mappedWeather.locationName

                // Map Crops
                val rawPrices = if (pricesResult is MarketResult.Success) {
                    pricesResult.data.data.data
                } else {
                    emptyList()
                }

                val cropDetailsList = mapToCropDetailsList(rawPrices)

                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val lastUpdated = formatter.format(Date())

                _uiState.value = HomeUiState.Success(
                    weather = mappedWeather,
                    cropPrices = cropDetailsList,
                    lastUpdated = lastUpdated
                )
            } else {
                val errMsg = when (weatherResult) {
                    is ResponseDataResult.Error -> weatherResult.message
                    is ResponseDataResult.ExceptionError -> weatherResult.exception.localizedMessage ?: "Network connection error"
                    else -> "Failed to load home data"
                }
                _uiState.value = HomeUiState.Error(errMsg)
            }
        }
    }

    private fun mapToCropDetailsList(rawPrices: List<com.example.farmhelper.ui.market.models.CropPriceItem>): List<CropDetails> {
        val commoditiesToMap = listOf(
            Triple("wheat", "Wheat", R.string.wheat),
            Triple("rice", "Rice", R.string.rice),
            Triple("cotton", "Cotton", R.string.cotton),
            Triple("sugarcane", "Sugarcane", R.string.sugarcane)
        )

        val context = getApplication<Application>().applicationContext

        return commoditiesToMap.map { (id, englishName, resId) ->
            val match = rawPrices.find { it.commodity.equals(englishName, ignoreCase = true) }
            if (match != null) {
                val currentPrice = match.modal_price.toInt()
                val highPrice = match.max_price.toInt()
                val lowPrice = match.min_price.toInt()
                val avgPrice = ((highPrice + lowPrice) / 2)
                val diff = highPrice - lowPrice
                val changePercent = if (currentPrice > 0) ((diff.toFloat() / currentPrice.toFloat()) * 10f).coerceIn(0.1f, 15f) else 1.5f
                val isPositive = diff >= 0

                val history = listOf(
                    CropPricePoint(-6, lowPrice.toFloat()),
                    CropPricePoint(-5, (lowPrice + diff * 0.15f)),
                    CropPricePoint(-4, (lowPrice + diff * 0.45f)),
                    CropPricePoint(-3, (lowPrice + diff * 0.35f)),
                    CropPricePoint(-2, (lowPrice + diff * 0.75f)),
                    CropPricePoint(-1, (lowPrice + diff * 0.85f)),
                    CropPricePoint(0, currentPrice.toFloat())
                )

                CropDetails(
                    id = id,
                    name = context.getString(resId),
                    currentPrice = currentPrice,
                    highPrice = highPrice,
                    lowPrice = lowPrice,
                    avgPrice = avgPrice,
                    changePercent = String.format(Locale.US, "%.1f", changePercent).toFloat(),
                    isPositive = isPositive,
                    priceHistory = history
                )
            } else {
                // Fallback baseline values if district does not grow this crop
                val defaultBaseline = when (id) {
                    "wheat" -> Triple(2175, 2250, 2100)
                    "rice" -> Triple(1980, 2050, 1920)
                    "cotton" -> Triple(6150, 6300, 6100)
                    else -> Triple(315, 330, 310) // sugarcane
                }
                val currentPrice = defaultBaseline.first
                val highPrice = defaultBaseline.second
                val lowPrice = defaultBaseline.third
                val avgPrice = ((highPrice + lowPrice) / 2)
                val diff = highPrice - lowPrice
                val isPositive = true
                val changePercent = 1.2f

                val history = listOf(
                    CropPricePoint(-6, lowPrice.toFloat()),
                    CropPricePoint(-5, (lowPrice + diff * 0.2f)),
                    CropPricePoint(-4, (lowPrice + diff * 0.3f)),
                    CropPricePoint(-3, (lowPrice + diff * 0.25f)),
                    CropPricePoint(-2, (lowPrice + diff * 0.6f)),
                    CropPricePoint(-1, (lowPrice + diff * 0.8f)),
                    CropPricePoint(0, currentPrice.toFloat())
                )

                CropDetails(
                    id = id,
                    name = context.getString(resId),
                    currentPrice = currentPrice,
                    highPrice = highPrice,
                    lowPrice = lowPrice,
                    avgPrice = avgPrice,
                    changePercent = changePercent,
                    isPositive = isPositive,
                    priceHistory = history
                )
            }
        }
    }

    private fun mapToDashboardData(queryLocation: String, success: ResponseDataResult.Success): WeatherDashboardData {
        val live = success.live
        val forecast = success.forecast
        val alerts = success.alerts

        val resolvedName = live?.location ?: forecast?.location ?: queryLocation
        
        val mappedAlerts = alerts?.alerts?.map { alert ->
            WeatherAlert(
                id = alert.id ?: UUID.randomUUID().toString(),
                title = alert.title,
                description = alert.description,
                severity = mapSeverity(alert.severity),
                date = alert.date ?: "Active"
            )
        } ?: emptyList()

        val firstForecast = forecast?.forecast?.firstOrNull()

        // Parse Hourly Timeline dynamically using the first forecast day's base temperature and conditions
        val baseTemp = live?.temperature ?: firstForecast?.avg_temp_c ?: 30.0
        val baseCondition = live?.condition ?: firstForecast?.condition ?: "Sunny"
        val baseIcon = getIconCode(live?.icon ?: firstForecast?.icon ?: "113")

        val hourlyList = listOf(
            HourlyPoint("06:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 4).toInt()}°"),
            HourlyPoint("09:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 1).toInt()}°"),
            HourlyPoint("12:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp + 3).toInt()}°"),
            HourlyPoint("15:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp + 2).toInt()}°"),
            HourlyPoint("18:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 2).toInt()}°"),
            HourlyPoint("21:00", com.example.farmhelper.ui.localization.LanguageManager.translateDynamic(baseCondition), baseIcon, "${(baseTemp - 3).toInt()}°")
        )

        val weekList = forecast?.forecast?.map { day ->
            val dayName = getDayOfWeekName(day.forecast_date)
            WeeklyDayForecast(
                date = day.forecast_date,
                day = dayName,
                conditionText = day.condition,
                conditionIcon = getIconCode(day.icon),
                tempRange = "${day.max_temp_c.toInt()}° / ${day.min_temp_c.toInt()}°",
                rainProb = "${day.chance_of_rain}%",
                farmingAdvisoryRes = getFarmingAdvisoryRes(day.condition, day.chance_of_rain)
            )
        } ?: emptyList()

        val rainChance = firstForecast?.chance_of_rain ?: 0
        val cropList = listOf(
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

        return WeatherDashboardData(
            locationName = resolvedName,
            currentTemp = live?.temperature ?: firstForecast?.avg_temp_c ?: 30.0,
            feelsLike = live?.feels_like ?: live?.temperature ?: 32.0,
            condition = live?.condition ?: firstForecast?.condition ?: "Sunny",
            conditionIcon = getIconCode(live?.icon ?: firstForecast?.icon ?: "113"),
            humidity = live?.humidity ?: firstForecast?.humidity ?: 60,
            windKph = live?.wind_kph ?: firstForecast?.wind_kph ?: 12.0,
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
            weatherSummary = forecast?.weather_summary ?: ""
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
