package com.example.farmhelper.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.R
import com.example.farmhelper.ui.home.models.CropDetails
import com.example.farmhelper.ui.home.models.CropPricePoint
import com.example.farmhelper.ui.theme.*

@Composable
fun CropTabs(
    crops: List<CropDetails>,
    selectedCropId: String,
    onCropSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = crops.indexOfFirst { it.id == selectedCropId }.coerceAtLeast(0),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        containerColor = Color.Transparent,
        contentColor = ForestGreen,
        edgePadding = 0.dp,
        divider = {},
        indicator = { tabPositions ->
            val index = crops.indexOfFirst { it.id == selectedCropId }.coerceAtLeast(0)
            if (index < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = ForestGreen,
                    height = 3.dp
                )
            }
        }
    ) {
        crops.forEach { crop ->
            Tab(
                selected = crop.id == selectedCropId,
                onClick = { onCropSelected(crop.id) },
                text = {
                    Text(
                        text = crop.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (crop.id == selectedCropId) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                },
                selectedContentColor = ForestGreen,
                unselectedContentColor = MediumGrayText
            )
        }
    }
}

@Composable
fun PriceDashboard(
    selectedCrop: CropDetails,
    onViewMarketDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Stats Row: Current Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.current_rate),
                        style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${selectedCrop.currentPrice}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText,
                                fontSize = 30.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${if (selectedCrop.isPositive) "+" else ""}${selectedCrop.changePercent}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (selectedCrop.isPositive) ForestGreen else AlertRed,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // Small badge to show trend icon clearly
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(if (selectedCrop.isPositive) ForestGreen.copy(alpha = 0.08f) else Color(0xFFFFEBEE), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedCrop.isPositive) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = if (selectedCrop.isPositive) ForestGreen else AlertRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selling Recommendation Block (High Usability Priority)
            val (trendColor, trendIcon, trendTitle, trendSubtitle) = if (selectedCrop.isPositive) {
                PriceQuadruple(
                    ForestGreen,
                    Icons.Outlined.TrendingUp,
                    stringResource(id = R.string.good_selling_day),
                    stringResource(id = R.string.price_rising)
                )
            } else {
                PriceQuadruple(
                    AlertRed,
                    Icons.Outlined.TrendingDown,
                    stringResource(id = R.string.hold_selling),
                    stringResource(id = R.string.price_falling)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(trendColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .border(1.dp, trendColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = trendTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = trendColor,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = trendSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkGrayText.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Faint, Soft Bezier Sparkline representation (Low Emphasis)
            if (selectedCrop.priceHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 4.dp)
                ) {
                    PriceTrendGraph(
                        history = selectedCrop.priceHistory,
                        isPositive = selectedCrop.isPositive
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View Market Details Button
            Button(
                onClick = onViewMarketDetailsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftOlive, contentColor = ForestGreen),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f))
            ) {
                Text(
                    text = stringResource(id = R.string.view_market_details),
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
fun PriceTrendGraph(
    history: List<CropPricePoint>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor = if (isPositive) ForestGreen else AlertRed
    val gradientColor = if (isPositive) ForestGreen.copy(alpha = 0.1f) else AlertRed.copy(alpha = 0.1f)

    Canvas(modifier = modifier.fillMaxSize()) {
        if (history.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height

        val maxPrice = history.maxOf { it.price }
        val minPrice = history.minOf { it.price }
        val priceRange = if (maxPrice == minPrice) 1f else (maxPrice - minPrice)

        val minOffset = history.minOf { it.dayOffset }
        val maxOffset = history.maxOf { it.dayOffset }
        val offsetRange = if (maxOffset == minOffset) 1f else (maxOffset - minOffset).toFloat()

        val points = history.map { point ->
            val x = ((point.dayOffset - minOffset).toFloat() / offsetRange) * width
            val y = height - (((point.price - minPrice) / priceRange) * (height * 0.75f) + (height * 0.1f))
            Offset(x, y)
        }

        val path = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, points[0].y)

            for (i in 1 until points.size) {
                val prevPoint = points[i - 1]
                val currPoint = points[i]
                val controlX1 = prevPoint.x + (currPoint.x - prevPoint.x) / 2f
                val controlY1 = prevPoint.y
                val controlX2 = prevPoint.x + (currPoint.x - prevPoint.x) / 2f
                val controlY2 = currPoint.y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, currPoint.x, currPoint.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, currPoint.x, currPoint.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.lineTo(points.first().x, height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradientColor, Color.Transparent)
                )
            )

            drawPath(
                path = path,
                color = strokeColor.copy(alpha = 0.8f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun TrendingCrops(
    crops: List<CropDetails>,
    modifier: Modifier = Modifier
) {
    // Hidden on simplified home screen
}

private data class PriceQuadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
