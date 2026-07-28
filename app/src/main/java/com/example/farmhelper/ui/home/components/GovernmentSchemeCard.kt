package com.example.farmhelper.ui.home.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.R
import com.example.farmhelper.ui.home.models.GovernmentScheme
import com.example.farmhelper.ui.theme.*

@Composable
fun GovernmentSchemeCard(
    schemes: List<GovernmentScheme>,
    onViewAllSchemesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SectionHeader(
            title = stringResource(id = R.string.gov_schemes)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(schemes) { scheme ->
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(26.dp), // Premium organic corners
                    colors = CardDefaults.cardColors(containerColor = LightCream), // Light Cream background
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Title header & Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(GlowGreen, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalance,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = scheme.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkGrayText,
                                        fontSize = 15.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Dynamic Status Badge
                            scheme.status?.let { statusText ->
                                val (statusBg, statusTextColor) = when (statusText.uppercase()) {
                                    "NEW" -> Pair(GlowGreen, ForestGreen)
                                    "POPULAR" -> Pair(Color(0xFFE3F2FD), Color(0xFF1976D2))
                                    "ENDING SOON" -> Pair(Color(0xFFFFEBEE), AlertRed)
                                    "ELIGIBLE" -> Pair(Color(0xFFFFF3E0), AlertOrange)
                                    else -> Pair(SoftFieldGreen, MediumGrayText)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = statusTextColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Benefit Highlight
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftFieldGreen)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.benefit),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MediumGrayText)
                                )
                                Text(
                                    text = scheme.benefit,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description and Eligibility
                        Text(
                            text = scheme.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkGrayText.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stringResource(id = R.string.eligibility)}: ${scheme.eligibility}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MediumGrayText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Apply Button
                        Button(
                            onClick = {
                                Toast.makeText(context, "Applying for: ${scheme.title}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = White)
                        ) {
                            Text(
                                text = stringResource(id = R.string.apply_now),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // View All Schemes Button
        Button(
            onClick = onViewAllSchemesClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoftOlive, contentColor = ForestGreen),
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f))
        ) {
            Text(
                text = stringResource(id = R.string.view_all_schemes),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
        }
    }
}
