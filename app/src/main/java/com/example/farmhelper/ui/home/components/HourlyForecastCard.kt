package com.example.farmhelper.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.home.models.HourlyForecast
import com.example.farmhelper.ui.theme.*

import androidx.compose.ui.res.stringResource
import com.example.farmhelper.R

@Composable
fun HourlyForecastCard(
    hourlyForecasts: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SectionHeader(title = stringResource(id = R.string.hourly_forecast))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(hourlyForecasts) { forecast ->
                val isNow = forecast.isNow
                Card(
                    modifier = Modifier.width(76.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNow) GlowGreen else White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isNow) PrimaryGreen else LightBorderGreen
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isNow) 3.dp else 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = forecast.time,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNow) PrimaryGreen else MediumGrayText,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = forecast.icon,
                            contentDescription = null,
                            tint = if (isNow) PrimaryGreen else AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${forecast.temp}°C",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
