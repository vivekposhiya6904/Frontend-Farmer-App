package com.example.farmhelper.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.R
import com.example.farmhelper.ui.theme.*

@Composable
fun GreetingHeader(
    userName: String,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            ProfileImage(
                name = userName,
                onClick = onProfileClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.good_morning),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MediumGrayText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
                Text(
                    text = "$userName 👋",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DarkGrayText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageSelector(
                currentCode = currentLanguageCode,
                onSelected = onLanguageSelected
            )
            NotificationButton(
                badgeCount = 3,
                onClick = onNotificationsClick
            )
        }
    }
}

@Composable
fun ProfileImage(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        if (name.isNotEmpty()) {
            name.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")
        } else {
            "F"
        }
    }

    // Leaf-shaped organic silhouette container
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(ForestGreen, SageGreen)
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
    }
}

@Composable
fun LanguageSelector(
    currentCode: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLanguage = currentCode.uppercase()

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(SoftOlive)
                .border(1.dp, ForestGreen.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = "Select Language",
                tint = ForestGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayLanguage,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(WarmWhite)
        ) {
            DropdownMenuItem(
                text = { Text(if (currentCode == "en") "✓ English" else "English", color = DarkGrayText) },
                onClick = {
                    onSelected("en")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (currentCode == "hi") "✓ हिन्दी" else "हिन्दी", color = DarkGrayText) },
                onClick = {
                    onSelected("hi")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (currentCode == "gu") "✓ ગુજરાતી" else "ગુજરાતી", color = DarkGrayText) },
                onClick = {
                    onSelected("gu")
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun NotificationButton(
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SoftOlive)
            .border(1.dp, ForestGreen.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = ForestGreen,
            modifier = Modifier.size(20.dp)
        )
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .size(12.dp)
                    .background(ClayOrange, CircleShape)
            )
        }
    }
}
