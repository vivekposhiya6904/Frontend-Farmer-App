package com.example.farmhelper.ui.community.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.farmhelper.ui.community.models.CommunityNotificationItem
import com.example.farmhelper.ui.community.viewmodel.CommunityNotificationViewModel
import com.example.farmhelper.ui.community.viewmodel.NotificationUiState
import com.example.farmhelper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityNotificationScreen(
    onBackClick: () -> Unit,
    onNavigateToTarget: (postId: String?, commentId: String?, actorUserId: String?, type: String) -> Unit,
    viewModel: CommunityNotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    var filterUnreadOnly by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = AlertRed, contentColor = White) {
                                Text("$unreadCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ForestGreen
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark All Read", color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
            )
        },
        containerColor = WarmBeige
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Filter Bar (All / Unread)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !filterUnreadOnly,
                        onClick = {
                            filterUnreadOnly = false
                            viewModel.toggleUnreadFilter(false)
                        },
                        label = { Text("All", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = White,
                            containerColor = SoftOlive
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    FilterChip(
                        selected = filterUnreadOnly,
                        onClick = {
                            filterUnreadOnly = true
                            viewModel.toggleUnreadFilter(true)
                        },
                        label = { Text("Unread Only", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = White,
                            containerColor = SoftOlive
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                HorizontalDivider(color = ForestGreen.copy(alpha = 0.1f))

                // Notification Feed Content Area
                when (val state = uiState) {
                    is NotificationUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ForestGreen)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Loading notifications...", color = ForestGreen, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    is NotificationUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Error: ${state.message}", color = AlertRed, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.refreshNotifications() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                ) {
                                    Text("Retry", color = White)
                                }
                            }
                        }
                    }

                    is NotificationUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsOff,
                                    contentDescription = null,
                                    tint = MediumGrayText,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (filterUnreadOnly) "No unread notifications" else "No notifications yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = DarkGrayText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "You will be notified when other farmers like or comment on your community posts.",
                                    fontSize = 13.sp,
                                    color = MediumGrayText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    is NotificationUiState.Success -> {
                        val pullRefreshState = rememberPullToRefreshState()

                        PullToRefreshBox(
                            isRefreshing = state.isRefreshing,
                            onRefresh = { viewModel.refreshNotifications() },
                            state = pullRefreshState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                itemsIndexed(state.notifications) { index, notification ->
                                    NotificationCard(
                                        notification = notification,
                                        onClick = {
                                            viewModel.markAsRead(notification.id)
                                            onNavigateToTarget(
                                                notification.postId,
                                                notification.commentId,
                                                notification.actorUserId,
                                                notification.notificationType
                                            )
                                        },
                                        onDelete = {
                                            viewModel.deleteNotification(notification.id)
                                        }
                                    )

                                    if (index >= state.notifications.size - 2 && state.hasMore && !state.isLoadingMore) {
                                        LaunchedEffect(Unit) {
                                            viewModel.loadMore()
                                        }
                                    }
                                }

                                if (state.isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = ForestGreen, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: CommunityNotificationItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val containerBg = if (!notification.isRead) SoftOlive.copy(alpha = 0.5f) else WarmWhite
    val borderStroke = if (!notification.isRead) BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f)) else BorderStroke(1.dp, ForestGreen.copy(alpha = 0.08f))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Actor Avatar with Type Icon Overlay Badge
            Box {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SoftOlive),
                    contentAlignment = Alignment.Center
                ) {
                    if (!notification.actorAvatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = notification.actorAvatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = notification.actorName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 16.sp
                        )
                    }
                }

                // Small notification type badge overlay
                val typeIcon = when (notification.notificationType) {
                    "like" -> Icons.Outlined.FavoriteBorder
                    "comment" -> Icons.Outlined.ChatBubbleOutline
                    "reply" -> Icons.AutoMirrored.Outlined.Reply
                    else -> Icons.Outlined.Notifications
                }

                val typeColor = when (notification.notificationType) {
                    "like" -> AlertRed
                    "comment" -> ForestGreen
                    "reply" -> ForestGreen
                    else -> MediumGrayText
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(WarmWhite)
                        .border(1.dp, typeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = DarkGrayText,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatRelativeTime(notification.createdAt),
                    fontSize = 11.sp,
                    color = MediumGrayText
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ForestGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MediumGrayText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatRelativeTime(isoString: String): String {
    return try {
        // Simple fallback formatter string display
        if (isoString.contains("T")) {
            val datePart = isoString.split("T")[0]
            val timePart = isoString.split("T")[1].take(5)
            "$datePart at $timePart"
        } else isoString
    } catch (e: Exception) {
        isoString
    }
}
