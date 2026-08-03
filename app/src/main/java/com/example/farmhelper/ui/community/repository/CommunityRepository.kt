package com.example.farmhelper.ui.community.repository

import com.example.farmhelper.api.NetworkErrorHandler
import com.example.farmhelper.api.RetrofitClient
import com.example.farmhelper.ui.community.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class CommunityRepository {

    suspend fun getFeed(page: Int = 1, limit: Int = 10, cropTag: String? = null): Result<FeedResponse> {
        return try {
            val response: Response<FeedResponse> = RetrofitClient.apiServices.getCommunityFeed(page, limit, cropTag)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun createPost(request: CreatePostRequest): Result<CreatePostResponse> {
        return try {
            val response: Response<CreatePostResponse> = RetrofitClient.apiServices.createCommunityPost(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun uploadImage(imageFile: File): Result<ImageUploadResponse> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val response = RetrofitClient.apiServices.uploadCommunityImage(body)
            val resBody = response.body()
            if (response.isSuccessful && resBody != null) {
                Result.success(resBody)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun uploadVideo(videoFile: File, title: String, description: String? = null): Result<VideoUploadResponse> {
        return try {
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val requestFile = videoFile.asRequestBody("video/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", videoFile.name, requestFile)
            val response = RetrofitClient.apiServices.uploadCommunityVideo(titleBody, descBody, body)
            val resBody = response.body()
            if (response.isSuccessful && resBody != null) {
                Result.success(resBody)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun toggleLike(postId: String): Result<LikeResponse> {
        return try {
            val response = RetrofitClient.apiServices.togglePostLike(postId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getComments(postId: String): Result<CommentListResponse> {
        return try {
            val response = RetrofitClient.apiServices.getPostComments(postId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun addComment(postId: String, content: String): Result<CommentResponse> {
        return try {
            val req = CreateCommentRequest(content = content)
            val response = RetrofitClient.apiServices.addPostComment(postId, req)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun replyToComment(commentId: String, content: String): Result<CommentResponse> {
        return try {
            val req = CreateCommentRequest(content = content)
            val response = RetrofitClient.apiServices.replyToComment(commentId, req)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun deleteComment(commentId: String): Result<Boolean> {
        return try {
            val response = RetrofitClient.apiServices.deleteComment(commentId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getMyProfile(): Result<ProfileResponse> {
        return try {
            val response = RetrofitClient.apiServices.getMyProfile()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getFarmerProfile(userId: String): Result<ProfileResponse> {
        return try {
            val response = RetrofitClient.apiServices.getFarmerProfile(userId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getMyPosts(page: Int = 1, limit: Int = 10): Result<UserPostsResponse> {
        return try {
            val response = RetrofitClient.apiServices.getMyPosts(page, limit)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getFarmerPosts(userId: String, page: Int = 1, limit: Int = 10): Result<UserPostsResponse> {
        return try {
            val response = RetrofitClient.apiServices.getFarmerPosts(userId, page, limit)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<ProfileResponse> {
        return try {
            val response = RetrofitClient.apiServices.updateFarmerProfile(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun deletePost(postId: String): Result<Boolean> {
        return try {
            val response = RetrofitClient.apiServices.deleteCommunityPost(postId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun searchPosts(
        query: String? = null,
        cropTag: String? = null,
        district: String? = null,
        village: String? = null,
        dateFilter: String? = null,
        mediaType: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<SearchPostsResponse> {
        return try {
            val response = RetrofitClient.apiServices.searchPosts(
                query = query,
                cropTag = cropTag,
                district = district,
                village = village,
                dateFilter = dateFilter,
                mediaType = mediaType,
                page = page,
                limit = limit
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun searchFarmers(
        query: String? = null,
        village: String? = null,
        district: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<SearchFarmersResponse> {
        return try {
            val response = RetrofitClient.apiServices.searchFarmers(
                query = query,
                village = village,
                district = district,
                page = page,
                limit = limit
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getSearchSuggestions(query: String): Result<SearchSuggestionsResponse> {
        return try {
            val response = RetrofitClient.apiServices.getSearchSuggestions(query)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getTrendingData(): Result<TrendingResponse> {
        return try {
            val response = RetrofitClient.apiServices.getTrendingData()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getNotifications(page: Int = 1, limit: Int = 10, unreadOnly: Boolean = false): Result<NotificationListResponse> {
        return try {
            val response = RetrofitClient.apiServices.getNotifications(page, limit, unreadOnly)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getUnreadNotificationCount(): Result<UnreadCountResponse> {
        return try {
            val response = RetrofitClient.apiServices.getUnreadNotificationCount()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Result<Boolean> {
        return try {
            val response = RetrofitClient.apiServices.markNotificationAsRead(notificationId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun markAllNotificationsAsRead(): Result<Boolean> {
        return try {
            val response = RetrofitClient.apiServices.markAllNotificationsAsRead()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Boolean> {
        return try {
            val response = RetrofitClient.apiServices.deleteNotification(notificationId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun reportPost(postId: String, reason: String, description: String? = null): Result<ReportResponse> {
        return try {
            val body = ReportRequest(reason, description)
            val response = RetrofitClient.apiServices.reportPost(postId, body)
            val resBody = response.body()
            if (response.isSuccessful && resBody != null) {
                Result.success(resBody)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun reportComment(commentId: String, reason: String, description: String? = null): Result<ReportResponse> {
        return try {
            val body = ReportRequest(reason, description)
            val response = RetrofitClient.apiServices.reportComment(commentId, body)
            val resBody = response.body()
            if (response.isSuccessful && resBody != null) {
                Result.success(resBody)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun blockUser(userId: String): Result<BlockResponse> {
        return try {
            val response = RetrofitClient.apiServices.blockUser(userId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun unblockUser(userId: String): Result<BlockResponse> {
        return try {
            val response = RetrofitClient.apiServices.unblockUser(userId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }

    suspend fun getBlockedUsers(): Result<BlockedUserListResponse> {
        return try {
            val response = RetrofitClient.apiServices.getBlockedUsers()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(NetworkErrorHandler.parseErrorResponse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e)))
        }
    }
}
