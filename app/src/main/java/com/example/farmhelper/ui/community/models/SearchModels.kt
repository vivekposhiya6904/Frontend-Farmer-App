package com.example.farmhelper.ui.community.models

import com.google.gson.annotations.SerializedName

data class FarmerSearchItem(
    @SerializedName("_id") val id: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("village") val village: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("total_posts") val totalPosts: Int = 0,
    @SerializedName("total_likes_received") val totalLikesReceived: Int = 0
)

data class SearchPostsResponseData(
    @SerializedName("posts") val posts: List<PostItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("has_more") val hasMore: Boolean
)

data class SearchPostsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: SearchPostsResponseData
)

data class SearchFarmersResponseData(
    @SerializedName("farmers") val farmers: List<FarmerSearchItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("has_more") val hasMore: Boolean
)

data class SearchFarmersResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: SearchFarmersResponseData
)

data class SearchSuggestionsData(
    @SerializedName("suggestions") val suggestions: List<String> = emptyList(),
    @SerializedName("crop_tags") val cropTags: List<String> = emptyList(),
    @SerializedName("locations") val locations: List<String> = emptyList()
)

data class SearchSuggestionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: SearchSuggestionsData
)

data class TrendingCropTag(
    @SerializedName("tag") val tag: String,
    @SerializedName("post_count") val postCount: Int
)

data class TrendingActiveFarmer(
    @SerializedName("user_id") val userId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("village") val village: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("posts_count") val postsCount: Int = 0,
    @SerializedName("likes_count") val likesCount: Int = 0
)

data class TrendingData(
    @SerializedName("trending_tags") val trendingTags: List<TrendingCropTag> = emptyList(),
    @SerializedName("active_farmers") val activeFarmers: List<TrendingActiveFarmer> = emptyList(),
    @SerializedName("recent_posts") val recentPosts: List<PostItem> = emptyList()
)

data class TrendingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TrendingData
)
