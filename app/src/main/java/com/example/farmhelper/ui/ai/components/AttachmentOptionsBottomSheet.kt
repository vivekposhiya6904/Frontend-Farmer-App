package com.example.farmhelper.ui.ai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentOptionsBottomSheet(
    onDismiss: () -> Unit,
    onSelectOption: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attach Content",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MediumGrayText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val options = listOf(
                AttachmentOptionItem("camera", "Take Photo", "Capture crop leaf or disease", Icons.Outlined.CameraAlt),
                AttachmentOptionItem("gallery", "Choose Photo", "Select from photo gallery", Icons.Outlined.PhotoLibrary),
                AttachmentOptionItem("document", "Choose Document", "Soil test reports, PDF", Icons.Outlined.Description),
                AttachmentOptionItem("video", "Choose Video", "Short field video clip", Icons.Outlined.Videocam),
                AttachmentOptionItem("voice", "Record Voice", "Record audio question", Icons.Outlined.Mic)
            )

            options.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectOption(item.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOlive,
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GlowGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = item.icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGrayText,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MediumGrayText,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class AttachmentOptionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)
