package com.example.farmhelper.ui.market.models

data class DistrictResponse(
    val success: Boolean,
    val message: String,
    val data: DistrictData
)

data class DistrictData(
    val districts: List<String>
)

data class CommodityResponse(
    val success: Boolean,
    val message: String,
    val data: CommodityData
)

data class CommodityData(
    val commodities: List<String>
)

data class MarketResponse(
    val success: Boolean,
    val message: String,
    val data: MarketData
)

data class MarketData(
    val markets: List<String>
)

data class CropPriceItem(
    val arrival_date: String,
    val commodity: String,
    val district: String,
    val market: String,
    val min_price: Double,
    val modal_price: Double,
    val max_price: Double,
    val variety: String
)

data class CropPriceResponse(
    val success: Boolean,
    val message: String,
    val data: CropPriceSearchData
)

data class CropPriceSearchData(
    val total: Int,
    val page: Int,
    val limit: Int,
    val data: List<CropPriceItem>
)

data class PriceHistoryItem(
    val date: String,
    val modal_price: Double
)

data class PriceHistoryResponse(
    val success: Boolean,
    val message: String,
    val data: PriceHistoryData
)

data class PriceHistoryData(
    val commodity: String,
    val district: String,
    val history: List<PriceHistoryItem>
)

data class LatestPriceResponse(
    val success: Boolean,
    val message: String,
    val data: LatestPriceData
)

data class LatestPriceData(
    val prices: List<CropPriceItem>
)

data class CropInsightsResponse(
    val success: Boolean,
    val message: String,
    val data: CropInsightsData
)

data class CropInsightsData(
    val commodity: String,
    val district: String,
    val last_updated: String,
    val trends: CropTrends,
    val best_market: BestMarketInfo,
    val selling_recommendation: SellingRecommendation,
    val insights_summary: String
)

data class CropTrends(
    val daily: TrendInfo,
    val weekly: TrendInfo,
    val monthly: TrendInfo,
    val moving_averages: MovingAverages,
    val statistics_30_days: Stats30Days
)

data class TrendInfo(
    val difference: Double,
    val percentage_change: Double,
    val direction: String
)

data class MovingAverages(
    val ma_7: Double,
    val ma_30: Double
)

data class Stats30Days(
    val average_price: Double,
    val minimum_price: Double,
    val maximum_price: Double,
    val stability_score: Double
)

data class BestMarketInfo(
    val market: String,
    val modal_price: Double,
    val reason: String
)

data class SellingRecommendation(
    val decision: String,
    val reasons: List<String>
)

data class TopMarketsResponse(
    val success: Boolean,
    val message: String,
    val data: TopMarketsData
)

data class TopMarketsData(
    val commodity: String,
    val district: String?,
    val markets: List<TopMarketItem>
)

data class TopMarketItem(
    val market: String,
    val current_price: Double,
    val arrival_volume: Double,
    val price_score: Double,
    val volume_score: Double,
    val total_score: Double
)

data class SubscriptionRequest(
    val commodity: String,
    val price_threshold: Double,
    val condition: String,
    val market: String? = null
)

data class SubscriptionItem(
    val id: String? = null,
    val _id: String? = null,
    val commodity: String,
    val price_threshold: Double,
    val condition: String,
    val market: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
)

data class SubscriptionResponse(
    val success: Boolean,
    val message: String,
    val data: SubscriptionItem? = null
)

data class SubscriptionsListResponse(
    val success: Boolean,
    val message: String,
    val data: List<SubscriptionItem> = emptyList()
)

data class FavoriteRequest(
    val commodity: String
)

data class FavoriteItem(
    val id: String? = null,
    val _id: String? = null,
    val commodity: String,
    val created_at: String? = null
)

data class FavoriteResponse(
    val success: Boolean,
    val message: String,
    val data: FavoriteItem? = null
)

data class FavoritesListResponse(
    val success: Boolean,
    val message: String,
    val data: List<FavoriteItem> = emptyList()
)

data class CropAlertItem(
    val id: String? = null,
    val _id: String? = null,
    val commodity: String,
    val market: String? = null,
    val target_price: Double,
    val actual_price: Double,
    val condition: String,
    val title: String,
    val message: String,
    val is_read: Boolean = false,
    val created_at: String? = null
)

data class CropAlertsListResponse(
    val success: Boolean,
    val message: String,
    val data: List<CropAlertItem> = emptyList()
)

data class VarietiesData(
    val varieties: List<String>
)

data class VarietiesResponse(
    val success: Boolean,
    val message: String,
    val data: VarietiesData
)
