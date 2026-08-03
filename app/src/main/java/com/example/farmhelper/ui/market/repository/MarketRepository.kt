package com.example.farmhelper.ui.market.repository

import com.example.farmhelper.api.NetworkErrorHandler
import com.example.farmhelper.api.RetrofitClient
import com.example.farmhelper.ui.market.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class MarketResult<out T> {
    data class Success<out T>(val data: T) : MarketResult<T>()
    data class Error(val message: String, val isServerWaking: Boolean = false) : MarketResult<Nothing>()
    data class ExceptionError(val exception: Exception) : MarketResult<Nothing>()
}

class MarketRepository {

    suspend fun getDistricts(): MarketResult<DistrictResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getDistricts()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getCommodities(): MarketResult<CommodityResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getCommodities()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getMarketsByDistrict(district: String, commodity: String? = null): MarketResult<MarketResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getMarketsByDistrict(district, commodity)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
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
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getPriceHistory(district: String, commodity: String, days: Int = 30): MarketResult<PriceHistoryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getPriceHistory(district, commodity, days)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getLatestPrices(district: String? = null, limit: Int = 50): MarketResult<LatestPriceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getLatestPrices(district, limit)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getCropInsights(commodity: String, district: String): MarketResult<CropInsightsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getCropInsights(commodity, district)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getTopMarkets(commodity: String, district: String? = null): MarketResult<TopMarketsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getTopMarkets(commodity, district)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getVarieties(commodity: String? = null, district: String? = null): MarketResult<VarietiesResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getVarieties(commodity, district)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun createSubscription(commodity: String, priceThreshold: Double, condition: String, market: String? = null): MarketResult<SubscriptionResponse> = withContext(Dispatchers.IO) {
        try {
            val req = SubscriptionRequest(commodity, priceThreshold, condition, market)
            val response = RetrofitClient.apiServices.createSubscription(req)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getSubscriptions(): MarketResult<SubscriptionsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getSubscriptions()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun deleteSubscription(subId: String): MarketResult<SubscriptionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.deleteSubscription(subId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun addFavorite(commodity: String): MarketResult<FavoriteResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.addFavorite(FavoriteRequest(commodity))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getFavorites(): MarketResult<FavoritesListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getFavorites()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun deleteFavorite(commodity: String): MarketResult<FavoriteResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.deleteFavorite(commodity)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun getCropAlerts(limit: Int = 20, isRead: Boolean? = null): MarketResult<CropAlertsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.getCropAlerts(limit, isRead)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }

    suspend fun markCropAlertRead(alertId: String): MarketResult<CropAlertsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiServices.markCropAlertRead(alertId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                MarketResult.Success(body)
            } else {
                MarketResult.Error(NetworkErrorHandler.parseErrorResponse(response))
            }
        } catch (e: Exception) {
            MarketResult.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }
}
