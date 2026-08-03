package com.example.farmhelper.ui.home.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.farmhelper.R
import com.example.farmhelper.ui.community.models.PostItem
import com.example.farmhelper.ui.home.models.CommunityPost
import com.example.farmhelper.ui.theme.*

@Composable
fun CommunityCard(
    post: CommunityPost,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLikedState by remember { mutableStateOf(post.isLiked) }
    var likesCountState by remember { mutableStateOf(post.likesCount) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SoftOlive),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarInitials = remember(post.authorName) {
                    post.authorName.trim().split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .take(2)
                        .joinToString("")
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlowGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarInitials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "${post.authorRole} • ${post.timeAgo}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MediumGrayText,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            post.imageResId?.let { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DarkGrayText,
                    lineHeight = 20.sp,
                    fontSize = 13.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (isLikedState) likesCountState-- else likesCountState++
                            isLikedState = !isLikedState
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLikedState) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLikedState) AlertRed else MediumGrayText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$likesCountState",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isLikedState) AlertRed else MediumGrayText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            Toast.makeText(context, "Opening Comments", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MediumGrayText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MediumGrayText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityItemCard(
    post: PostItem,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onAuthorClick: (authorId: String) -> Unit = {},
    onDeletePostClick: (() -> Unit)? = null,
    onReportPostClick: (() -> Unit)? = null,
    onBlockAuthorClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Author Avatar & Name & Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val authorName = post.authorName ?: "Farmer"
                val avatarInitials = remember(authorName) {
                    authorName.trim().split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .take(2)
                        .joinToString("")
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SoftOlive)
                        .clickable { post.authorId?.let { onAuthorClick(it) } },
                    contentAlignment = Alignment.Center
                ) {
                    if (!post.authorAvatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = post.authorAvatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = avatarInitials.ifEmpty { "F" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { post.authorId?.let { onAuthorClick(it) } }
                ) {
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText,
                            fontSize = 15.sp
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        post.location?.let { loc ->
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MediumGrayText,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = loc,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MediumGrayText,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        val formattedDate = post.createdAt.take(10)
                        Text(
                            text = "• $formattedDate",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MediumGrayText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // More Options Menu (⋮)
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MediumGrayText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        if (onDeletePostClick != null) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = AlertRed, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMoreMenu = false
                                    onDeletePostClick()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                                }
                            )
                        }

                        if (onReportPostClick != null) {
                            DropdownMenuItem(
                                text = { Text("Report Post", color = DarkGrayText) },
                                onClick = {
                                    showMoreMenu = false
                                    onReportPostClick()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Outlined.Flag, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                }
                            )
                        }

                        if (onBlockAuthorClick != null) {
                            DropdownMenuItem(
                                text = { Text("Block Farmer", color = AlertRed) },
                                onClick = {
                                    showMoreMenu = false
                                    onBlockAuthorClick()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Outlined.Block, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Crop Tag Badge
                val cropTag = post.cropTag ?: "General"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GlowGreen.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tag,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = cropTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Text Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DarkGrayText,
                    lineHeight = 21.sp,
                    fontSize = 14.sp
                )
            )

            // Explicitly distinguish post types
            val isYouTubePost = !post.youtubeVideoId.isNullOrBlank() ||
                    !post.thumbnailUrl.isNullOrBlank() ||
                    (!post.videoUrl.isNullOrBlank() && post.videoUrl.contains("youtube", ignoreCase = true))

            val isImagePost = !isYouTubePost && !post.imageUrl.isNullOrBlank()

            if (isYouTubePost) {
                Spacer(modifier = Modifier.height(10.dp))
                val videoId = post.youtubeVideoId?.takeIf { it.isNotBlank() }

                // Primary thumbnail URL: post.thumbnailUrl -> hqdefault -> mqdefault -> default
                val primaryThumbnailUrl = remember(post.id, post.thumbnailUrl, videoId) {
                    post.thumbnailUrl?.ifBlank { null }
                        ?: videoId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
                        ?: videoId?.let { "https://img.youtube.com/vi/$it/mqdefault.jpg" }
                        ?: videoId?.let { "https://img.youtube.com/vi/$it/default.jpg" }
                }

                // Fallback thumbnail URL for newly uploaded YouTube videos that might still be processing hqdefault
                val fallbackThumbnailUrl = remember(videoId) {
                    videoId?.let { "https://img.youtube.com/vi/$it/default.jpg" }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ForestGreen.copy(alpha = 0.08f))
                        .clickable {
                            launchYouTubeVideo(context, post.videoUrl, post.youtubeVideoId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!primaryThumbnailUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = primaryThumbnailUrl,
                            contentDescription = "YouTube Video Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(SoftOlive),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = ForestGreen,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            },
                            error = {
                                if (!fallbackThumbnailUrl.isNullOrBlank() && fallbackThumbnailUrl != primaryThumbnailUrl) {
                                    SubcomposeAsyncImage(
                                        model = fallbackThumbnailUrl,
                                        contentDescription = "YouTube Video Thumbnail Fallback",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        error = { YouTubeFallbackContainer() }
                                    )
                                } else {
                                    YouTubeFallbackContainer()
                                }
                            }
                        )
                    } else {
                        YouTubeFallbackContainer()
                    }

                    // Play Button Overlay
                    Surface(
                        shape = CircleShape,
                        color = AlertRed.copy(alpha = 0.9f),
                        shadowElevation = 6.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play YouTube Video",
                            tint = White,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(32.dp)
                        )
                    }

                    // YouTube Badge Chip
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = AlertRed,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "YouTube",
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else if (isImagePost) {
                Spacer(modifier = Modifier.height(10.dp))
                SubcomposeAsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SoftOlive),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = ForestGreen,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SoftOlive),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ImageNotSupported,
                                    contentDescription = null,
                                    tint = MediumGrayText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Image unavailable",
                                    color = MediumGrayText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row (Like & Comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLikeClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) AlertRed else MediumGrayText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (post.isLiked) AlertRed else MediumGrayText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCommentClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MediumGrayText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MediumGrayText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun YouTubeFallbackContainer() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftOlive),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.PlayCircleOutline,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "YouTube Video",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ForestGreen
            )
        }
    }
}

private fun launchYouTubeVideo(context: android.content.Context, videoUrl: String?, youtubeVideoId: String?) {
    val cleanId = youtubeVideoId?.trim()?.ifBlank { null }
    val cleanUrl = videoUrl?.trim()?.ifBlank { null }

    val targetUrl = when {
        !cleanUrl.isNullOrBlank() -> cleanUrl
        !cleanId.isNullOrBlank() -> "https://www.youtube.com/watch?v=$cleanId"
        else -> null
    }

    if (targetUrl.isNullOrBlank()) {
        Toast.makeText(context, "Video URL is unavailable", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val fallbackUrl = if (!cleanId.isNullOrBlank()) "https://www.youtube.com/watch?v=$cleanId" else targetUrl
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (err: Exception) {
            Toast.makeText(context, "Cannot open video URL", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
