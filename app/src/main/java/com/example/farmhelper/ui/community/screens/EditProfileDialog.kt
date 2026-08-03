package com.example.farmhelper.ui.community.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.community.models.FarmerProfile
import com.example.farmhelper.ui.theme.*

@Composable
fun EditProfileDialog(
    currentProfile: FarmerProfile,
    onDismiss: () -> Unit,
    onSave: (bio: String?, village: String?, district: String?, state: String?, profileImage: String?, coverImage: String?) -> Unit
) {
    var bio by remember { mutableStateOf(currentProfile.bio ?: "") }
    var village by remember { mutableStateOf(currentProfile.village ?: "") }
    var district by remember { mutableStateOf(currentProfile.district ?: "") }
    var state by remember { mutableStateOf(currentProfile.state ?: "Gujarat") }
    var profileImage by remember { mutableStateOf(currentProfile.profileImage ?: "") }
    var coverImage by remember { mutableStateOf(currentProfile.coverImage ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Farmer Profile",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 300) bio = it },
                    label = { Text("Farmer Bio") },
                    placeholder = { Text("Describe your crops, location & experience...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        focusedLabelColor = ForestGreen
                    )
                )

                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text("Village / Town") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        focusedLabelColor = ForestGreen
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            focusedLabelColor = ForestGreen
                        )
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            focusedLabelColor = ForestGreen
                        )
                    )
                }

                OutlinedTextField(
                    value = profileImage,
                    onValueChange = { profileImage = it },
                    label = { Text("Profile Photo URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        focusedLabelColor = ForestGreen
                    )
                )

                OutlinedTextField(
                    value = coverImage,
                    onValueChange = { coverImage = it },
                    label = { Text("Cover Banner Photo URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        focusedLabelColor = ForestGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        bio.ifBlank { null },
                        village.ifBlank { null },
                        district.ifBlank { null },
                        state.ifBlank { null },
                        profileImage.ifBlank { null },
                        coverImage.ifBlank { null }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Profile", color = White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MediumGrayText)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = WarmWhite
    )
}
