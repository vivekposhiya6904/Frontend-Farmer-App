package com.example.farmhelper.ui.market

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmhelper.R
import com.example.farmhelper.ui.localization.LanguageManager
import com.example.farmhelper.ui.market.models.*
import com.example.farmhelper.ui.market.viewmodel.*
import com.example.farmhelper.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val insightsState by viewModel.insightsState.collectAsState()
    val topMarketsState by viewModel.topMarketsState.collectAsState()
    val favoritesState by viewModel.favoritesState.collectAsState()
    val subscriptionsState by viewModel.subscriptionsState.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedCommodity by viewModel.selectedCommodity.collectAsState()
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val isVoiceListening by viewModel.isVoiceListening.collectAsState()

    var showSearchSuggestions by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showSubscriptionsSheet by remember { mutableStateOf(false) }
    var alertCommodityTarget by remember { mutableStateOf<Pair<String, Double>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.selectCurrentLocation(context)
        } else {
            Toast.makeText(context, context.getString(R.string.gps_permission_denied_message), Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBeige)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Premium Search Header
            MarketSearchHeader(
                searchQuery = searchQuery,
                onQueryChange = {
                    viewModel.searchQuery.value = it
                    showSearchSuggestions = it.isNotEmpty()
                    viewModel.searchOrFilter()
                },
                onSearchFocus = { showSearchSuggestions = true },
                onVoiceClick = { viewModel.startVoiceListening() },
                onClearClick = {
                    viewModel.searchQuery.value = ""
                    showSearchSuggestions = false
                    viewModel.searchOrFilter()
                },
                onAlertsClick = {
                    viewModel.fetchSubscriptions()
                    showSubscriptionsSheet = true
                }
            )

            // 2. Cascade Filter Dropdowns
            FilterDropdownBar(
                selectedDistrict = selectedDistrict,
                selectedCommodity = selectedCommodity,
                selectedMarket = selectedMarket,
                districtsList = (uiState as? MarketUiState.Success)?.districts ?: emptyList(),
                commoditiesList = (uiState as? MarketUiState.Success)?.commodities ?: emptyList(),
                marketsList = (uiState as? MarketUiState.Success)?.markets ?: emptyList(),
                onDistrictSelected = { viewModel.onDistrictSelected(it) },
                onCommoditySelected = { viewModel.onCommoditySelected(it) },
                onMarketSelected = { viewModel.onMarketSelected(it) },
                onClearAll = { viewModel.clearFilters() }
            )

            // 3. Screen content depending on state
            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    is MarketUiState.Loading -> {
                        MarketShimmerLoader()
                    }
                    is MarketUiState.Error -> {
                        val errMsg = (uiState as MarketUiState.Error).message
                        val isNoInternet = errMsg.contains("No internet connection", ignoreCase = true) ||
                                           errMsg.contains("UnknownHostException", ignoreCase = true) ||
                                           errMsg.contains("ConnectException", ignoreCase = true)
                        if (isNoInternet) {
                            MarketOfflineCard(
                                onRetry = { viewModel.fetchInitialData() }
                            )
                        } else {
                            MarketErrorCard(
                                message = errMsg,
                                onRetry = { viewModel.fetchInitialData() }
                            )
                        }
                    }
                    is MarketUiState.Success -> {
                        val successState = uiState as MarketUiState.Success

                        if (showSearchSuggestions && searchQuery.isEmpty()) {
                            // Search suggestions / recent search overlay
                            RecentSearchList(
                                recentSearches = recentSearches,
                                onSearchSelected = {
                                    viewModel.searchQuery.value = it
                                    showSearchSuggestions = false
                                    viewModel.searchOrFilter()
                                },
                                onClearHistory = { viewModel.clearRecentSearches() },
                                onUseCurrentLocationClick = {
                                    showSearchSuggestions = false
                                    checkAndRequestLocationPermission(
                                        context = context,
                                        permissionLauncher = permissionLauncher,
                                        onRationaleRequired = { showPermissionRationaleDialog = true },
                                        onAlreadyGranted = { viewModel.selectCurrentLocation(context) }
                                    )
                                }
                            )
                        } else {
                            var isRefreshing by remember { mutableStateOf(false) }
                            val pullRefreshState = rememberPullToRefreshState()

                            LaunchedEffect(uiState) {
                                isRefreshing = false
                            }

                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = {
                                    isRefreshing = true
                                    viewModel.fetchInitialData()
                                },
                                state = pullRefreshState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // A. Popular Crops Quick Selector
                                    item {
                                        PopularCropsRow(
                                            commodities = successState.commodities,
                                            selectedCommodity = successState.commodities.find { it.equals(viewModel.activeCommodity.value, ignoreCase = true) } ?: "",
                                            onSelected = {
                                                viewModel.selectActiveCropAndDistrict(it, viewModel.activeDistrict.value)
                                            }
                                        )
                                    }

                                    // B. Crop Price Trends Graph Section
                                    item {
                                        MarketTrendChartCard(
                                            commodityName = viewModel.activeCommodity.value,
                                            districtName = viewModel.activeDistrict.value,
                                            historyState = historyState,
                                            selectedDays = selectedDays,
                                            onDaysSelected = { viewModel.setDays(it) }
                                        )
                                    }

                                    // C. Selling Insights Card
                                    item {
                                        MarketInsightsCard(
                                            insightsState = insightsState
                                        )
                                    }

                                    // C1. Top Mandis Ranking Card
                                    item {
                                        TopMarketsCard(
                                            topMarketsState = topMarketsState,
                                            commodity = viewModel.activeCommodity.value
                                        )
                                    }

                                    // C2. Government MSP Card
                                    val avgPrice = (insightsState as? InsightsUiState.Success)?.insights?.trends?.statistics_30_days?.average_price
                                        ?: successState.prices.filter { it.commodity.equals(viewModel.activeCommodity.value, ignoreCase = true) }.map { it.modal_price }.average().takeIf { !it.isNaN() }
                                        ?: 0.0
                                    if (avgPrice > 0.0) {
                                        item {
                                            GovernmentMspCard(
                                                commodityName = viewModel.activeCommodity.value,
                                                prevailingAvgPrice = avgPrice
                                            )
                                        }
                                    }

                                    // D. Detailed Crop Prices List Header
                                    item {
                                        Text(
                                            text = stringResource(id = R.string.trending_crops),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreen
                                            ),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                        )
                                    }

                                    // E. Crop Price Items
                                    if (successState.prices.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.no_market_data),
                                                    style = MaterialTheme.typography.bodyMedium.copy(color = MediumGrayText),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        items(successState.prices) { priceItem ->
                                            val isFav = favoritesState.any { it.equals(priceItem.commodity, ignoreCase = true) }
                                            CropRateCard(
                                                priceItem = priceItem,
                                                isFavorite = isFav,
                                                onFavoriteToggle = { viewModel.toggleFavorite(priceItem.commodity) },
                                                onSetAlertClick = { alertCommodityTarget = Pair(priceItem.commodity, priceItem.modal_price) },
                                                onClick = {
                                                    viewModel.selectActiveCropAndDistrict(priceItem.commodity, priceItem.district)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Voice Search Listening Overlay
        if (isVoiceListening) {
            VoiceListeningOverlay(
                onDismiss = { viewModel.stopVoiceListening() },
                onSimulatedInput = { viewModel.stopVoiceListening(it) }
            )
        }

        // 5. Price Alerts Creation Dialog
        alertCommodityTarget?.let { (comm, price) ->
            com.example.farmhelper.ui.market.screens.CreatePriceAlertDialog(
                commodity = comm,
                currentPrice = price,
                onDismiss = { alertCommodityTarget = null },
                onCreateAlert = { c, threshold, cond ->
                    viewModel.createSubscription(c, threshold, cond)
                    Toast.makeText(context, "Price alert saved for $c", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 6. Active Subscriptions Sheet
        if (showSubscriptionsSheet) {
            com.example.farmhelper.ui.market.screens.PriceSubscriptionsSheet(
                subscriptions = subscriptionsState,
                onDismiss = { showSubscriptionsSheet = false },
                onDeleteSub = { viewModel.deleteSubscription(it) }
            )
        }

        // 7. Location Permission Rationale Dialog
        if (showPermissionRationaleDialog) {
            PermissionRationaleDialog(
                onDismiss = { showPermissionRationaleDialog = false },
                onSettingsRedirect = {
                    showPermissionRationaleDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onGrantPermission = {
                    showPermissionRationaleDialog = false
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }
            )
        }
    }
}

@Composable
fun MarketSearchHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearchFocus: () -> Unit,
    onVoiceClick: () -> Unit,
    onClearClick: () -> Unit,
    onAlertsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = WarmBeige,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, LightBorderGreen, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MediumGrayText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.search_crop_hint),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MediumGrayText.copy(alpha = 0.6f))
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = DarkGrayText),
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures { onSearchFocus() }
                                }
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = onClearClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MediumGrayText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(GlowGreen, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = ForestGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun FilterDropdownBar(
    selectedDistrict: String?,
    selectedCommodity: String?,
    selectedMarket: String?,
    districtsList: List<String>,
    commoditiesList: List<String>,
    marketsList: List<String>,
    onDistrictSelected: (String?) -> Unit,
    onCommoditySelected: (String?) -> Unit,
    onMarketSelected: (String?) -> Unit,
    onClearAll: () -> Unit
) {
    var activeFilterMenu by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterPill(
                    label = selectedDistrict?.let { LanguageManager.translateDynamic(it) } ?: stringResource(id = R.string.filter_district),
                    isSelected = selectedDistrict != null,
                    onClick = { activeFilterMenu = "district" }
                )
            }
            item {
                FilterPill(
                    label = selectedCommodity?.let { LanguageManager.translateDynamic(it) } ?: stringResource(id = R.string.filter_commodity),
                    isSelected = selectedCommodity != null,
                    onClick = { activeFilterMenu = "commodity" }
                )
            }
            if (selectedDistrict != null) {
                item {
                    FilterPill(
                        label = selectedMarket?.let { LanguageManager.translateDynamic(it) } ?: stringResource(id = R.string.filter_market),
                        isSelected = selectedMarket != null,
                        onClick = { activeFilterMenu = "market" }
                    )
                }
            }
        }

        if (selectedDistrict != null || selectedCommodity != null || selectedMarket != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.clear_filters),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AlertRed,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onClearAll() }
            )
        }
    }

    // Menus Dialogs
    when (activeFilterMenu) {
        "district" -> {
            FilterSelectDialog(
                title = stringResource(id = R.string.filter_district),
                options = districtsList,
                onSelected = {
                    onDistrictSelected(it)
                    activeFilterMenu = null
                },
                onDismiss = { activeFilterMenu = null }
            )
        }
        "commodity" -> {
            FilterSelectDialog(
                title = stringResource(id = R.string.filter_commodity),
                options = commoditiesList,
                onSelected = {
                    onCommoditySelected(it)
                    activeFilterMenu = null
                },
                onDismiss = { activeFilterMenu = null }
            )
        }
        "market" -> {
            FilterSelectDialog(
                title = stringResource(id = R.string.filter_market),
                options = marketsList,
                onSelected = {
                    onMarketSelected(it)
                    activeFilterMenu = null
                },
                onDismiss = { activeFilterMenu = null }
            )
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) ForestGreen else SoftOlive)
            .border(1.dp, if (isSelected) ForestGreen else LightBorderGreen, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) Color.White else DarkGrayText,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (isSelected) Color.White else MediumGrayText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FilterSelectDialog(
    title: String,
    options: List<String>,
    onSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.any_filter_option, title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(null) }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = LightBorderGreen)
                }
                items(options) { option ->
                    Text(
                        text = LanguageManager.translateDynamic(option),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(option) }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = LightBorderGreen)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel), color = MediumGrayText)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
@Composable
fun PopularCropsRow(
    commodities: List<String>,
    selectedCommodity: String,
    onSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.popular_crops_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(commodities.take(10)) { crop ->
                val isSelected = crop.equals(selectedCommodity, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GlowGreen else Color.White)
                        .border(
                            1.dp,
                            if (isSelected) ForestGreen else LightBorderGreen,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelected(crop) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = LanguageManager.translateDynamic(crop),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isSelected) ForestGreen else DarkGrayText,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MarketTrendChartCard(
    commodityName: String,
    districtName: String,
    historyState: PriceHistoryUiState,
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorderGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.price_history_trend),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    )
                    Text(
                        text = "${LanguageManager.translateDynamic(commodityName)} - ${LanguageManager.translateDynamic(districtName)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                    )
                }

                // Days Selection Toggle Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        7 to stringResource(id = R.string.time_7d),
                        30 to stringResource(id = R.string.time_30d),
                        90 to stringResource(id = R.string.time_90d),
                        180 to stringResource(id = R.string.time_180d),
                        365 to stringResource(id = R.string.time_365d)
                    ).forEach { (days, label) ->
                        val isSelected = selectedDays == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ForestGreen else Color.Transparent)
                                .clickable { onDaysSelected(days) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else MediumGrayText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (historyState) {
                is PriceHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ForestGreen)
                    }
                }
                is PriceHistoryUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = historyState.message,
                            style = MaterialTheme.typography.bodyMedium.copy(color = AlertRed)
                        )
                    }
                }
                is PriceHistoryUiState.Success -> {
                    val points = historyState.history
                    if (points.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No historical price data available.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MediumGrayText)
                            )
                        }
                    } else {
                        CustomInteractiveChart(points = points)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomInteractiveChart(
    points: List<PriceHistoryItem>
) {
    val prices = points.map { it.modal_price.toFloat() }
    val maxPrice = (prices.maxOrNull() ?: 100f) * 1.05f
    val minPrice = (prices.minOrNull() ?: 0f) * 0.95f
    val range = maxPrice - minPrice

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }

    var tooltipPointIndex by remember { mutableStateOf<Int?>(null) }
    var touchPositionX by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(points) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    val maxScroll = size.width * (scale - 1f)
                    offsetX = (offsetX + pan.x).coerceIn(-maxScroll, 0f)
                }
            }
            .pointerInput(points) {
                detectTapGestures(
                    onPress = { position ->
                        val adjustedX = position.x - offsetX
                        val totalWidth = size.width * scale
                        val stepX = totalWidth / (points.size - 1).coerceAtLeast(1)
                        val index = (adjustedX / stepX)
                            .toInt()
                            .coerceIn(0, points.size - 1)
                        tooltipPointIndex = index
                        touchPositionX = position.x
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val totalWidth = width * scale
        val stepX = totalWidth / (points.size - 1).coerceAtLeast(1)

        val chartPath = Path()
        val fillPath = Path()

        val pointsList = points.mapIndexed { index, item ->
            val x = index * stepX + offsetX
            val ratio = if (range > 0) (item.modal_price.toFloat() - minPrice) / range else 0.5f
            val y = height - (ratio * (height - 40.dp.toPx())) - 20.dp.toPx()
            Offset(x, y)
        }

        // Draw grid lines
        val gridCount = 4
        for (i in 0 until gridCount) {
            val gridY = 20.dp.toPx() + (height - 40.dp.toPx()) * i / (gridCount - 1)
            drawLine(
                color = LightBorderGreen.copy(alpha = 0.5f),
                start = Offset(0f, gridY),
                end = Offset(width, gridY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Generate Path
        if (pointsList.isNotEmpty()) {
            chartPath.moveTo(pointsList.first().x, pointsList.first().y)
            fillPath.moveTo(pointsList.first().x, height)
            fillPath.lineTo(pointsList.first().x, pointsList.first().y)

            for (i in 1 until pointsList.size) {
                val pPrev = pointsList[i - 1]
                val pCur = pointsList[i]
                // Draw smooth curve using cubic bezier control points
                val controlX1 = pPrev.x + (pCur.x - pPrev.x) / 2
                val controlY1 = pPrev.y
                val controlX2 = pPrev.x + (pCur.x - pPrev.x) / 2
                val controlY2 = pCur.y

                chartPath.cubicTo(controlX1, controlY1, controlX2, controlY2, pCur.x, pCur.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, pCur.x, pCur.y)
            }

            fillPath.lineTo(pointsList.last().x, height)
            fillPath.close()

            // Draw Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(SecondaryGreen.copy(alpha = 0.25f), Color.Transparent)
                )
            )

            // Draw Path Line
            drawPath(
                path = chartPath,
                color = ForestGreen,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw hovered point & vertical marker line if active
            tooltipPointIndex?.let { index ->
                val p = pointsList[index]
                if (p.x in 0f..width) {
                    // Draw vertical guide line
                    drawLine(
                        color = ForestGreen.copy(alpha = 0.3f),
                        start = Offset(p.x, 0f),
                        end = Offset(p.x, height),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw outer glowing circle
                    drawCircle(
                        color = SecondaryGreen.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = p
                    )

                    // Draw inner solid circle
                    drawCircle(
                        color = ForestGreen,
                        radius = 5.dp.toPx(),
                        center = p
                    )
                }
            }
        }
    }

    // Display overlay tooltip values directly
    tooltipPointIndex?.let { index ->
        val item = points[index]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .background(SoftFieldGreen, RoundedCornerShape(12.dp))
                .border(1.dp, ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.date,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = MediumGrayText)
            )
            Text(
                text = stringResource(id = R.string.modal_price_label, item.modal_price.toInt().toString()),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
            )
        }
    }
}

@Composable
fun MarketInsightsCard(
    insightsState: InsightsUiState
) {
    when (insightsState) {
        is InsightsUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ForestGreen)
            }
        }
        is InsightsUiState.Error -> {
            // Adapted gracefully - hide or show empty
        }
        is InsightsUiState.Success -> {
            val insights = insightsState.insights

            val recommendationColor = when (insights.selling_recommendation.decision.lowercase()) {
                "good time to sell", "sell immediately" -> ForestGreen
                "hold" -> AlertOrange
                else -> AlertOrange
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, LightBorderGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title and status pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.market_intelligence),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(recommendationColor.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = LanguageManager.translateDynamic(insights.selling_recommendation.decision),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = recommendationColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = LanguageManager.translateDynamic(insights.insights_summary),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DarkGrayText,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // bullet reasons
                    insights.selling_recommendation.reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium.copy(color = ForestGreen, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = LanguageManager.translateDynamic(reason),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MediumGrayText)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = LightBorderGreen)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Best market sub-info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.best_market_title),
                                style = MaterialTheme.typography.labelSmall.copy(color = MediumGrayText)
                            )
                            Text(
                                text = LanguageManager.translateDynamic(insights.best_market.market),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkGrayText)
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.price_per_qtl, insights.best_market.modal_price.toInt().toString()),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CropRateCard(
    priceItem: CropPriceItem,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onSetAlertClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorderGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SoftFieldGreen, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = LanguageManager.translateDynamic(priceItem.commodity),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkGrayText),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) AlertOrange else MediumGrayText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onSetAlertClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert",
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "${LanguageManager.translateDynamic(priceItem.market)} (${LanguageManager.translateDynamic(priceItem.district)})",
                    style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                )
                Text(
                    text = "${stringResource(id = R.string.variety_label)}: ${LanguageManager.translateDynamic(priceItem.variety)} • ${stringResource(id = R.string.date_label)}: ${priceItem.arrival_date}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MediumGrayText, fontSize = 10.sp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(id = R.string.price_per_qtl, priceItem.modal_price.toInt().toString()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.min_price_label, priceItem.min_price.toInt().toString()),
                        style = MaterialTheme.typography.labelSmall.copy(color = MediumGrayText, fontSize = 9.sp)
                    )
                    Text(
                        text = stringResource(id = R.string.max_price_label, priceItem.max_price.toInt().toString()),
                        style = MaterialTheme.typography.labelSmall.copy(color = MediumGrayText, fontSize = 9.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentSearchList(
    recentSearches: List<String>,
    onSearchSelected: (String) -> Unit,
    onClearHistory: () -> Unit,
    onUseCurrentLocationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorderGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Current Location Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUseCurrentLocationClick() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.use_current_location),
                    style = MaterialTheme.typography.bodyMedium.copy(color = ForestGreen, fontWeight = FontWeight.Bold)
                )
            }
            HorizontalDivider(color = LightBorderGreen)

            if (recentSearches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.recent_searches),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
                    )
                    Text(
                        text = stringResource(id = R.string.clear_history),
                        style = MaterialTheme.typography.labelMedium.copy(color = AlertRed, fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable { onClearHistory() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                recentSearches.forEach { search ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearchSelected(search) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MediumGrayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = LanguageManager.translateDynamic(search),
                            style = MaterialTheme.typography.bodyMedium.copy(color = DarkGrayText)
                        )
                    }
                    HorizontalDivider(color = LightBorderGreen)
                }
            }
        }
    }
}

@Composable
fun VoiceListeningOverlay(
    onDismiss: () -> Unit,
    onSimulatedInput: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    // Automatically simulate a result after 3 seconds for demonstration / testability
    LaunchedEffect(Unit) {
        delay(3000)
        onSimulatedInput("Cotton")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                // outer wave ripple
                Box(
                    modifier = Modifier
                        .size(100.dp * waveScale)
                        .background(GlowGreen.copy(alpha = 0.3f), CircleShape)
                )

                // inner solid microphone icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(ForestGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listening",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.voice_listening),
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun MarketShimmerLoader() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mock chart shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, LightBorderGreen, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            CircularProgressIndicator(
                color = ForestGreen,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Mock list items shimmer
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, LightBorderGreen, RoundedCornerShape(20.dp))
            )
        }
    }
}

@Composable
fun MarketErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBorderGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = AlertRed,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.error_fetching_data),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkGrayText),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MediumGrayText),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.retry_button),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GovernmentMspCard(
    commodityName: String,
    prevailingAvgPrice: Double,
    modifier: Modifier = Modifier
) {
    val mspRate = getMspForCrop(commodityName) ?: return // Hide if no MSP declared for crop

    val priceDifference = prevailingAvgPrice - mspRate
    val isAboveMsp = priceDifference >= 0
    val diffText = abs(priceDifference.toInt())

    val comparisonText = if (isAboveMsp) {
        stringResource(id = R.string.msp_above_prevailing, diffText)
    } else {
        stringResource(id = R.string.msp_below_prevailing, diffText)
    }

    val badgeText = if (isAboveMsp) stringResource(id = R.string.msp_above_badge) else stringResource(id = R.string.msp_below_badge)
    val badgeColor = if (isAboveMsp) ForestGreen else AlertRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorderGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Gavel,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.gov_msp_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main MSP Value display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${LanguageManager.translateDynamic(commodityName)} MSP (2024-25)",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DarkGrayText, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = stringResource(id = R.string.price_per_qtl, mspRate.toInt().toString()),
                    style = MaterialTheme.typography.titleLarge.copy(color = ForestGreen, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LightBorderGreen.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Comparison status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = if (isAboveMsp) ForestGreen else AlertOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comparisonText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = DarkGrayText),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.msp_desc),
                style = MaterialTheme.typography.labelSmall.copy(color = MediumGrayText, fontSize = 10.sp),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun MarketOfflineCard(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBorderGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = "Offline",
                    tint = AlertOrange,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.no_internet_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkGrayText),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.offline_mode_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MediumGrayText),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.retry_button),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TopMarketsCard(
    topMarketsState: TopMarketsUiState,
    commodity: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorderGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Top APMC Mandis for $commodity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (topMarketsState) {
                is TopMarketsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ForestGreen)
                    }
                }
                is TopMarketsUiState.Error -> {
                    Text(
                        text = "Top mandis ranking unavailable",
                        style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                    )
                }
                is TopMarketsUiState.Success -> {
                    val markets = topMarketsState.topMarkets.take(3)
                    if (markets.isEmpty()) {
                        Text(
                            text = "No top mandis data available",
                            style = MaterialTheme.typography.bodySmall.copy(color = MediumGrayText)
                        )
                    } else {
                        markets.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .background(
                                                if (index == 0) ForestGreen else LightBorderGreen,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (index == 0) Color.White else DarkGrayText,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = LanguageManager.translateDynamic(item.market),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DarkGrayText)
                                        )
                                        Text(
                                            text = "Score: ${String.format(Locale.getDefault(), "%.1f", item.total_score)}/100",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MediumGrayText, fontSize = 10.sp)
                                        )
                                    }
                                }
                                Text(
                                    text = "₹${item.current_price.toInt()}/qtl",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
                                )
                            }
                            if (index < markets.size - 1) {
                                HorizontalDivider(color = LightBorderGreen.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getMspForCrop(cropName: String): Double? {
    return when (cropName.trim().lowercase()) {
        "groundnut" -> 6780.0
        "cotton" -> 7120.0
        "wheat" -> 2275.0
        "rice", "paddy" -> 2300.0
        "sugarcane" -> 340.0
        "mustard" -> 5650.0
        "cumin seeds", "cumin" -> 30000.0
        "castor seed", "castor" -> 5800.0
        "sesamum" -> 8660.0
        "gram", "chickpea" -> 5440.0
        "onion" -> 2000.0
        "potato" -> 1500.0
        "tomato" -> 1800.0
        "garlic" -> 6000.0
        else -> null
    }
}

private fun checkAndRequestLocationPermission(
    context: Context,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onRationaleRequired: () -> Unit,
    onAlreadyGranted: () -> Unit
) {
    val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    if (fineGranted || coarseGranted) {
        onAlreadyGranted()
    } else {
        val activity = context.findActivity()
        if (activity != null && (
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
        )) {
            onRationaleRequired()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }
}

@Composable
private fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onSettingsRedirect: () -> Unit,
    onGrantPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.permission_denied_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permission_denied_desc),
                style = MaterialTheme.typography.bodyMedium.copy(color = DarkGrayText)
            )
        },
        confirmButton = {
            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text(text = stringResource(R.string.permission_grant_btn), color = Color.White)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSettingsRedirect) {
                    Text(text = stringResource(R.string.permission_settings_btn), color = ForestGreen)
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.dismiss_btn), color = ForestGreen)
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
