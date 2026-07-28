package com.example.farmhelper.ui.market.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.market.models.*
import com.example.farmhelper.ui.market.repository.MarketRepository
import com.example.farmhelper.ui.market.repository.MarketResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.farmhelper.ui.weather.location.AppLocationProvider
import java.util.Locale

sealed interface MarketUiState {
    object Loading : MarketUiState
    data class Success(
        val prices: List<CropPriceItem>,
        val districts: List<String>,
        val commodities: List<String>,
        val markets: List<String> = emptyList()
    ) : MarketUiState
    data class Error(val message: String) : MarketUiState
}

sealed interface PriceHistoryUiState {
    object Loading : PriceHistoryUiState
    data class Success(val history: List<PriceHistoryItem>) : PriceHistoryUiState
    data class Error(val message: String) : PriceHistoryUiState
}

sealed interface InsightsUiState {
    object Loading : InsightsUiState
    data class Success(val insights: CropInsightsData) : InsightsUiState
    data class Error(val message: String) : InsightsUiState
}

sealed interface TopMarketsUiState {
    object Loading : TopMarketsUiState
    data class Success(val topMarkets: List<TopMarketItem>) : TopMarketsUiState
    data class Error(val message: String) : TopMarketsUiState
}

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MarketRepository()
    private val sharedPrefs = application.getSharedPreferences("market_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<MarketUiState>(MarketUiState.Loading)
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    private val _historyState = MutableStateFlow<PriceHistoryUiState>(PriceHistoryUiState.Loading)
    val historyState: StateFlow<PriceHistoryUiState> = _historyState.asStateFlow()

    private val _insightsState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val insightsState: StateFlow<InsightsUiState> = _insightsState.asStateFlow()

    private val _topMarketsState = MutableStateFlow<TopMarketsUiState>(TopMarketsUiState.Loading)
    val topMarketsState: StateFlow<TopMarketsUiState> = _topMarketsState.asStateFlow()

    private val _favoritesState = MutableStateFlow<List<String>>(emptyList())
    val favoritesState: StateFlow<List<String>> = _favoritesState.asStateFlow()

    private val _subscriptionsState = MutableStateFlow<List<SubscriptionItem>>(emptyList())
    val subscriptionsState: StateFlow<List<SubscriptionItem>> = _subscriptionsState.asStateFlow()

    private val _cropAlertsState = MutableStateFlow<List<CropAlertItem>>(emptyList())
    val cropAlertsState: StateFlow<List<CropAlertItem>> = _cropAlertsState.asStateFlow()

    // Filter states
    val selectedDistrict = MutableStateFlow<String?>(null)
    val selectedCommodity = MutableStateFlow<String?>(null)
    val selectedMarket = MutableStateFlow<String?>(null)

    // Current displayed crop detail (for graph & insights)
    val activeCommodity = MutableStateFlow<String>("Groundnut")
    val activeDistrict = MutableStateFlow<String>("Rajkot")

    // Chart Time Frame: 7, 30, 90, 180, 365 days
    val selectedDays = MutableStateFlow<Int>(30)

    // Search query state
    val searchQuery = MutableStateFlow<String>("")

    // Listening state for voice search
    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    // Recent searches list
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    init {
        loadRecentSearches()
        fetchInitialData()
        fetchFavorites()
        fetchSubscriptions()
        fetchCropAlerts()
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.value = MarketUiState.Loading
            
            val districtsResult = repository.getDistricts()
            val commoditiesResult = repository.getCommodities()

            var districts = emptyList<String>()
            var commodities = emptyList<String>()

            if (districtsResult is MarketResult.Success) {
                districts = districtsResult.data.data.districts
            }
            if (commoditiesResult is MarketResult.Success) {
                commodities = commoditiesResult.data.data.commodities
            }

            // Set default active commodity and district if lists are not empty
            if (activeCommodity.value.isEmpty() && commodities.isNotEmpty()) {
                activeCommodity.value = commodities.first()
            }
            if (activeDistrict.value.isEmpty() && districts.isNotEmpty()) {
                activeDistrict.value = districts.first()
            }

            // Load latest rates
            fetchLatestPrices(districts, commodities)
            fetchPriceHistoryAndInsights()
        }
    }

    private fun fetchLatestPrices(districts: List<String>, commodities: List<String>) {
        viewModelScope.launch {
            val response = repository.searchCropPrices(
                district = selectedDistrict.value,
                commodity = selectedCommodity.value,
                market = selectedMarket.value,
                page = 1,
                limit = 50
            )
            when (response) {
                is MarketResult.Success -> {
                    val prices = response.data.data.data
                    _uiState.value = MarketUiState.Success(
                        prices = prices,
                        districts = districts,
                        commodities = commodities,
                        markets = emptyList() // populated dynamically on district selection
                    )
                }
                is MarketResult.Error -> {
                    _uiState.value = MarketUiState.Error(response.message)
                }
                is MarketResult.ExceptionError -> {
                    _uiState.value = MarketUiState.Error(response.exception.localizedMessage ?: "Unknown network error")
                }
            }
        }
    }

    fun fetchPriceHistoryAndInsights() {
        viewModelScope.launch {
            val commodity = activeCommodity.value
            val district = activeDistrict.value
            if (commodity.isEmpty() || district.isEmpty()) return@launch

            _historyState.value = PriceHistoryUiState.Loading
            _insightsState.value = InsightsUiState.Loading

            // Fetch History
            when (val historyRes = repository.getPriceHistory(district, commodity, selectedDays.value)) {
                is MarketResult.Success -> {
                    _historyState.value = PriceHistoryUiState.Success(historyRes.data.data.history)
                }
                is MarketResult.Error -> {
                    _historyState.value = PriceHistoryUiState.Error(historyRes.message)
                }
                is MarketResult.ExceptionError -> {
                    _historyState.value = PriceHistoryUiState.Error(historyRes.exception.localizedMessage ?: "Unknown error")
                }
            }

            // Fetch Insights
            when (val insightsRes = repository.getCropInsights(commodity, district)) {
                is MarketResult.Success -> {
                    _insightsState.value = InsightsUiState.Success(insightsRes.data.data)
                }
                is MarketResult.Error -> {
                    _insightsState.value = InsightsUiState.Error(insightsRes.message)
                }
                is MarketResult.ExceptionError -> {
                    _insightsState.value = InsightsUiState.Error(insightsRes.exception.localizedMessage ?: "Unknown error")
                }
            }

            // Fetch Top Markets
            fetchTopMarkets(commodity, district)
        }
    }

    fun fetchTopMarkets(commodity: String, district: String? = null) {
        viewModelScope.launch {
            _topMarketsState.value = TopMarketsUiState.Loading
            when (val res = repository.getTopMarkets(commodity, district)) {
                is MarketResult.Success -> {
                    _topMarketsState.value = TopMarketsUiState.Success(res.data.data.markets)
                }
                is MarketResult.Error -> {
                    _topMarketsState.value = TopMarketsUiState.Error(res.message)
                }
                is MarketResult.ExceptionError -> {
                    _topMarketsState.value = TopMarketsUiState.Error(res.exception.localizedMessage ?: "Failed to fetch top markets")
                }
            }
        }
    }

    fun fetchFavorites() {
        viewModelScope.launch {
            when (val res = repository.getFavorites()) {
                is MarketResult.Success -> {
                    _favoritesState.value = res.data.data.map { it.commodity }
                }
                else -> {}
            }
        }
    }

    fun toggleFavorite(commodity: String) {
        viewModelScope.launch {
            val isFav = _favoritesState.value.contains(commodity)
            if (isFav) {
                when (repository.deleteFavorite(commodity)) {
                    is MarketResult.Success -> {
                        _favoritesState.value = _favoritesState.value.filter { !it.equals(commodity, ignoreCase = true) }
                    }
                    else -> {}
                }
            } else {
                when (repository.addFavorite(commodity)) {
                    is MarketResult.Success -> {
                        _favoritesState.value = _favoritesState.value + commodity
                    }
                    else -> {}
                }
            }
        }
    }

    fun fetchSubscriptions() {
        viewModelScope.launch {
            when (val res = repository.getSubscriptions()) {
                is MarketResult.Success -> {
                    _subscriptionsState.value = res.data.data
                }
                else -> {}
            }
        }
    }

    fun createSubscription(commodity: String, priceThreshold: Double, condition: String, market: String? = null) {
        viewModelScope.launch {
            when (repository.createSubscription(commodity, priceThreshold, condition, market)) {
                is MarketResult.Success -> {
                    fetchSubscriptions()
                }
                else -> {}
            }
        }
    }

    fun deleteSubscription(subId: String) {
        viewModelScope.launch {
            when (repository.deleteSubscription(subId)) {
                is MarketResult.Success -> {
                    _subscriptionsState.value = _subscriptionsState.value.filter { it.id != subId && it._id != subId }
                }
                else -> {}
            }
        }
    }

    fun fetchCropAlerts() {
        viewModelScope.launch {
            when (val res = repository.getCropAlerts()) {
                is MarketResult.Success -> {
                    _cropAlertsState.value = res.data.data
                }
                else -> {}
            }
        }
    }

    fun searchOrFilter() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is MarketUiState.Success) return@launch

            val query = searchQuery.value.trim()
            if (query.isNotEmpty()) {
                addRecentSearch(query)
            }

            // We perform API search using parameters
            val response = repository.searchCropPrices(
                district = selectedDistrict.value,
                commodity = selectedCommodity.value ?: if (query.isNotEmpty()) query else null,
                market = selectedMarket.value,
                page = 1,
                limit = 50
            )

            when (response) {
                is MarketResult.Success -> {
                    var finalPrices = response.data.data.data
                    // Local filter overlay by keyword if user enters specific search query for market or variety
                    if (query.isNotEmpty()) {
                        finalPrices = finalPrices.filter {
                            it.commodity.contains(query, ignoreCase = true) ||
                            it.market.contains(query, ignoreCase = true) ||
                            it.district.contains(query, ignoreCase = true) ||
                            it.variety.contains(query, ignoreCase = true)
                        }
                    }
                    _uiState.value = currentState.copy(prices = finalPrices)
                }
                is MarketResult.Error -> {
                    _uiState.value = MarketUiState.Error(response.message)
                }
                is MarketResult.ExceptionError -> {
                    _uiState.value = MarketUiState.Error(response.exception.localizedMessage ?: "Unknown network error")
                }
            }
        }
    }

    fun onDistrictSelected(district: String?) {
        selectedDistrict.value = district
        selectedMarket.value = null // reset market when district changes
        
        // Fetch markets for this district
        viewModelScope.launch {
            if (district != null) {
                val response = repository.getMarketsByDistrict(district)
                val currentState = _uiState.value
                if (currentState is MarketUiState.Success && response is MarketResult.Success) {
                    _uiState.value = currentState.copy(markets = response.data.data.markets)
                }
            } else {
                val currentState = _uiState.value
                if (currentState is MarketUiState.Success) {
                    _uiState.value = currentState.copy(markets = emptyList())
                }
            }
            searchOrFilter()
        }
    }

    fun onCommoditySelected(commodity: String?) {
        selectedCommodity.value = commodity
        searchOrFilter()
    }

    fun onMarketSelected(market: String?) {
        selectedMarket.value = market
        searchOrFilter()
    }

    fun selectActiveCropAndDistrict(commodity: String, district: String) {
        activeCommodity.value = commodity
        activeDistrict.value = district
        fetchPriceHistoryAndInsights()
    }

    fun setDays(days: Int) {
        selectedDays.value = days
        fetchPriceHistoryAndInsights()
    }

    fun clearFilters() {
        selectedDistrict.value = null
        selectedCommodity.value = null
        selectedMarket.value = null
        searchQuery.value = ""
        val currentState = _uiState.value
        if (currentState is MarketUiState.Success) {
            _uiState.value = currentState.copy(markets = emptyList())
        }
        searchOrFilter()
    }

    // Voice search simulated triggers
    fun startVoiceListening() {
        _isVoiceListening.value = true
    }

    fun stopVoiceListening(resultText: String? = null) {
        _isVoiceListening.value = false
        if (resultText != null) {
            searchQuery.value = resultText
            searchOrFilter()
        }
    }

    private fun loadRecentSearches() {
        val listStr = sharedPrefs.getString("recent_searches", "") ?: ""
        if (listStr.isNotEmpty()) {
            _recentSearches.value = listStr.split("|||").filter { it.isNotEmpty() }
        }
    }

    private fun addRecentSearch(query: String) {
        val currentList = _recentSearches.value.toMutableList()
        currentList.remove(query)
        currentList.add(0, query)
        if (currentList.size > 5) {
            currentList.removeAt(5)
        }
        _recentSearches.value = currentList
        sharedPrefs.edit().putString("recent_searches", currentList.joinToString("|||")).apply()
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        sharedPrefs.edit().remove("recent_searches").apply()
    }

    fun selectCurrentLocation(context: Context) {
        viewModelScope.launch {
            val location = AppLocationProvider.getCurrentLocation(context)
            if (location != null) {
                val geocoder = android.location.Geocoder(context, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val subAdmin = address.subAdminArea ?: ""
                        val locality = address.locality ?: ""
                        val admin = address.adminArea ?: ""
                        
                        val currentState = _uiState.value
                        if (currentState is MarketUiState.Success) {
                            val matchedDistrict = currentState.districts.find { district ->
                                district.equals(subAdmin, ignoreCase = true) ||
                                district.equals(locality, ignoreCase = true) ||
                                subAdmin.contains(district, ignoreCase = true) ||
                                locality.contains(district, ignoreCase = true) ||
                                admin.contains(district, ignoreCase = true)
                            }
                            if (matchedDistrict != null) {
                                onDistrictSelected(matchedDistrict)
                            } else {
                                onDistrictSelected(currentState.districts.firstOrNull())
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
