package com.example.farmhelper.ui.ai.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.ai.models.VoiceState
import com.example.farmhelper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantBottomSheet(
    voiceState: VoiceState,
    onDismiss: () -> Unit,
    onToggleState: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Pulse Animation for Mic Ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Voice Assistant",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MediumGrayText)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Microphone Visualizer Container
            Box(
                modifier = Modifier
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse Rings
                if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                if (voiceState == VoiceState.LISTENING) GlowGreen.copy(alpha = 0.4f)
                                else AccentGreen.copy(alpha = 0.3f)
                            )
                    )
                }

                // Inner Main Circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = when (voiceState) {
                                    VoiceState.LISTENING -> listOf(ForestGreen, SageGreen)
                                    VoiceState.PROCESSING -> listOf(AccentGreen, ForestGreen)
                                    VoiceState.SPEAKING -> listOf(ForestGreen, GlowGreen)
                                    else -> listOf(MediumGrayText, DarkGrayText)
                                }
                            )
                        )
                        .border(3.dp, WarmWhite, CircleShape)
                        .clickable { onToggleState() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (voiceState) {
                            VoiceState.SPEAKING -> Icons.Default.VolumeUp
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Microphone",
                        tint = White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // State Description Text
            val stateTitle = when (voiceState) {
                VoiceState.LISTENING -> "Listening..."
                VoiceState.PROCESSING -> "Processing your request..."
                VoiceState.SPEAKING -> "Assistant is speaking..."
                VoiceState.IDLE -> "Tap microphone to speak"
                VoiceState.ERROR -> "Voice recognition failed"
            }

            val stateSubtitle = when (voiceState) {
                VoiceState.LISTENING -> "Speak clearly in English, Gujarati, or Hindi"
                VoiceState.PROCESSING -> "Analyzing speech & generating advice..."
                VoiceState.SPEAKING -> "Playing audio response"
                VoiceState.IDLE -> "Press mic and ask about your crops or weather"
                VoiceState.ERROR -> "Please check microphone permissions and try again"
            }

            Text(
                text = stateTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkGrayText,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stateSubtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MediumGrayText,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
                ) {
                    Text("Cancel", color = ForestGreen, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retry", color = White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
