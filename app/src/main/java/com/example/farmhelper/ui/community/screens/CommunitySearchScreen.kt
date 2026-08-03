package com.example.farmhelper.ui.community.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.farmhelper.ui.community.models.FarmerSearchItem
import com.example.farmhelper.ui.community.models.PostItem
import com.example.farmhelper.ui.community.viewmodel.*
import com.example.farmhelper.ui.home.components.CommunityItemCard
import com.example.farmhelper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitySearchScreen(
    onBackClick: () -> Unit,
    onOpenProfile: (userId: String) -> Unit,
    onOpenPostComments: (post: PostItem) -> Unit = {},
    viewModel: CommunitySearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val query by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val searchResultState by viewModel.searchResultState.collectAsState()
    val trendingState by viewModel.trendingState.collectAsState()

    var showFilterBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = WarmWhite,
                tonalElevation = 2.dp,
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(bottom = 8.dp)) {
                    // 1. Search Bar Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ForestGreen
                            )
                        }

                        TextField(
                            value = query,
                            onValueChange = { viewModel.onQueryChanged(it) },
                            placeholder = {
                                Text(
                                    text = "Search posts, crops, farmers, villages...",
                                    fontSize = 13.sp,
                                    color = MediumGrayText
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ForestGreen
                                )
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = MediumGrayText
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                                viewModel.onSearchSubmitted()
                            }),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SoftOlive,
                                unfocusedContainerColor = SoftOlive,
                                disabledContainerColor = SoftOlive,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        )

                        IconButton(onClick = { showFilterBottomSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (filterState.activeFilterCount > 0) {
                                        Badge(containerColor = AlertRed, contentColor = White) {
                                            Text("${filterState.activeFilterCount}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = "Filter",
                                    tint = ForestGreen
                                )
                            }
                        }
                    }

                    // 2. Quick Filter & Search Result Tabs Bar
                    if (searchResultState is SearchResultUiState.Success || searchResultState is SearchResultUiState.Loading || searchResultState is SearchResultUiState.Empty) {
                        TabRow(
                            selectedTabIndex = selectedTab.ordinal,
                            containerColor = WarmWhite,
                            contentColor = ForestGreen
                        ) {
                            Tab(
                                selected = selectedTab == SearchTab.ALL,
                                onClick = { viewModel.selectTab(SearchTab.ALL) },
                                text = { Text("All", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedTab == SearchTab.POSTS,
                                onClick = { viewModel.selectTab(SearchTab.POSTS) },
                                text = { Text("Posts", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedTab == SearchTab.FARMERS,
                                onClick = { viewModel.selectTab(SearchTab.FARMERS) },
                                text = { Text("Farmers", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        },
        containerColor = WarmBeige
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Content Area
            if (query.isNotEmpty() && suggestions.isNotEmpty() && searchResultState is SearchResultUiState.Idle) {
                // Live Suggestions List Overlay
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(WarmWhite)
                ) {
                    item {
                        Text(
                            text = "Suggestions",
                            fontWeight = FontWeight.Bold,
                            color = MediumGrayText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(suggestions) { suggestionText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    keyboardController?.hide()
                                    viewModel.onQueryChanged(suggestionText)
                                    viewModel.onSearchSubmitted(suggestionText)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MediumGrayText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = suggestionText,
                                fontSize = 14.sp,
                                color = DarkGrayText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider(color = ForestGreen.copy(alpha = 0.08f))
                    }
                }
            } else if (searchResultState is SearchResultUiState.Idle && query.isEmpty()) {
                // Default Discovery View (Recent Searches + Trending Section)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Recent Searches
                    if (recentSearches.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Recent Searches",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ForestGreen
                                        )
                                        TextButton(onClick = { viewModel.clearAllRecentSearches() }) {
                                            Text("Clear All", fontSize = 12.sp, color = AlertRed)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        recentSearches.forEach { searchItem ->
                                            InputChip(
                                                selected = false,
                                                onClick = {
                                                    viewModel.onQueryChanged(searchItem)
                                                    viewModel.onSearchSubmitted(searchItem)
                                                },
                                                label = { Text(searchItem, fontSize = 13.sp) },
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove",
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { viewModel.deleteRecentSearch(searchItem) }
                                                    )
                                                },
                                                colors = InputChipDefaults.inputChipColors(
                                                    containerColor = SoftOlive,
                                                    labelColor = DarkGrayText
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Trending Section
                    when (val trend = trendingState) {
                        is TrendingUiState.Loading -> {
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

                        is TrendingUiState.Success -> {
                            val data = trend.data

                            // Trending Tags
                            if (data.trendingTags.isNotEmpty()) {
                                item {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "🔥 Trending Crop Tags",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = DarkGrayText,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                        )

                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(data.trendingTags) { tagItem ->
                                                SuggestionChip(
                                                    onClick = {
                                                        viewModel.updateFilters(filterState.copy(cropTag = tagItem.tag))
                                                        viewModel.onSearchSubmitted(tagItem.tag)
                                                    },
                                                    label = {
                                                        Text("#${tagItem.tag} (${tagItem.postCount})", fontWeight = FontWeight.Bold)
                                                    },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = WarmWhite,
                                                        labelColor = ForestGreen
                                                    ),
                                                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f)),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Most Active Farmers
                            if (data.activeFarmers.isNotEmpty()) {
                                item {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "🌟 Most Active Farmers",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = DarkGrayText,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                        )

                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(data.activeFarmers) { farmer ->
                                                Card(
                                                    modifier = Modifier
                                                        .width(160.dp)
                                                        .clickable { onOpenProfile(farmer.userId) },
                                                    shape = RoundedCornerShape(18.dp),
                                                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                                                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f)),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(54.dp)
                                                                .clip(CircleShape)
                                                                .background(SoftOlive),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (!farmer.profileImage.isNullOrEmpty()) {
                                                                AsyncImage(
                                                                    model = farmer.profileImage,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                            } else {
                                                                Text(
                                                                    text = farmer.fullName.take(1).uppercase(),
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = ForestGreen,
                                                                    fontSize = 18.sp
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Text(
                                                            text = farmer.fullName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = DarkGrayText,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1
                                                        )

                                                        Text(
                                                            text = farmer.district ?: "Farmer",
                                                            fontSize = 11.sp,
                                                            color = MediumGrayText,
                                                            maxLines = 1
                                                        )

                                                        Spacer(modifier = Modifier.height(6.dp))

                                                        Text(
                                                            text = "${farmer.postsCount} posts",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = ForestGreen
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Recent Posts
                            if (data.recentPosts.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "📢 Recent Discussions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = DarkGrayText,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                    )
                                }

                                items(data.recentPosts) { post ->
                                    CommunityItemCard(
                                        post = post,
                                        onAuthorClick = { authorId -> onOpenProfile(authorId) }
                                    )
                                }
                            }
                        }

                        is TrendingUiState.Error -> {
                            item {
                                Text(
                                    text = "Failed to load trending items",
                                    color = AlertRed,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Search Results Area
                when (val state = searchResultState) {
                    is SearchResultUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ForestGreen)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Searching community...", color = ForestGreen, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    is SearchResultUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Outlined.SearchOff, contentDescription = null, tint = MediumGrayText, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No results found for '${state.query}'", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkGrayText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Try searching with a different word or clearing your filters.", color = MediumGrayText, fontSize = 13.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.clearFilters() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                ) {
                                    Text("Clear Filters", color = White)
                                }
                            }
                        }
                    }

                    is SearchResultUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Search error: ${state.message}", color = AlertRed, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.executeSearch() }, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) {
                                    Text("Retry", color = White)
                                }
                            }
                        }
                    }

                    is SearchResultUiState.Success -> {
                        val pullRefreshState = rememberPullToRefreshState()

                        PullToRefreshBox(
                            isRefreshing = false,
                            onRefresh = { viewModel.executeSearch() },
                            state = pullRefreshState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Render Farmers (if ALL or FARMERS tab selected)
                                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.FARMERS) && state.farmers.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Farmers (${state.farmers.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ForestGreen,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                        )
                                    }

                                    items(state.farmers) { farmer ->
                                        FarmerSearchResultCard(
                                            farmer = farmer,
                                            onClick = { onOpenProfile(farmer.id) }
                                        )
                                    }
                                }

                                // Render Posts (if ALL or POSTS tab selected)
                                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.POSTS) && state.posts.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Community Posts (${state.posts.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ForestGreen,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                        )
                                    }

                                    items(state.posts) { post ->
                                        CommunityItemCard(
                                            post = post,
                                            onAuthorClick = { authorId -> onOpenProfile(authorId) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

            // Filter Bottom Sheet Modal
            if (showFilterBottomSheet) {
                FilterBottomSheet(
                    currentFilter = filterState,
                    onDismiss = { showFilterBottomSheet = false },
                    onApply = { newFilter ->
                        viewModel.updateFilters(newFilter)
                        showFilterBottomSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun FarmerSearchResultCard(
    farmer: FarmerSearchItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SoftOlive),
                contentAlignment = Alignment.Center
            ) {
                if (!farmer.profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = farmer.profileImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = farmer.fullName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = farmer.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DarkGrayText
                )

                val loc = listOfNotNull(farmer.village, farmer.district).joinToString(", ")
                if (loc.isNotEmpty()) {
                    Text(text = loc, fontSize = 12.sp, color = MediumGrayText)
                }

                Text(
                    text = "${farmer.totalPosts} posts • ${farmer.totalLikesReceived} likes",
                    fontSize = 11.sp,
                    color = ForestGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MediumGrayText
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: SearchFilterState,
    onDismiss: () -> Unit,
    onApply: (SearchFilterState) -> Unit
) {
    val cropTags = listOf("All", "Cotton", "Wheat", "Groundnut", "Rice", "Vegetables", "Bajra")
    val dateOptions = listOf("any", "today", "week", "month")
    val mediaOptions = listOf("all", "text", "image", "video")

    var selectedCrop by remember { mutableStateOf(currentFilter.cropTag ?: "All") }
    var districtText by remember { mutableStateOf(currentFilter.district ?: "") }
    var villageText by remember { mutableStateOf(currentFilter.village ?: "") }
    var selectedDate by remember { mutableStateOf(currentFilter.dateFilter ?: "any") }
    var selectedMedia by remember { mutableStateOf(currentFilter.mediaType ?: "all") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WarmWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Search Filters", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ForestGreen)
                TextButton(onClick = {
                    selectedCrop = "All"
                    districtText = ""
                    villageText = ""
                    selectedDate = "any"
                    selectedMedia = "all"
                }) {
                    Text("Reset", color = AlertRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Crop Filter
            Text("Crop Category", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGrayText)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cropTags.forEach { tag ->
                    FilterChip(
                        selected = selectedCrop == tag,
                        onClick = { selectedCrop = tag },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = White,
                            containerColor = SoftOlive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location Filters
            Text("Location Filters", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGrayText)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = districtText,
                    onValueChange = { districtText = it },
                    label = { Text("District") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = villageText,
                    onValueChange = { villageText = it },
                    label = { Text("Village") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Range
            Text("Date Posted", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGrayText)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                dateOptions.forEach { opt ->
                    FilterChip(
                        selected = selectedDate == opt,
                        onClick = { selectedDate = opt },
                        label = { Text(opt.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = White,
                            containerColor = SoftOlive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Media Type
            Text("Media Type", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGrayText)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                mediaOptions.forEach { opt ->
                    FilterChip(
                        selected = selectedMedia == opt,
                        onClick = { selectedMedia = opt },
                        label = { Text(opt.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = White,
                            containerColor = SoftOlive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onApply(
                        SearchFilterState(
                            cropTag = if (selectedCrop != "All") selectedCrop else null,
                            district = districtText.ifBlank { null },
                            village = villageText.ifBlank { null },
                            dateFilter = if (selectedDate != "any") selectedDate else null,
                            mediaType = if (selectedMedia != "all") selectedMedia else null
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Apply Filters", fontWeight = FontWeight.Bold, color = White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
