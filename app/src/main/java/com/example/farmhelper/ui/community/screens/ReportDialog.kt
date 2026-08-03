package com.example.farmhelper.ui.community.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.theme.*

@Composable
fun ReportDialog(
    targetType: String, // "post" or "comment"
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String, description: String?) -> Unit
) {
    val reasons = listOf(
        "Spam",
        "Fake Information",
        "Offensive Content",
        "Harassment",
        "Violence",
        "Adult Content",
        "Other"
    )

    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var descriptionText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report ${targetType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}",
                fontWeight = FontWeight.Bold,
                color = ForestGreen,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Why are you reporting this ${targetType}?",
                    fontSize = 13.sp,
                    color = DarkGrayText,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (reason == selectedReason),
                                onClick = { selectedReason = reason }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (reason == selectedReason),
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reason,
                            fontSize = 14.sp,
                            color = DarkGrayText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = { Text("Additional details (optional)...", fontSize = 12.sp, color = MediumGrayText) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = ForestGreen.copy(alpha = 0.3f)
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    onSubmitReport(selectedReason, descriptionText.ifBlank { null })
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit Report", color = White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MediumGrayText)
            }
        },
        containerColor = WarmWhite,
        shape = RoundedCornerShape(20.dp)
    )
}
