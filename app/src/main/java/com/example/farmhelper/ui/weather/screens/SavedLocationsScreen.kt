package com.example.farmhelper.ui.weather.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.theme.*
import com.example.farmhelper.ui.weather.models.SavedLocationCreateRequest
import com.example.farmhelper.ui.weather.models.SavedLocationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLocationsScreen(
    savedLocations: List<SavedLocationItem>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onAddLocation: (SavedLocationCreateRequest) -> Unit,
    onDeleteLocation: (String) -> Unit,
    onSetDefaultLocation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

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
                    text = "Saved Farm Fields",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(ForestGreen, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Location",
                        tint = White
                    )
                }
            }

            HorizontalDivider(color = ForestGreen.copy(alpha = 0.1f))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ForestGreen)
                }
            } else if (savedLocations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhite),
                        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Saved Fields Yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Save your farm locations with custom nicknames (e.g., 'Home Farm', 'North Paddock') to quickly check weather across all your fields.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = DarkGrayText),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Add Farm Location", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedLocations) { item ->
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
                                    IconButton(
                                        onClick = {
                                            item.location_id?.let { onSetDefaultLocation(it) }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (item.is_default) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Default Farm",
                                            tint = if (item.is_default) AccentGreen else MediumGrayText
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        val displayName = item.nickname?.ifBlank { item.location_name } ?: item.location_name
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreen
                                            )
                                        )
                                        Text(
                                            text = "${item.location_name}, ${item.district ?: ""}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = DarkGrayText)
                                        )
                                        if (item.is_default) {
                                            Text(
                                                text = "Default Primary Field",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = ForestGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                                item.location_id?.let { locId ->
                                    IconButton(onClick = { onDeleteLocation(locId) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Field",
                                            tint = AlertRed.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddSavedLocationDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { locReq ->
                        onAddLocation(locReq)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSavedLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (SavedLocationCreateRequest) -> Unit
) {
    var nickname by remember { mutableStateOf("Home Farm") }
    var locationName by remember { mutableStateOf("Gondal") }
    var district by remember { mutableStateOf("Rajkot") }
    var state by remember { mutableStateOf("Gujarat") }
    var isDefault by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save New Farm Location",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Field Nickname (e.g. Home Farm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Location / City Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("District Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isDefault = !isDefault }
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set as Primary Default Field", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (locationName.isNotBlank() && nickname.isNotBlank()) {
                        onConfirm(
                            SavedLocationCreateRequest(
                                location_name = locationName.trim(),
                                latitude = 21.96,
                                longitude = 70.80,
                                district = district.trim(),
                                state = state.trim(),
                                country = "India",
                                nickname = nickname.trim(),
                                is_default = isDefault
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Save Location", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MediumGrayText)
            }
        }
    )
}
