package com.example.farmhelper.ui.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.community.models.CommentItem
import com.example.farmhelper.ui.community.models.CreatePostRequest
import com.example.farmhelper.ui.community.models.FarmerProfile
import com.example.farmhelper.ui.community.models.PostItem
import com.example.farmhelper.ui.community.models.UpdateProfileRequest
import com.example.farmhelper.ui.community.repository.CommunityRepository
import com.example.farmhelper.ui.community.screens.CommentsUiState
import com.example.farmhelper.ui.community.screens.ProfileUiState
import com.example.farmhelper.ui.community.screens.UserPostsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface CommunityFeedUiState {
    object Loading : CommunityFeedUiState
    data class Success(
        val posts: List<PostItem>,
        val currentPage: Int,
        val hasMore: Boolean,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val selectedCropTag: String = "All"
    ) : CommunityFeedUiState
    data class Error(val message: String) : CommunityFeedUiState
    data class Empty(val selectedCropTag: String = "All") : CommunityFeedUiState
}

sealed interface CreatePostUiState {
    object Idle : CreatePostUiState
    data class Submitting(val statusText: String = "Publishing post...") : CreatePostUiState
    object Success : CreatePostUiState
    data class Error(val message: String) : CreatePostUiState
}

class CommunityViewModel(
    private val repository: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityFeedUiState>(CommunityFeedUiState.Loading)
    val uiState: StateFlow<CommunityFeedUiState> = _uiState.asStateFlow()

    private val _createPostState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val createPostState: StateFlow<CreatePostUiState> = _createPostState.asStateFlow()

    private val _commentsState = MutableStateFlow<CommentsUiState>(CommentsUiState.Loading)
    val commentsState: StateFlow<CommentsUiState> = _commentsState.asStateFlow()

    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    private val _userPostsUiState = MutableStateFlow<UserPostsUiState>(UserPostsUiState.Loading)
    val userPostsUiState: StateFlow<UserPostsUiState> = _userPostsUiState.asStateFlow()

    private var activeCommentsPostId: String? = null
    private var currentCropTag: String = "All"
    private var currentPage: Int = 1

    init {
        fetchFeed(cropTag = "All", page = 1)
    }

    fun fetchFeed(cropTag: String = currentCropTag, page: Int = 1, isRefresh: Boolean = false) {
        currentCropTag = cropTag
        currentPage = page

        viewModelScope.launch {
            if (isRefresh) {
                val currentState = _uiState.value
                if (currentState is CommunityFeedUiState.Success) {
                    _uiState.value = currentState.copy(isRefreshing = true)
                }
            } else if (page == 1) {
                _uiState.value = CommunityFeedUiState.Loading
            }

            val filterTag = if (cropTag.equals("All", ignoreCase = true)) null else cropTag
            val result = repository.getFeed(page = page, limit = 10, cropTag = filterTag)

            result.onSuccess { response ->
                val feedData = response.data
                val newPosts = feedData.posts

                if (newPosts.isEmpty() && page == 1) {
                    _uiState.value = CommunityFeedUiState.Empty(selectedCropTag = cropTag)
                } else {
                    val existingPosts = if (page > 1 && _uiState.value is CommunityFeedUiState.Success) {
                        (_uiState.value as CommunityFeedUiState.Success).posts
                    } else emptyList()

                    val combinedPosts = if (page == 1) newPosts else existingPosts + newPosts

                    _uiState.value = CommunityFeedUiState.Success(
                        posts = combinedPosts,
                        currentPage = feedData.page,
                        hasMore = feedData.hasMore,
                        isRefreshing = false,
                        isLoadingMore = false,
                        selectedCropTag = cropTag
                    )
                }
            }.onFailure { exception ->
                _uiState.value = CommunityFeedUiState.Error(exception.message ?: "Network error loading feed")
            }
        }
    }

    fun refreshFeed() {
        fetchFeed(cropTag = currentCropTag, page = 1, isRefresh = true)
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState is CommunityFeedUiState.Success && currentState.hasMore && !currentState.isLoadingMore) {
            _uiState.value = currentState.copy(isLoadingMore = true)
            fetchFeed(cropTag = currentCropTag, page = currentState.currentPage + 1)
        }
    }

    fun filterByCropTag(tag: String) {
        if (tag != currentCropTag) {
            fetchFeed(cropTag = tag, page = 1)
        }
    }

    // --- OPTIMISTIC LIKE TOGGLE ---
    fun toggleLike(postId: String) {
        val currentState = _uiState.value
        if (currentState is CommunityFeedUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val currentlyLiked = post.isLiked
                    val newCount = if (currentlyLiked) maxOf(0, post.likesCount - 1) else post.likesCount + 1
                    post.copy(isLiked = !currentlyLiked, likesCount = newCount)
                } else post
            }
            _uiState.value = currentState.copy(posts = updatedPosts)

            viewModelScope.launch {
                val result = repository.toggleLike(postId)
                result.onFailure {
                    fetchFeed(cropTag = currentCropTag, page = 1)
                }
            }
        }
    }

    // --- COMMENTS WORKFLOW ---
    fun loadComments(postId: String) {
        activeCommentsPostId = postId
        _commentsState.value = CommentsUiState.Loading
        viewModelScope.launch {
            val result = repository.getComments(postId)
            result.onSuccess { res ->
                if (res.data.isEmpty()) {
                    _commentsState.value = CommentsUiState.Empty
                } else {
                    _commentsState.value = CommentsUiState.Success(res.data)
                }
            }.onFailure { err ->
                _commentsState.value = CommentsUiState.Error(err.message ?: "Failed to load comments")
            }
        }
    }

    fun submitComment(postId: String, content: String, replyingToCommentId: String? = null) {
        viewModelScope.launch {
            val result = if (replyingToCommentId != null) {
                repository.replyToComment(replyingToCommentId, content)
            } else {
                repository.addComment(postId, content)
            }

            result.onSuccess {
                loadComments(postId)
                incrementPostCommentsCount(postId, 1)
            }.onFailure { err ->
                _commentsState.value = CommentsUiState.Error(err.message ?: "Failed to post comment")
            }
        }
    }

    fun deleteComment(commentId: String) {
        val postId = activeCommentsPostId ?: return
        viewModelScope.launch {
            val result = repository.deleteComment(commentId)
            result.onSuccess {
                loadComments(postId)
                incrementPostCommentsCount(postId, -1)
            }.onFailure { err ->
                _commentsState.value = CommentsUiState.Error(err.message ?: "Failed to delete comment")
            }
        }
    }

    private fun incrementPostCommentsCount(postId: String, delta: Int) {
        val currentState = _uiState.value
        if (currentState is CommunityFeedUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val newCount = maxOf(0, post.commentsCount + delta)
                    post.copy(commentsCount = newCount)
                } else post
            }
            _uiState.value = currentState.copy(posts = updatedPosts)
        }
    }

    // --- PROFILE WORKFLOW ---
    fun fetchProfile(userId: String? = null) {
        _profileUiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            val result = if (userId == null) repository.getMyProfile() else repository.getFarmerProfile(userId)
            result.onSuccess { res ->
                val profile = res.data
                if (profile != null) {
                    _profileUiState.value = ProfileUiState.Success(profile = profile, isOwnProfile = (userId == null))
                } else {
                    _profileUiState.value = ProfileUiState.Error("Profile not found")
                }
            }.onFailure { err ->
                _profileUiState.value = ProfileUiState.Error(err.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(
        bio: String?,
        village: String?,
        district: String?,
        state: String?,
        profileImage: String?,
        coverImage: String?
    ) {
        viewModelScope.launch {
            val req = UpdateProfileRequest(
                bio = bio,
                village = village,
                district = district,
                state = state,
                profileImage = profileImage,
                coverImage = coverImage
            )
            val result = repository.updateProfile(req)
            result.onSuccess { res ->
                res.data?.let { updated ->
                    _profileUiState.value = ProfileUiState.Success(profile = updated, isOwnProfile = true)
                }
                refreshFeed()
            }
        }
    }

    fun fetchUserPosts(userId: String? = null) {
        _userPostsUiState.value = UserPostsUiState.Loading
        viewModelScope.launch {
            val result = if (userId == null) repository.getMyPosts() else repository.getFarmerPosts(userId)
            result.onSuccess { res ->
                val posts = res.data.posts
                if (posts.isEmpty()) {
                    _userPostsUiState.value = UserPostsUiState.Empty
                } else {
                    _userPostsUiState.value = UserPostsUiState.Success(posts)
                }
            }.onFailure { err ->
                _userPostsUiState.value = UserPostsUiState.Error(err.message ?: "Failed to load posts")
            }
        }
    }

    fun softDeletePost(postId: String) {
        viewModelScope.launch {
            val result = repository.deletePost(postId)
            result.onSuccess {
                val currentPosts = (_userPostsUiState.value as? UserPostsUiState.Success)?.posts ?: emptyList()
                val updated = currentPosts.filterNot { it.id == postId }
                if (updated.isEmpty()) {
                    _userPostsUiState.value = UserPostsUiState.Empty
                } else {
                    _userPostsUiState.value = UserPostsUiState.Success(updated)
                }
                refreshFeed()
                fetchProfile(null) // Refresh stats
            }
        }
    }

    fun createPostWithMedia(
        content: String,
        cropTag: String,
        location: String?,
        imageFile: File? = null,
        videoFile: File? = null,
        rawImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _createPostState.value = CreatePostUiState.Submitting("Processing media...")

            var finalImageUrl: String? = rawImageUrl
            var youtubeVideoId: String? = null

            if (imageFile != null) {
                _createPostState.value = CreatePostUiState.Submitting("Uploading photo...")
                val imgResult = repository.uploadImage(imageFile)
                if (imgResult.isSuccess) {
                    finalImageUrl = imgResult.getOrNull()?.data?.imageUrl
                } else {
                    val err = imgResult.exceptionOrNull()?.message ?: "Failed to upload image photo"
                    _createPostState.value = CreatePostUiState.Error(err)
                    return@launch
                }
            }

            if (videoFile != null) {
                _createPostState.value = CreatePostUiState.Submitting("Uploading video to YouTube...")
                val vidResult = repository.uploadVideo(
                    videoFile = videoFile,
                    title = content.take(50),
                    description = content
                )
                if (vidResult.isSuccess) {
                    val vData = vidResult.getOrNull()?.data
                    youtubeVideoId = vData?.youtubeVideoId
                } else {
                    val err = vidResult.exceptionOrNull()?.message ?: "Failed to upload video to YouTube"
                    _createPostState.value = CreatePostUiState.Error(err)
                    return@launch
                }
            }

            _createPostState.value = CreatePostUiState.Submitting("Publishing post...")
            val req = CreatePostRequest(
                content = content,
                cropTag = cropTag,
                location = location,
                imageUrl = finalImageUrl,
                youtubeVideoId = youtubeVideoId
            )

            val result = repository.createPost(req)
            result.onSuccess {
                _createPostState.value = CreatePostUiState.Success
                refreshFeed()
            }.onFailure { error ->
                _createPostState.value = CreatePostUiState.Error(error.message ?: "Failed to post update")
            }
        }
    }

    fun resetCreatePostState() {
        _createPostState.value = CreatePostUiState.Idle
    }

    fun reportPost(postId: String, reason: String, description: String?, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.reportPost(postId, reason, description)
            res.onSuccess {
                onResult(true, it.message)
            }.onFailure {
                onResult(false, it.message ?: "Failed to report post")
            }
        }
    }

    fun reportComment(commentId: String, reason: String, description: String?, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.reportComment(commentId, reason, description)
            res.onSuccess {
                onResult(true, it.message)
            }.onFailure {
                onResult(false, it.message ?: "Failed to report comment")
            }
        }
    }

    fun blockUser(userId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.blockUser(userId)
            res.onSuccess {
                val curr = _uiState.value
                if (curr is CommunityFeedUiState.Success) {
                    val updatedPosts = curr.posts.filterNot { it.authorId == userId }
                    if (updatedPosts.isEmpty()) {
                        _uiState.value = CommunityFeedUiState.Empty(curr.selectedCropTag)
                    } else {
                        _uiState.value = curr.copy(posts = updatedPosts)
                    }
                }
                onResult(true, "User blocked. Their posts have been removed.")
            }.onFailure {
                onResult(false, it.message ?: "Failed to block user")
            }
        }
    }
}
