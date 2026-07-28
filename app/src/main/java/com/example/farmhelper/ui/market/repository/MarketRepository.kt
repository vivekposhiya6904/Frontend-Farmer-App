package com.example.farmhelper.ui.market.repository

import com.example.farmhelper.api.RetrofitClient
import com.example.farmhelper.ui.market.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class MarketResult<out T> {
    data class Success<out T>(val data: T) : MarketResult<T>()
    data class Error(val message: String) : MarketResult<Nothing>()
    data class ExceptionError(val exception: Exception) : MarketResult<Nothing>()
}

class MarketRepository {

    suspend fun getDistricts(): MarketResult<DistrictResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getDistricts()
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch districts")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getCommodities(): MarketResult<CommodityResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getCommodities()
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch commodities")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getMarketsByDistrict(district: String, commodity: String? = null): MarketResult<MarketResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getMarketsByDistrict(district, commodity)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch markets")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun searchCropPrices(
        district: String? = null,
        commodity: String? = null,
        market: String? = null,
        variety: String? = null,
        date: String? = null,
        page: Int = 1,
        limit: Int = 50
    ): MarketResult<CropPriceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.searchCropPrices(
                district, commodity, market, variety, date, page, limit
            )
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to search crop prices")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getPriceHistory(district: String, commodity: String, days: Int = 30): MarketResult<PriceHistoryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getPriceHistory(district, commodity, days)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch price history")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getLatestPrices(district: String? = null, limit: Int = 50): MarketResult<LatestPriceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getLatestPrices(district, limit)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch latest prices")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getCropInsights(commodity: String, district: String): MarketResult<CropInsightsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getCropInsights(commodity, district)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch crop insights")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getTopMarkets(commodity: String, district: String? = null): MarketResult<TopMarketsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getTopMarkets(commodity, district)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch top markets")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getVarieties(commodity: String? = null, district: String? = null): MarketResult<VarietiesResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getVarieties(commodity, district)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch varieties")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun createSubscription(commodity: String, priceThreshold: Double, condition: String, market: String? = null): MarketResult<SubscriptionResponse> = withContext(Dispatchers.IO) {
        try {
            val req = SubscriptionRequest(commodity, priceThreshold, condition, market)
            val response = RetrofitClient.apiServices.createSubscription(req)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to create price alert")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getSubscriptions(): MarketResult<SubscriptionsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getSubscriptions()
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch subscriptions")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun deleteSubscription(subId: String): MarketResult<SubscriptionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.deleteSubscription(subId)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to delete subscription")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun addFavorite(commodity: String): MarketResult<FavoriteResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.addFavorite(FavoriteRequest(commodity))
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to add favorite crop")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getFavorites(): MarketResult<FavoritesListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getFavorites()
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch favorite crops")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun deleteFavorite(commodity: String): MarketResult<FavoriteResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.deleteFavorite(commodity)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to delete favorite crop")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun getCropAlerts(limit: Int = 20, isRead: Boolean? = null): MarketResult<CropAlertsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getCropAlerts(limit, isRead)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to fetch crop alerts")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }

    suspend fun markCropAlertRead(alertId: String): MarketResult<CropAlertsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.markCropAlertRead(alertId)
            if (response.isSuccessful && response.body() != null) {
                MarketResult.Success(response.body()!!)
            } else {
                MarketResult.Error(response.errorBody()?.string() ?: "Failed to mark alert as read")
            }
        } catch (e: Exception) {
            MarketResult.ExceptionError(e)
        }
    }
}
