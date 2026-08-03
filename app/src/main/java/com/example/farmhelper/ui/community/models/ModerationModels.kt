package com.example.farmhelper.ui.community.models

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("reason") val reason: String,
    @SerializedName("description") val description: String? = null
)

data class ReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("report_id") val reportId: String? = null
)

data class BlockResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class BlockedUserItem(
    @SerializedName("user_id") val userId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("village") val village: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("blocked_at") val blockedAt: String
)

data class BlockedUserListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("blocked_users") val blockedUsers: List<BlockedUserItem>
)
