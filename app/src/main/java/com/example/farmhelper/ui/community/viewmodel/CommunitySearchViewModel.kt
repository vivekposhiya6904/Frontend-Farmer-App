package com.example.farmhelper.ui.community.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.community.models.FarmerSearchItem
import com.example.farmhelper.ui.community.models.PostItem
import com.example.farmhelper.ui.community.models.TrendingData
import com.example.farmhelper.ui.community.repository.CommunityRepository
import com.example.farmhelper.ui.community.utils.RecentSearchManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchFilterState(
    val cropTag: String? = null,
    val district: String? = null,
    val village: String? = null,
    val dateFilter: String? = null, // "today", "week", "month"
    val mediaType: String? = null   // "all", "text", "image", "video"
) {
    val activeFilterCount: Int
        get() = listOfNotNull(
            if (cropTag != null && cropTag != "All") cropTag else null,
            district?.takeIf { it.isNotBlank() },
            village?.takeIf { it.isNotBlank() },
            dateFilter?.takeIf { it.isNotBlank() },
            mediaType?.takeIf { it.isNotBlank() && it != "all" }
        ).size
}

enum class SearchTab {
    ALL,
    POSTS,
    FARMERS
}

sealed interface SearchResultUiState {
    object Idle : SearchResultUiState
    object Loading : SearchResultUiState
    data class Success(
        val posts: List<PostItem>,
        val farmers: List<FarmerSearchItem>,
        val postsHasMore: Boolean = false,
        val farmersHasMore: Boolean = false,
        val postsPage: Int = 1,
        val farmersPage: Int = 1,
        val isLoadingMore: Boolean = false
    ) : SearchResultUiState
    data class Empty(val query: String) : SearchResultUiState
    data class Error(val message: String) : SearchResultUiState
}

sealed interface TrendingUiState {
    object Loading : TrendingUiState
    data class Success(val data: TrendingData) : TrendingUiState
    data class Error(val message: String) : TrendingUiState
}

class CommunitySearchViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CommunityRepository()
    private val recentSearchManager = RecentSearchManager(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(SearchTab.ALL)
    val selectedTab: StateFlow<SearchTab> = _selectedTab.asStateFlow()

    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _searchResultState = MutableStateFlow<SearchResultUiState>(SearchResultUiState.Idle)
    val searchResultState: StateFlow<SearchResultUiState> = _searchResultState.asStateFlow()

    private val _trendingState = MutableStateFlow<TrendingUiState>(TrendingUiState.Loading)
    val trendingState: StateFlow<TrendingUiState> = _trendingState.asStateFlow()

    private var suggestionJob: Job? = null

    init {
        loadRecentSearches()
        fetchTrending()
    }

    fun loadRecentSearches() {
        _recentSearches.value = recentSearchManager.getRecentSearches()
    }

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery

        if (newQuery.isBlank()) {
            _suggestions.value = emptyList()
            _searchResultState.value = SearchResultUiState.Idle
            return
        }

        // Debounced live suggestion fetch
        suggestionJob?.cancel()
        suggestionJob = viewModelScope.launch {
            delay(250) // 250ms debounce
            val result = repository.getSearchSuggestions(newQuery)
            result.onSuccess { res ->
                _suggestions.value = res.data.suggestions
            }
        }
    }

    fun onSearchSubmitted(query: String = _searchQuery.value) {
        val trimmed = query.trim()
        if (trimmed.isEmpty() && _filterState.value.activeFilterCount == 0) return

        if (trimmed.isNotEmpty()) {
            recentSearchManager.addRecentSearch(trimmed)
            loadRecentSearches()
        }

        _suggestions.value = emptyList()
        executeSearch(query = trimmed, page = 1)
    }

    fun selectTab(tab: SearchTab) {
        _selectedTab.value = tab
    }

    fun updateFilters(newFilter: SearchFilterState) {
        _filterState.value = newFilter
        if (_searchQuery.value.isNotBlank() || newFilter.activeFilterCount > 0) {
            executeSearch(query = _searchQuery.value, page = 1)
        }
    }

    fun clearFilters() {
        _filterState.value = SearchFilterState()
        if (_searchQuery.value.isNotBlank()) {
            executeSearch(query = _searchQuery.value, page = 1)
        } else {
            _searchResultState.value = SearchResultUiState.Idle
        }
    }

    fun executeSearch(query: String = _searchQuery.value, page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _searchResultState.value = SearchResultUiState.Loading
            }

            val filter = _filterState.value
            val postsResult = repository.searchPosts(
                query = query.ifBlank { null },
                cropTag = filter.cropTag,
                district = filter.district,
                village = filter.village,
                dateFilter = filter.dateFilter,
                mediaType = filter.mediaType,
                page = page,
                limit = 10
            )

            val farmersResult = repository.searchFarmers(
                query = query.ifBlank { null },
                village = filter.village,
                district = filter.district,
                page = page,
                limit = 10
            )

            val posts = postsResult.getOrNull()?.data?.posts ?: emptyList()
            val postsHasMore = postsResult.getOrNull()?.data?.hasMore ?: false

            val farmers = farmersResult.getOrNull()?.data?.farmers ?: emptyList()
            val farmersHasMore = farmersResult.getOrNull()?.data?.hasMore ?: false

            if (posts.isEmpty() && farmers.isEmpty()) {
                _searchResultState.value = SearchResultUiState.Empty(query = query)
            } else {
                _searchResultState.value = SearchResultUiState.Success(
                    posts = posts,
                    farmers = farmers,
                    postsHasMore = postsHasMore,
                    farmersHasMore = farmersHasMore,
                    postsPage = page,
                    farmersPage = page
                )
            }
        }
    }

    fun fetchTrending() {
        _trendingState.value = TrendingUiState.Loading
        viewModelScope.launch {
            val result = repository.getTrendingData()
            result.onSuccess { res ->
                _trendingState.value = TrendingUiState.Success(res.data)
            }.onFailure { err ->
                _trendingState.value = TrendingUiState.Error(err.message ?: "Failed to load trending items")
            }
        }
    }

    fun deleteRecentSearch(query: String) {
        recentSearchManager.deleteRecentSearch(query)
        loadRecentSearches()
    }

    fun clearAllRecentSearches() {
        recentSearchManager.clearAllRecentSearches()
        loadRecentSearches()
    }
}
