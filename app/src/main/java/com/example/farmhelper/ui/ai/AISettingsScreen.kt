package com.example.farmhelper.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmhelper.ui.ai.viewmodel.AIAssistantViewModel
import com.example.farmhelper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onBackClick: () -> Unit = {},
    viewModel: AIAssistantViewModel = viewModel()
) {
    val settings by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ForestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
            )
        },
        containerColor = WarmBeige
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voice & Audio Preferences Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Voice & Speech Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto Speak Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto Speak Responses", fontWeight = FontWeight.SemiBold, color = DarkGrayText)
                            Text(text = "Automatically play voice for assistant answers", fontSize = 12.sp, color = MediumGrayText)
                        }
                        Switch(
                            checked = settings.autoSpeak,
                            onCheckedChange = { viewModel.onUpdateSettings(autoSpeak = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = ForestGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speech Speed Slider
                    Text(text = "Speech Speed: ${String.format("%.1fx", settings.speechSpeed)}", fontWeight = FontWeight.SemiBold, color = DarkGrayText)
                    Slider(
                        value = settings.speechSpeed,
                        onValueChange = { viewModel.onUpdateSettings(speechSpeed = it) },
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = ForestGreen, activeTrackColor = ForestGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speech Gender Selector
                    Text(text = "Voice Gender", fontWeight = FontWeight.SemiBold, color = DarkGrayText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Female", "Male").forEach { gender ->
                            val isSelected = settings.speechGender == gender
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onUpdateSettings(speechGender = gender) },
                                label = { Text(gender) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = White,
                                    containerColor = SoftOlive
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // General & Storage Preferences Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "General Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save History Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Save Chat History", fontWeight = FontWeight.SemiBold, color = DarkGrayText)
                            Text(text = "Store local conversation history on device", fontSize = 12.sp, color = MediumGrayText)
                        }
                        Switch(
                            checked = settings.saveHistory,
                            onCheckedChange = { viewModel.onUpdateSettings(saveHistory = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = ForestGreen)
                        )
                    }
                }
            }

            // Backend Integration Placeholder Card (Milestone 2 Ready)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SoftOlive),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Dns, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FastAPI LLM Engine (Milestone 2)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Model: ${settings.selectedModel}\nStatus: Frontend Foundation Ready for API Integration",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MediumGrayText,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
