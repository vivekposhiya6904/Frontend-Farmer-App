package com.example.farmhelper.ui.community.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.farmhelper.ui.community.models.FarmerProfile
import com.example.farmhelper.ui.community.models.PostItem
import com.example.farmhelper.ui.community.viewmodel.CommunityViewModel
import com.example.farmhelper.ui.home.components.CommunityItemCard
import com.example.farmhelper.ui.theme.*

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val profile: FarmerProfile, val isOwnProfile: Boolean) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface UserPostsUiState {
    object Loading : UserPostsUiState
    data class Success(val posts: List<PostItem>) : UserPostsUiState
    data class Error(val message: String) : UserPostsUiState
    object Empty : UserPostsUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProfileScreen(
    targetUserId: String? = null, // null means "My Profile"
    onBackClick: () -> Unit,
    onOpenBlockedUsers: () -> Unit = {},
    viewModel: CommunityViewModel
) {
    val context = LocalContext.current
    val profileUiState by viewModel.profileUiState.collectAsState()
    val userPostsUiState by viewModel.userPostsUiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<PostItem?>(null) }

    LaunchedEffect(targetUserId) {
        viewModel.fetchProfile(targetUserId)
        viewModel.fetchUserPosts(targetUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (val state = profileUiState) {
                        is ProfileUiState.Success -> if (state.isOwnProfile) "My Profile" else "${state.profile.fullName}'s Profile"
                        else -> "Farmer Profile"
                    }
                    Text(
                        text = titleText,
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
                    val pState = profileUiState
                    if (pState is ProfileUiState.Success && pState.isOwnProfile) {
                        IconButton(onClick = onOpenBlockedUsers) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOff,
                                contentDescription = "Blocked Farmers",
                                tint = ForestGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
            )
        },
        containerColor = SoftOlive.copy(alpha = 0.3f)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val pState = profileUiState) {
                is ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ForestGreen)
                    }
                }
                is ProfileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error: ${pState.message}", color = AlertRed)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.fetchProfile(targetUserId)
                                    viewModel.fetchUserPosts(targetUserId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Text("Retry", color = White)
                            }
                        }
                    }
                }
                is ProfileUiState.Success -> {
                    val profile = pState.profile
                    val isOwn = pState.isOwnProfile

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Cover & Profile Header Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    // Cover Banner
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .background(ForestGreen.copy(alpha = 0.85f))
                                    ) {
                                        if (!profile.coverImage.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = profile.coverImage,
                                                contentDescription = "Cover Image",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    // Avatar & Details
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            // Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .offset(y = (-36).dp)
                                                    .clip(CircleShape)
                                                    .background(SoftOlive)
                                                    .border(3.dp, WarmWhite, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!profile.profileImage.isNullOrEmpty()) {
                                                    AsyncImage(
                                                        model = profile.profileImage,
                                                        contentDescription = "Avatar",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    val initials = profile.fullName.trim().split(" ")
                                                        .mapNotNull { it.firstOrNull()?.uppercase() }
                                                        .take(2)
                                                        .joinToString("")
                                                    Text(
                                                        text = initials.ifEmpty { "F" },
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            color = ForestGreen,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }

                                            // Edit Button (if own profile)
                                            if (isOwn) {
                                                Button(
                                                    onClick = { showEditDialog = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SoftOlive),
                                                    shape = RoundedCornerShape(14.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null,
                                                        tint = ForestGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Edit Profile",
                                                        color = ForestGreen,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height((-20).dp))

                                        // Farmer Name & Location
                                        Text(
                                            text = profile.fullName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DarkGrayText,
                                                fontSize = 20.sp
                                            )
                                        )

                                        val locationParts = listOfNotNull(profile.village, profile.district, profile.state)
                                        val fullLocation = if (locationParts.isNotEmpty()) locationParts.joinToString(", ") else "Farmer Community Member"

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = MediumGrayText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = fullLocation,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MediumGrayText,
                                                    fontSize = 13.sp
                                                )
                                            )
                                        }

                                        // Bio
                                        profile.bio?.let { bioText ->
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = bioText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = DarkGrayText,
                                                    fontSize = 14.sp,
                                                    lineHeight = 20.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Statistics Cards Row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    title = "Posts",
                                    value = "${profile.stats.totalPosts}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Likes",
                                    value = "${profile.stats.totalLikesReceived}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Comments",
                                    value = "${profile.stats.totalCommentsReceived}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Posts Header
                        item {
                            Text(
                                text = "Farmer Posts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGrayText,
                                    fontSize = 17.sp
                                ),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }

                        // My / Farmer Posts List
                        when (val postsState = userPostsUiState) {
                            is UserPostsUiState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = ForestGreen)
                                    }
                                }
                            }
                            is UserPostsUiState.Empty -> {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = WarmWhite)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No posts published yet.",
                                                color = MediumGrayText,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                            is UserPostsUiState.Success -> {
                                items(postsState.posts) { post ->
                                    CommunityItemCard(
                                        post = post,
                                        onLikeClick = { viewModel.toggleLike(post.id) },
                                        onCommentClick = { viewModel.loadComments(post.id) },
                                        onDeletePostClick = if (isOwn) { { postToDelete = post } } else null
                                    )
                                }
                            }
                            is UserPostsUiState.Error -> {
                                item {
                                    Text(
                                        text = "Failed to load posts: ${postsState.message}",
                                        color = AlertRed,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Edit Profile Modal Dialog
                    if (showEditDialog) {
                        EditProfileDialog(
                            currentProfile = profile,
                            onDismiss = { showEditDialog = false },
                            onSave = { bio, village, district, state, profileImage, coverImage ->
                                viewModel.updateProfile(bio, village, district, state, profileImage, coverImage)
                                showEditDialog = false
                            }
                        )
                    }

                    // Delete Post Confirmation Dialog
                    postToDelete?.let { target ->
                        AlertDialog(
                            onDismissRequest = { postToDelete = null },
                            title = { Text("Delete Post", fontWeight = FontWeight.Bold, color = AlertRed) },
                            text = { Text("Are you sure you want to delete this post? It will be removed from your profile and the community feed.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.softDeletePost(target.id)
                                        postToDelete = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                                ) {
                                    Text("Delete", color = White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { postToDelete = null }) {
                                    Text("Cancel", color = MediumGrayText)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = WarmWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MediumGrayText,
                    fontSize = 11.sp
                )
            )
        }
    }
}
