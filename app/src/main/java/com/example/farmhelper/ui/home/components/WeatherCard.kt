package com.example.farmhelper.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.R
import com.example.farmhelper.ui.theme.*

@Composable
fun WeatherCard(
    currentTemp: Int,
    condition: String,
    rainChance: String,
    advisoryTitle: String,
    advisorySubtitle: String,
    advisoryColor: Color,
    onViewFullWeatherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp), // organic corners
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SageGreen.copy(alpha = 0.85f), SoftOlive.copy(alpha = 0.95f))
                    )
                )
                .padding(24.dp)
        ) {
            // Temperature and Weather Icon Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.current_weather),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currentTemp°C",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 44.sp
                        )
                    )
                    Text(
                        text = getLocalizedCondition(condition),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ForestGreen.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Weather illustration representation
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(White.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(34.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = condition,
                        tint = AccentGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rain Chance Display
            Text(
                text = stringResource(id = R.string.rain_chance, rainChance),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ForestGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Farmer-Centric Advisory Block (High contrast, readable WarmWhite surface)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmWhite, RoundedCornerShape(16.dp))
                    .border(1.dp, ForestGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = advisoryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = advisoryTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = advisorySubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkGrayText.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View Full Weather Button (Primary call-to-action)
            Button(
                onClick = onViewFullWeatherClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.view_full_weather),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun getLocalizedCondition(condition: String): String {
    val resId = when (condition.lowercase()) {
        "sunny", "clear" -> R.string.weather_sunny
        "cloudy", "overcast" -> R.string.weather_cloudy
        "partly cloudy" -> R.string.weather_partly_cloudy
        "heavy rain", "torrential rain" -> R.string.weather_heavy_rain
        "light rain", "patchy light rain", "showers", "scattered showers" -> R.string.weather_scattered_showers
        else -> null
    }
    return if (resId != null) stringResource(resId) else condition
}
