package com.example.farmhelper.ui.community.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.community.models.CommentItem
import com.example.farmhelper.ui.theme.*

sealed interface CommentsUiState {
    object Loading : CommentsUiState
    data class Success(val comments: List<CommentItem>) : CommentsUiState
    data class Error(val message: String) : CommentsUiState
    object Empty : CommentsUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    commentsState: CommentsUiState,
    postAuthorName: String,
    onDismiss: () -> Unit,
    onSubmitComment: (content: String, replyingToCommentId: String?) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputContent by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<CommentItem?>(null) }
    var commentToDelete by remember { mutableStateOf<CommentItem?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 18.sp
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MediumGrayText)
                }
            }

            HorizontalDivider(color = SoftOlive)

            // Body Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (commentsState) {
                    is CommentsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ForestGreen)
                        }
                    }

                    is CommentsUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = commentsState.message, color = AlertRed, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) {
                                Text("Retry", color = White)
                            }
                        }
                    }

                    is CommentsUiState.Empty -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No comments yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Be the first farmer to share thoughts or advice!",
                                style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                            )
                        }
                    }

                    is CommentsUiState.Success -> {
                        val commentsList = commentsState.comments
                        if (commentsList.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "No comments yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Be the first farmer to share thoughts!", style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(commentsList, key = { it.id }) { comment ->
                                    CommentCardItem(
                                        comment = comment,
                                        onReplyClick = { target -> replyingToComment = target },
                                        onDeleteClick = { target -> commentToDelete = target }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Replying Banner
            replyingToComment?.let { target ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftOlive)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.SubdirectoryArrowRight, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Replying to ${target.authorName}", style = MaterialTheme.typography.labelMedium.copy(color = ForestGreen, fontWeight = FontWeight.Bold))
                    }
                    IconButton(onClick = { replyingToComment = null }, modifier = Modifier.size(20.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Reply", tint = MediumGrayText, modifier = Modifier.size(14.dp))
                    }
                }
            }

            HorizontalDivider(color = SoftOlive)

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputContent,
                    onValueChange = { inputContent = it },
                    placeholder = {
                        Text(
                            if (replyingToComment != null) "Write a reply..." else "Add a comment...",
                            fontSize = 13.sp,
                            color = MediumGrayText
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = SoftOlive,
                        focusedContainerColor = White,
                        unfocusedContainerColor = SoftOlive.copy(alpha = 0.3f)
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputContent.trim().isNotBlank()) {
                            onSubmitComment(inputContent.trim(), replyingToComment?.id)
                            inputContent = ""
                            replyingToComment = null
                        }
                    },
                    enabled = inputContent.trim().isNotBlank(),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (inputContent.trim().isNotBlank()) ForestGreen else SoftOlive)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Comment",
                        tint = if (inputContent.trim().isNotBlank()) White else MediumGrayText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        commentToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { commentToDelete = null },
                title = { Text("Delete Comment", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this comment?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteComment(target.id)
                            commentToDelete = null
                        }
                    ) {
                        Text("Delete", color = AlertRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { commentToDelete = null }) {
                        Text("Cancel", color = MediumGrayText)
                    }
                }
            )
        }
    }
}

@Composable
fun CommentCardItem(
    comment: CommentItem,
    onReplyClick: (CommentItem) -> Unit,
    onDeleteClick: (CommentItem) -> Unit,
    isReply: Boolean = false
) {
    val initial = comment.authorName.take(1).uppercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 28.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Author Initials Avatar
            Box(
                modifier = Modifier
                    .size(if (isReply) 28.dp else 34.dp)
                    .clip(CircleShape)
                    .background(if (isReply) SoftOlive else ForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = if (isReply) ForestGreen else White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isReply) 11.sp else 13.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText,
                            fontSize = 12.sp
                        )
                    )
                    IconButton(
                        onClick = { onDeleteClick(comment) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MediumGrayText.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkGrayText,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Reply Action Button
                if (!isReply) {
                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier
                            .clickable { onReplyClick(comment) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Nested 1-Level Replies
        if (comment.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            comment.replies.forEach { reply ->
                CommentCardItem(
                    comment = reply,
                    onReplyClick = onReplyClick,
                    onDeleteClick = onDeleteClick,
                    isReply = true
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
