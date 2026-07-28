package com.example.farmhelper.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.home.models.AlertSeverity
import com.example.farmhelper.ui.home.models.WeatherAlert
import com.example.farmhelper.ui.theme.*

@Composable
fun AlertCard(
    alert: WeatherAlert,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, contentColor, icon) = when (alert.severity) {
        AlertSeverity.CRITICAL -> AlertQuadruple(
            Color(0xFFFFEBEE),
            Color(0xFFFFCDD2),
            AlertRed,
            Icons.Outlined.ErrorOutline
        )
        AlertSeverity.WARNING -> AlertQuadruple(
            Color(0xFFFFF3E0),
            Color(0xFFFFE0B2),
            AlertOrange,
            Icons.Outlined.Warning
        )
        AlertSeverity.INFO -> AlertQuadruple(
            Color(0xFFFFFDE7),
            Color(0xFFFFF9C4),
            AlertYellow,
            Icons.Outlined.Info
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Alert",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkGrayText.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.date,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MediumGrayText,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

private data class AlertQuadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
