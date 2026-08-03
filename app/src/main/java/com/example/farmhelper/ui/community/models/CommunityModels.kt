package com.example.farmhelper.ui.community.models

import com.google.gson.annotations.SerializedName

data class PostItem(
    @SerializedName("_id") val id: String,
    @SerializedName("author_id") val authorId: String?,
    @SerializedName("author_name") val authorName: String?,
    @SerializedName("author_role") val authorRole: String?,
    @SerializedName("author_avatar") val authorAvatar: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("crop_tag") val cropTag: String?,
    @SerializedName("content") val content: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("youtube_video_id") val youtubeVideoId: String?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("comments_count") val commentsCount: Int = 0,
    @SerializedName("is_liked_by_me") val isLiked: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class FeedData(
    @SerializedName("posts") val posts: List<PostItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("has_more") val hasMore: Boolean
)

data class FeedResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: FeedData
)

data class CreatePostRequest(
    @SerializedName("content") val content: String,
    @SerializedName("crop_tag") val cropTag: String = "General",
    @SerializedName("location") val location: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("youtube_video_id") val youtubeVideoId: String? = null
)

data class CreatePostResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PostItem?
)

data class ImageUploadData(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("filename") val filename: String? = null
)

data class ImageUploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ImageUploadData
)

data class VideoUploadData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("youtubeVideoId") val youtubeVideoId: String?,
    @SerializedName("youtubeUrl") val youtubeUrl: String?,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?
)

data class VideoUploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: VideoUploadData?
)

data class LikeData(
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("likes_count") val likesCount: Int
)

data class LikeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: LikeData
)

data class CommentItem(
    @SerializedName("_id") val id: String,
    @SerializedName("post_id") val postId: String,
    @SerializedName("parent_comment_id") val parentCommentId: String? = null,
    @SerializedName("author_id") val authorId: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("author_avatar") val authorAvatar: String? = null,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("replies") val replies: List<CommentItem> = emptyList()
)

data class CommentListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<CommentItem>
)

data class CreateCommentRequest(
    @SerializedName("content") val content: String
)

data class CommentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CommentItem?
)

// --- PROFILE SCHEMAS ---

data class ProfileStats(
    @SerializedName("total_posts") val totalPosts: Int = 0,
    @SerializedName("total_likes_received") val totalLikesReceived: Int = 0,
    @SerializedName("total_comments_received") val totalCommentsReceived: Int = 0
)

data class FarmerProfile(
    @SerializedName("_id") val id: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("role") val role: String = "Farmer",
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("village") val village: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("cover_image") val coverImage: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("stats") val stats: ProfileStats = ProfileStats()
)

data class ProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: FarmerProfile?
)

data class UpdateProfileRequest(
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("village") val village: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("cover_image") val coverImage: String? = null
)

data class UserPostsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: FeedData
)
