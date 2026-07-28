package com.example.farmhelper.ui.home.models

import androidx.compose.ui.graphics.vector.ImageVector

data class HourlyForecast(
    val time: String,
    val temp: Int,
    val icon: ImageVector,
    val isNow: Boolean = false
)

data class WeeklyForecastDay(
    val day: String,
    val tempMax: Int,
    val tempMin: Int,
    val icon: ImageVector,
    val condition: String
)

data class CropPricePoint(
    val dayOffset: Int, // e.g. -6, -5, -4, -3, -2, -1, 0
    val price: Float
)

data class CropDetails(
    val id: String,
    val name: String,
    val currentPrice: Int,
    val highPrice: Int,
    val lowPrice: Int,
    val avgPrice: Int,
    val changePercent: Float,
    val isPositive: Boolean,
    val priceHistory: List<CropPricePoint>
)

data class MarketDetails(
    val name: String,
    val distanceKm: Float,
    val prevailingRate: Int,
    val volume: String,
    val transportCost: Int
)

data class WeatherAlert(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val date: String
)

enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val timeAgo: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val imageResId: Int? = null
)

data class GovernmentScheme(
    val id: String,
    val title: String,
    val description: String,
    val eligibility: String,
    val benefit: String,
    val applyUrl: String,
    val status: String? = null
)
