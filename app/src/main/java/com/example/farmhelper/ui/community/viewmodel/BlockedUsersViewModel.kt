package com.example.farmhelper.ui.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.community.models.BlockedUserItem
import com.example.farmhelper.ui.community.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BlockedUsersUiState {
    object Loading : BlockedUsersUiState
    data class Success(val blockedUsers: List<BlockedUserItem>) : BlockedUsersUiState
    object Empty : BlockedUsersUiState
    data class Error(val message: String) : BlockedUsersUiState
}

class BlockedUsersViewModel(
    private val repository: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockedUsersUiState>(BlockedUsersUiState.Loading)
    val uiState: StateFlow<BlockedUsersUiState> = _uiState.asStateFlow()

    init {
        fetchBlockedUsers()
    }

    fun fetchBlockedUsers() {
        viewModelScope.launch {
            _uiState.value = BlockedUsersUiState.Loading
            val result = repository.getBlockedUsers()
            result.onSuccess { res ->
                if (res.blockedUsers.isEmpty()) {
                    _uiState.value = BlockedUsersUiState.Empty
                } else {
                    _uiState.value = BlockedUsersUiState.Success(res.blockedUsers)
                }
            }.onFailure { err ->
                _uiState.value = BlockedUsersUiState.Error(err.message ?: "Failed to fetch blocked users")
            }
        }
    }

    fun unblockUser(userId: String) {
        val curr = _uiState.value
        if (curr is BlockedUsersUiState.Success) {
            val updated = curr.blockedUsers.filterNot { it.userId == userId }
            if (updated.isEmpty()) {
                _uiState.value = BlockedUsersUiState.Empty
            } else {
                _uiState.value = BlockedUsersUiState.Success(updated)
            }

            viewModelScope.launch {
                repository.unblockUser(userId)
            }
        }
    }
}
