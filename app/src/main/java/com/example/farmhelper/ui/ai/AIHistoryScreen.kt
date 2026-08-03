package com.example.farmhelper.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmhelper.ui.ai.models.Conversation
import com.example.farmhelper.ui.ai.viewmodel.AIAssistantViewModel
import com.example.farmhelper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIHistoryScreen(
    onBackClick: () -> Unit = {},
    onSelectConversation: (String) -> Unit = {},
    viewModel: AIAssistantViewModel = viewModel()
) {
    val historyState by viewModel.historyUiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredConversations = remember(historyState.conversations, historyState.searchQuery) {
        if (historyState.searchQuery.isBlank()) {
            historyState.conversations
        } else {
            historyState.conversations.filter {
                it.title.contains(historyState.searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(historyState.searchQuery, ignoreCase = true)
            }
        }
    }

    val pinnedList = filteredConversations.filter { it.isPinned }
    val unpinnedList = filteredConversations.filter { !it.isPinned }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chat History",
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
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = ForestGreen)
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
        ) {
            // Search Bar
            if (isSearchActive) {
                OutlinedTextField(
                    value = historyState.searchQuery,
                    onValueChange = { viewModel.onSearchHistory(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search conversations...") },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = MediumGrayText) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = ForestGreen.copy(alpha = 0.2f),
                        focusedContainerColor = WarmWhite,
                        unfocusedContainerColor = WarmWhite
                    ),
                    singleLine = true
                )
            }

            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = MediumGrayText, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "No history found", fontWeight = FontWeight.Bold, color = DarkGrayText, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Your previous AI Assistant chats will appear here.", color = MediumGrayText, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pinnedList.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pinned Conversations",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(pinnedList, key = { it.id }) { item ->
                            ConversationHistoryCard(
                                conversation = item,
                                onClick = { onSelectConversation(item.id) },
                                onPin = { viewModel.onPinConversation(item.id) },
                                onDelete = { viewModel.onDeleteConversation(item.id) }
                            )
                        }
                    }

                    if (unpinnedList.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Conversations",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(unpinnedList, key = { it.id }) { item ->
                            ConversationHistoryCard(
                                conversation = item,
                                onClick = { onSelectConversation(item.id) },
                                onPin = { viewModel.onPinConversation(item.id) },
                                onDelete = { viewModel.onDeleteConversation(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationHistoryCard(
    conversation: Conversation,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText,
                            fontSize = 15.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MediumGrayText,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversation.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MediumGrayText.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPin, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (conversation.isPinned) Icons.Default.Pin else Icons.Outlined.PushPin,
                        contentDescription = "Pin",
                        tint = if (conversation.isPinned) ForestGreen else MediumGrayText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AlertRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
