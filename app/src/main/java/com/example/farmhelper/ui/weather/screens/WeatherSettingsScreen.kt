package com.example.farmhelper.ui.weather.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.localization.LanguageManager
import com.example.farmhelper.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSettingsScreen(
    currentUnit: String,
    notificationsEnabled: Boolean,
    onBackClick: () -> Unit,
    onUnitChanged: (String) -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedLanguage by remember { mutableStateOf(LanguageManager.currentLanguage) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WarmBeige
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SoftOlive, CircleShape)
                        .border(1.dp, ForestGreen.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ForestGreen
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Weather Preferences",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 20.sp
                    )
                )
            }

            HorizontalDivider(color = ForestGreen.copy(alpha = 0.1f))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Language Preference
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = ForestGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Preferred Language",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("en" to "English", "hi" to "हिंदी", "gu" to "ગુજરાતી").forEach { (code, label) ->
                                FilterChip(
                                    selected = (selectedLanguage == code),
                                    onClick = {
                                        selectedLanguage = code
                                        coroutineScope.launch {
                                            LanguageManager.saveLanguage(context, code)
                                        }
                                        onLanguageSelected(code)
                                    },
                                    label = { Text(label, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreen,
                                        selectedLabelColor = White,
                                        containerColor = SoftOlive,
                                        labelColor = DarkGrayText
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Temperature Unit Preference
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Thermostat,
                                contentDescription = null,
                                tint = ForestGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Temperature Unit",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                )
                                Text(
                                    text = "Display temperatures in ${if (currentUnit == "F") "Fahrenheit (°F)" else "Celsius (°C)"}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                                )
                            }
                        }
                        Row {
                            FilterChip(
                                selected = (currentUnit == "C"),
                                onClick = { onUnitChanged("C") },
                                label = { Text("°C", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = White,
                                    containerColor = SoftOlive,
                                    labelColor = DarkGrayText
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = (currentUnit == "F"),
                                onClick = { onUnitChanged("F") },
                                label = { Text("°F", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = White,
                                    containerColor = SoftOlive,
                                    labelColor = DarkGrayText
                                )
                            )
                        }
                    }
                }

                // 3. Severe Weather Notification Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = ForestGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Severe Weather Alerts",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                )
                                Text(
                                    text = "Receive background warnings for storms, heavy rainfall, & heatwaves",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = ForestGreen,
                                uncheckedThumbColor = MediumGrayText,
                                uncheckedTrackColor = SoftOlive
                            )
                        )
                    }
                }
            }
        }
    }
}
