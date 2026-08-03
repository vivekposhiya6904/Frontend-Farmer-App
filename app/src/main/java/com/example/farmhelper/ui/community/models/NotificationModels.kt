package com.example.farmhelper.ui.community.models

import com.google.gson.annotations.SerializedName

data class CommunityNotificationItem(
    @SerializedName("_id") val id: String,
    @SerializedName("recipient_user_id") val recipientUserId: String,
    @SerializedName("actor_user_id") val actorUserId: String,
    @SerializedName("actor_name") val actorName: String,
    @SerializedName("actor_avatar") val actorAvatar: String? = null,
    @SerializedName("notification_type") val notificationType: String, // like, comment, reply, post_deleted, system
    @SerializedName("post_id") val postId: String? = null,
    @SerializedName("comment_id") val commentId: String? = null,
    @SerializedName("message") val message: String,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String
)

data class NotificationListResponseData(
    @SerializedName("notifications") val notifications: List<CommunityNotificationItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("has_more") val hasMore: Boolean,
    @SerializedName("unread_count") val unreadCount: Int
)

data class NotificationListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: NotificationListResponseData
)

data class UnreadCountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("unread_count") val unreadCount: Int
)
