package com.example.farmhelper.ui.community

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmhelper.ui.community.models.PostItem
import com.example.farmhelper.ui.community.screens.CreatePostDialog
import com.example.farmhelper.ui.community.viewmodel.CommunityFeedUiState
import com.example.farmhelper.ui.community.viewmodel.CommunityViewModel
import com.example.farmhelper.ui.community.viewmodel.CreatePostUiState
import com.example.farmhelper.ui.home.components.CommunityItemCard
import com.example.farmhelper.ui.theme.*

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Notifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    onBackClick: () -> Unit = {},
    onOpenProfile: (userId: String?) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    viewModel: CommunityViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val createPostState by viewModel.createPostState.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPostForComments by remember { mutableStateOf<PostItem?>(null) }
    var reportTargetPost by remember { mutableStateOf<PostItem?>(null) }
    var blockTargetUser by remember { mutableStateOf<Pair<String, String>?>(null) }

    val cropFilterTags = listOf("All", "Cotton", "Wheat", "Groundnut", "Rice", "Vegetables", "Bajra")
    var selectedTag by remember { mutableStateOf("All") }

    val listState = rememberLazyListState()

    // Handle Create Post State feedback
    LaunchedEffect(createPostState) {
        when (val state = createPostState) {
            is CreatePostUiState.Success -> {
                Toast.makeText(context, "Post published to community!", Toast.LENGTH_SHORT).show()
                showCreateDialog = false
                viewModel.resetCreatePostState()
            }
            is CreatePostUiState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetCreatePostState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Farmer Community",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    )
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
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Community",
                            tint = ForestGreen
                        )
                    }
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(containerColor = AlertRed, contentColor = White) {
                                        Text("$unreadNotificationCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = ForestGreen
                            )
                        }
                    }
                    IconButton(onClick = { onOpenProfile(null) }) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "My Profile",
                            tint = ForestGreen
                        )
                    }
                    IconButton(onClick = { viewModel.refreshFeed() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = ForestGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ForestGreen,
                contentColor = White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Post")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Post Update", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = WarmBeige
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Crop Filter Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmWhite)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cropFilterTags.forEach { tag ->
                        val isSelected = tag == selectedTag
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTag = tag
                                viewModel.filterByCropTag(tag)
                            },
                            label = {
                                Text(
                                    text = tag,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Outlined.Tag,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = White,
                                containerColor = SoftOlive,
                                labelColor = DarkGrayText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = ForestGreen.copy(alpha = 0.15f),
                                selectedBorderColor = ForestGreen
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                HorizontalDivider(color = ForestGreen.copy(alpha = 0.1f), thickness = 1.dp)

                // Main Feed Content Area
                when (val state = uiState) {
                    is CommunityFeedUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ForestGreen)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading community feed...",
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    is CommunityFeedUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.2f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Failed to load community feed",
                                        color = AlertRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.message,
                                        color = DarkGrayText,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.refreshFeed() },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                    ) {
                                        Text("Retry", color = White)
                                    }
                                }
                            }
                        }
                    }

                    is CommunityFeedUiState.Empty -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Forum,
                                    contentDescription = null,
                                    tint = MediumGrayText,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No posts yet for category '${state.selectedCropTag}'",
                                    color = DarkGrayText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Be the first farmer to share an update or ask a question!",
                                    color = MediumGrayText,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { showCreateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Create First Post", color = White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    is CommunityFeedUiState.Success -> {
                        val pullRefreshState = rememberPullToRefreshState()

                        PullToRefreshBox(
                            isRefreshing = state.isRefreshing,
                            onRefresh = { viewModel.refreshFeed() },
                            state = pullRefreshState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsIndexed(state.posts) { index, post ->
                                    CommunityItemCard(
                                        post = post,
                                        onLikeClick = { viewModel.toggleLike(post.id) },
                                        onCommentClick = {
                                            selectedPostForComments = post
                                            viewModel.loadComments(post.id)
                                        },
                                        onAuthorClick = { authorId ->
                                            onOpenProfile(authorId)
                                        },
                                        onReportPostClick = { reportTargetPost = post },
                                        onBlockAuthorClick = { post.authorId?.let { blockTargetUser = Pair(it, post.authorName ?: "Farmer") } }
                                    )

                                    // Infinite Scroll Trigger
                                    if (index >= state.posts.size - 2 && state.hasMore && !state.isLoadingMore) {
                                        LaunchedEffect(Unit) {
                                            viewModel.loadMore()
                                        }
                                    }
                                }

                                // Pagination Loader at bottom
                                if (state.isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    color = ForestGreen,
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Loading more posts...",
                                                    color = MediumGrayText,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create Post Dialog Modal
            if (showCreateDialog) {
                CreatePostDialog(
                    createPostState = createPostState,
                    onDismiss = { showCreateDialog = false },
                    onSubmit = { content, cropTag, location, imageFile, videoFile ->
                        viewModel.createPostWithMedia(
                            content = content,
                            cropTag = cropTag,
                            location = location,
                            imageFile = imageFile,
                            videoFile = videoFile
                        )
                    }
                )
            }

            // Report Post Dialog Modal
            reportTargetPost?.let { postToReport ->
                com.example.farmhelper.ui.community.screens.ReportDialog(
                    targetType = "post",
                    onDismiss = { reportTargetPost = null },
                    onSubmitReport = { reason, description ->
                        viewModel.reportPost(postToReport.id, reason, description) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            reportTargetPost = null
                        }
                    }
                )
            }

            // Block User Confirmation Dialog Modal
            blockTargetUser?.let { (userId, userName) ->
                AlertDialog(
                    onDismissRequest = { blockTargetUser = null },
                    title = { Text("Block $userName?", fontWeight = FontWeight.Bold, color = ForestGreen) },
                    text = { Text("Their posts and comments will be hidden from your community feed.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.blockUser(userId) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    blockTargetUser = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                        ) {
                            Text("Block", color = White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { blockTargetUser = null }) {
                            Text("Cancel", color = MediumGrayText)
                        }
                    },
                    containerColor = WarmWhite,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Comments Bottom Sheet Modal
            selectedPostForComments?.let { targetPost ->
                com.example.farmhelper.ui.community.screens.CommentsBottomSheet(
                    commentsState = commentsState,
                    postAuthorName = targetPost.authorName ?: "Farmer",
                    onDismiss = { selectedPostForComments = null },
                    onSubmitComment = { content, replyingToCommentId ->
                        viewModel.submitComment(targetPost.id, content, replyingToCommentId)
                    },
                    onDeleteComment = { commentId ->
                        viewModel.deleteComment(commentId)
                    },
                    onRetry = {
                        viewModel.loadComments(targetPost.id)
                    }
                )
            }
        }
    }
}
