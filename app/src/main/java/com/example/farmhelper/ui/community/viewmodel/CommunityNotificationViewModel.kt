package com.example.farmhelper.ui.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.community.models.CommunityNotificationItem
import com.example.farmhelper.ui.community.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotificationUiState {
    object Loading : NotificationUiState
    data class Success(
        val notifications: List<CommunityNotificationItem>,
        val page: Int,
        val hasMore: Boolean,
        val unreadCount: Int,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val filterUnreadOnly: Boolean = false
    ) : NotificationUiState
    object Empty : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

class CommunityNotificationViewModel(
    private val repository: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var currentFilterUnreadOnly: Boolean = false
    private var currentPage: Int = 1

    init {
        fetchNotifications(page = 1)
        fetchUnreadCount()
    }

    fun fetchNotifications(page: Int = 1, isRefresh: Boolean = false, unreadOnly: Boolean = currentFilterUnreadOnly) {
        currentFilterUnreadOnly = unreadOnly
        currentPage = page

        viewModelScope.launch {
            if (isRefresh) {
                val curr = _uiState.value
                if (curr is NotificationUiState.Success) {
                    _uiState.value = curr.copy(isRefreshing = true)
                }
            } else if (page == 1) {
                _uiState.value = NotificationUiState.Loading
            }

            val result = repository.getNotifications(page = page, limit = 15, unreadOnly = unreadOnly)
            result.onSuccess { res ->
                val data = res.data
                _unreadCount.value = data.unreadCount

                if (data.notifications.isEmpty() && page == 1) {
                    _uiState.value = NotificationUiState.Empty
                } else {
                    val existing = if (page > 1 && _uiState.value is NotificationUiState.Success) {
                        (_uiState.value as NotificationUiState.Success).notifications
                    } else emptyList()

                    val combined = if (page == 1) data.notifications else existing + data.notifications

                    _uiState.value = NotificationUiState.Success(
                        notifications = combined,
                        page = data.page,
                        hasMore = data.hasMore,
                        unreadCount = data.unreadCount,
                        isRefreshing = false,
                        isLoadingMore = false,
                        filterUnreadOnly = unreadOnly
                    )
                }
            }.onFailure { err ->
                _uiState.value = NotificationUiState.Error(err.message ?: "Failed to load notifications")
            }
        }
    }

    fun refreshNotifications() {
        fetchNotifications(page = 1, isRefresh = true)
        fetchUnreadCount()
    }

    fun loadMore() {
        val curr = _uiState.value
        if (curr is NotificationUiState.Success && curr.hasMore && !curr.isLoadingMore) {
            _uiState.value = curr.copy(isLoadingMore = true)
            fetchNotifications(page = curr.page + 1)
        }
    }

    fun toggleUnreadFilter(unreadOnly: Boolean) {
        fetchNotifications(page = 1, unreadOnly = unreadOnly)
    }

    fun markAsRead(notificationId: String) {
        val curr = _uiState.value
        if (curr is NotificationUiState.Success) {
            val updated = curr.notifications.map { item ->
                if (item.id == notificationId) item.copy(isRead = true) else item
            }
            val newUnread = maxOf(0, curr.unreadCount - 1)
            _unreadCount.value = newUnread
            _uiState.value = curr.copy(notifications = updated, unreadCount = newUnread)

            viewModelScope.launch {
                repository.markNotificationAsRead(notificationId)
            }
        }
    }

    fun markAllAsRead() {
        val curr = _uiState.value
        if (curr is NotificationUiState.Success) {
            val updated = curr.notifications.map { it.copy(isRead = true) }
            _unreadCount.value = 0
            _uiState.value = curr.copy(notifications = updated, unreadCount = 0)

            viewModelScope.launch {
                repository.markAllNotificationsAsRead()
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        val curr = _uiState.value
        if (curr is NotificationUiState.Success) {
            val targetItem = curr.notifications.find { it.id == notificationId }
            val updated = curr.notifications.filterNot { it.id == notificationId }
            val newUnread = if (targetItem?.isRead == false) maxOf(0, curr.unreadCount - 1) else curr.unreadCount
            _unreadCount.value = newUnread

            if (updated.isEmpty()) {
                _uiState.value = NotificationUiState.Empty
            } else {
                _uiState.value = curr.copy(notifications = updated, unreadCount = newUnread)
            }

            viewModelScope.launch {
                repository.deleteNotification(notificationId)
            }
        }
    }

    fun fetchUnreadCount() {
        viewModelScope.launch {
            val res = repository.getUnreadNotificationCount()
            res.onSuccess {
                _unreadCount.value = it.unreadCount
            }
        }
    }
}
