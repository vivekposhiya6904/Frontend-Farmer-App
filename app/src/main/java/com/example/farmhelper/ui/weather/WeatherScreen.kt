package com.example.farmhelper.ui.weather

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmhelper.R
import com.example.farmhelper.ui.home.components.SectionHeader
import com.example.farmhelper.ui.home.models.AlertSeverity
import com.example.farmhelper.ui.home.models.WeatherAlert
import com.example.farmhelper.ui.theme.*
import com.example.farmhelper.ui.weather.models.*
import com.example.farmhelper.ui.weather.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val languageFlow = remember { com.example.farmhelper.ui.localization.LanguageManager.getLanguageFlow(context) }
    val currentLanguageCode by languageFlow.collectAsState(initial = com.example.farmhelper.ui.localization.LanguageManager.currentLanguage)

    val weatherState by viewModel.weatherState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val favoriteLocations by viewModel.favoriteLocations.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.fetchWeatherWithGPS()
        } else {
            Toast.makeText(context, context.getString(R.string.gps_permission_denied_message), Toast.LENGTH_LONG).show()
        }
    }

    // Auto-focus search input when activated
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    val userCrops by viewModel.userCrops.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationHistory by viewModel.notificationHistory.collectAsState()
    val isCrudLoading by viewModel.isCrudLoading.collectAsState()

    var activeSheet by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBeige)
    ) {
        DecorativeBackground(screenWidth, screenHeight)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isSearchActive) {
                                    isSearchActive = false
                                } else {
                                    onBackClick()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(SoftOlive, CircleShape)
                                .border(1.dp, ForestGreen.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = ForestGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.weather_screen_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                fontSize = 20.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    viewModel.fetchUserCrops()
                                    activeSheet = "crops"
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.Eco, contentDescription = "Crops", tint = ForestGreen)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.fetchSavedLocations()
                                    activeSheet = "locations"
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "Locations", tint = ForestGreen)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.fetchNotificationHistory()
                                    activeSheet = "notifications"
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "History", tint = ForestGreen)
                            }
                            IconButton(
                                onClick = { activeSheet = "settings" },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings", tint = ForestGreen)
                            }
                        }
                    }

                    // Search Trigger Bar
                    if (!isSearchActive) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clickable { isSearchActive = true },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = ForestGreen
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(id = R.string.search_hint),
                                    color = MediumGrayText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        checkAndRequestLocationPermission(
                                            context = context,
                                            permissionLauncher = permissionLauncher,
                                            onRationaleRequired = { showPermissionRationaleDialog = true },
                                            onAlreadyGranted = { viewModel.fetchWeatherWithGPS() }
                                        )
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Use Current Location",
                                        tint = ForestGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Weather Main Content (Inactive search screen)
                when (val state = weatherState) {
                    is WeatherState.Loading -> {
                        WeatherScreenLoadingSkeleton()
                    }
                    is WeatherState.Success -> {
                        var isRefreshing by remember { mutableStateOf(false) }
                        val pullRefreshState = rememberPullToRefreshState()
                        
                        LaunchedEffect(state) {
                            isRefreshing = false
                        }
                        
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                isRefreshing = true
                                viewModel.fetchWeather(state.data.locationName)
                            },
                            state = pullRefreshState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            WeatherMainContent(
                                data = state.data,
                                favoriteLocations = favoriteLocations,
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                currentLanguageCode = currentLanguageCode
                            )
                        }
                    }
                    is WeatherState.Error -> {
                        if (state.lastCachedData != null) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                OfflineBanner()
                                WeatherMainContent(
                                    data = state.lastCachedData,
                                    favoriteLocations = favoriteLocations,
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    currentLanguageCode = currentLanguageCode
                                )
                            }
                        } else {
                            EmptyStateBlock(
                                errorMessage = state.message,
                                onRetryClick = {
                                    checkAndRequestLocationPermission(
                                        context = context,
                                        permissionLauncher = permissionLauncher,
                                        onRationaleRequired = { showPermissionRationaleDialog = true },
                                        onAlreadyGranted = { viewModel.fetchWeatherWithGPS() }
                                    )
                                }
                            )
                        }
                    }
                }

                // Smoothly expanding active search screen overlay
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(animationSpec = tween(250)) + slideInVertically(
                        initialOffsetY = { -50 },
                        animationSpec = tween(250)
                    ),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
                        targetOffsetY = { -50 },
                        animationSpec = tween(200)
                    )
                ) {
                    BackHandler {
                        isSearchActive = false
                    }
                    ActiveSearchOverlay(
                        searchQuery = searchQuery,
                        suggestions = suggestions,
                        recentSearches = recentSearches,
                        favoriteLocations = favoriteLocations,
                        focusRequester = focusRequester,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onBackClick = { isSearchActive = false },
                        onSuggestionClick = { loc ->
                            viewModel.fetchWeather(loc)
                            isSearchActive = false
                        },
                        onRecentSearchClick = { loc ->
                            viewModel.fetchWeather(loc)
                            isSearchActive = false
                        },
                        onRecentSearchDelete = { viewModel.removeRecentSearch(it) },
                        onClearHistoryClick = { viewModel.clearRecentSearches() },
                        onUseCurrentLocationClick = {
                            isSearchActive = false
                            checkAndRequestLocationPermission(
                                context = context,
                                permissionLauncher = permissionLauncher,
                                onRationaleRequired = { showPermissionRationaleDialog = true },
                                onAlreadyGranted = { viewModel.fetchWeatherWithGPS() }
                            )
                        }
                    )
                }
            }
        }

        when (activeSheet) {
            "crops" -> {
                BackHandler { activeSheet = null }
                com.example.farmhelper.ui.weather.screens.UserCropSetupScreen(
                    userCrops = userCrops,
                    isLoading = isCrudLoading,
                    onBackClick = { activeSheet = null },
                    onAddCrop = { viewModel.addUserCrop(it) },
                    onDeleteCrop = { viewModel.deleteUserCrop(it) }
                )
            }
            "locations" -> {
                BackHandler { activeSheet = null }
                com.example.farmhelper.ui.weather.screens.SavedLocationsScreen(
                    savedLocations = savedLocations,
                    isLoading = isCrudLoading,
                    onBackClick = { activeSheet = null },
                    onAddLocation = { viewModel.addSavedLocation(it) },
                    onDeleteLocation = { viewModel.deleteSavedLocation(it) },
                    onSetDefaultLocation = { viewModel.setDefaultLocation(it) }
                )
            }
            "settings" -> {
                BackHandler { activeSheet = null }
                com.example.farmhelper.ui.weather.screens.WeatherSettingsScreen(
                    currentUnit = temperatureUnit,
                    notificationsEnabled = notificationsEnabled,
                    onBackClick = { activeSheet = null },
                    onUnitChanged = { viewModel.updateTemperatureUnit(it) },
                    onNotificationToggle = { viewModel.toggleNotificationEnabled(it) },
                    onLanguageSelected = { langCode ->
                        viewModel.syncLanguagePreference(langCode)
                        val loc = (weatherState as? WeatherState.Success)?.data?.locationName ?: "Rajkot"
                        viewModel.fetchWeather(loc)
                    }
                )
            }
            "notifications" -> {
                BackHandler { activeSheet = null }
                com.example.farmhelper.ui.weather.screens.NotificationHistoryScreen(
                    notifications = notificationHistory,
                    isLoading = isCrudLoading,
                    onBackClick = { activeSheet = null },
                    onDeleteNotification = { viewModel.deleteNotificationHistoryItem(it) }
                )
            }
        }
    }

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

@Composable
private fun WeatherMainContent(
    data: WeatherDashboardData,
    favoriteLocations: List<String>,
    onFavoriteToggle: (String) -> Unit,
    currentLanguageCode: String
) {
    val context = LocalContext.current
    val isFavorite = remember(favoriteLocations, data.locationName) {
        favoriteLocations.contains(data.locationName)
    }

    // Dynamic translation of details label metrics in Composables
    val detailedMetrics = remember(data, currentLanguageCode) {
        listOf(
            MetricDetail(context.getString(R.string.humidity), "${data.humidity}%", Icons.Outlined.WaterDrop),
            MetricDetail(context.getString(R.string.wind), "${data.windKph.toInt()} km/h ${data.windDir}", Icons.Outlined.Air),
            MetricDetail(context.getString(R.string.pressure), "${data.pressureHpa.toInt()} hPa", Icons.Outlined.Speed),
            MetricDetail(context.getString(R.string.visibility), "${data.visibilityKm.toInt()} km", Icons.Outlined.Visibility),
            MetricDetail(context.getString(R.string.uv_index), "${data.uvIndex.toInt()}", Icons.Outlined.WbSunny),
            MetricDetail(context.getString(R.string.sunrise), data.sunrise, Icons.Outlined.LightMode),
            MetricDetail(context.getString(R.string.sunset), data.sunset, Icons.Outlined.NightsStay),
            MetricDetail(context.getString(R.string.moon_phase), data.moonPhase, Icons.Outlined.Brightness3)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Premium Hero Card
        item {
            HeroWeatherCard(
                temp = data.currentTemp.toInt(),
                condition = data.condition,
                iconCode = data.conditionIcon,
                location = data.locationName,
                updatedTime = data.lastUpdatedText,
                feelsLike = data.feelsLike.toInt(),
                isFavorite = isFavorite,
                onFavoriteClick = { onFavoriteToggle(data.locationName) },
                summaryText = data.weatherSummary
            )
        }

        // 2. Next Rain Indicator Card (If Rain Forecasted)
        if (!data.nextRainTime.isNullOrEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftOlive),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ForestGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WaterDrop,
                                contentDescription = null,
                                tint = White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Next Expected Rain",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = data.nextRainTime,
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkGrayText, fontSize = 12.sp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Field Work Recommendations
        item {
            FarmingRecommendationsSection(data.sevenDayForecast)
        }

        // 3. Hourly Forecast
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = stringResource(id = R.string.hourly_forecast))
                HourlyTimelineRow(points = data.hourlyForecast)
            }
        }

        // 4. Weather Alerts Section
        if (data.alerts.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(title = stringResource(id = R.string.weather_alerts))
                    data.alerts.forEach { alert ->
                        AlertBlock(alert = alert)
                    }
                }
            }
        }

        // 5. Detailed Weather Metrics
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = stringResource(id = R.string.detailed_metrics))
                MetricsGrid(metrics = detailedMetrics)
            }
        }

        // 6. Seven Day Forecast
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = stringResource(id = R.string.seven_day_forecast))
                WeeklyForecastColumn(forecasts = data.sevenDayForecast)
            }
        }

        // 7. Crop Advisory
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = stringResource(id = R.string.crop_advisory))
                CropAdvisoryBlock(advisories = data.cropAdvisories)
            }
        }

        // 8. Analytic Insights Placeholder
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = stringResource(id = R.string.weather_history))
                HistoryPlaceholderBlock()
            }
        }
    }
}

@Composable
private fun ActiveSearchOverlay(
    searchQuery: String,
    suggestions: List<String>,
    recentSearches: List<String>,
    favoriteLocations: List<String>,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onRecentSearchDelete: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
    onUseCurrentLocationClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = WarmBeige
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SoftOlive, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ForestGreen
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = MediumGrayText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ForestGreen
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = ForestGreen
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = ForestGreen.copy(alpha = 0.3f),
                        cursorColor = ForestGreen,
                        focusedTextColor = DarkGrayText,
                        unfocusedTextColor = DarkGrayText
                    ),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchQuery.trim().isNotEmpty()) {
                            onSuggestionClick(searchQuery.trim())
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    })
                )
            }

            HorizontalDivider(color = ForestGreen.copy(alpha = 0.1f))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // GPS detection shortcut
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onUseCurrentLocationClick() }
                            .background(SoftOlive.copy(alpha = 0.5f))
                            .border(1.dp, ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.use_current_location),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        )
                    }
                }

                // Autocomplete/Suggestions filtering
                if (searchQuery.trim().length >= 2) {
                    item {
                        Text(
                            text = "Suggestions",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MediumGrayText,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }

                    if (suggestions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_results),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AlertRed
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.no_results_suggestions),
                                        style = MaterialTheme.typography.bodySmall.copy(color = DarkGrayText),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(suggestions) { suggestion ->
                            SuggestionItemRow(
                                suggestion = suggestion,
                                onClick = { onSuggestionClick(suggestion) }
                            )
                        }
                    }
                } else {
                    // Recent Searches
                    if (recentSearches.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.recent_searches),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.clear_history),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = AlertRed,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.clickable { onClearHistoryClick() }
                                )
                            }
                        }

                        items(recentSearches) { recent ->
                            RecentSearchItemRow(
                                location = recent,
                                onDeleteClick = { onRecentSearchDelete(recent) },
                                onItemClick = { onRecentSearchClick(recent) }
                            )
                        }
                    }

                    // Favorite Locations List
                    if (favoriteLocations.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.favorites),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                ),
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }

                        items(favoriteLocations) { favorite ->
                            FavoriteLocationItemRow(
                                location = favorite,
                                onItemClick = { onRecentSearchClick(favorite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItemRow(
    suggestion: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationCity,
                contentDescription = null,
                tint = ForestGreen.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = DarkGrayText
                )
            )
        }
    }
}

@Composable
private fun RecentSearchItemRow(
    location: String,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick() }
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MediumGrayText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium.copy(color = DarkGrayText)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MediumGrayText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoriteLocationItemRow(
    location: String,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen
                )
            )
        }
    }
}

@Composable
private fun HeroWeatherCard(
    temp: Int,
    condition: String,
    iconCode: String,
    location: String,
    updatedTime: String,
    feelsLike: Int,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    summaryText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SageGreen.copy(alpha = 0.9f), SoftOlive.copy(alpha = 0.95f))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = location,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                fontSize = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) AccentGreen else ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = updatedTime,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ForestGreen.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$temp°C",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 52.sp
                        )
                    )
                    Text(
                        text = getLocalizedCondition(condition),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = ForestGreen.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Dynamic weather illustration code
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getWeatherIcon(iconCode),
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmWhite, RoundedCornerShape(16.dp))
                    .border(1.dp, ForestGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.feels_like_temp, feelsLike),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = summaryText.ifEmpty { stringResource(id = R.string.weather_summary_text) },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkGrayText.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmingRecommendationsSection(
    sevenDayForecast: List<WeeklyDayForecast>,
    modifier: Modifier = Modifier
) {
    val firstForecast = sevenDayForecast.firstOrNull()
    val recommendations = remember(firstForecast) {
        if (firstForecast != null) {
            val rainChance = firstForecast.rainProb.replace("%", "").toIntOrNull() ?: 0
            val condition = firstForecast.conditionText.lowercase()
            
            val works = mutableListOf<Pair<Int, Color>>()
            
            // Localized Field Work Recommendations
            if (rainChance > 50) {
                works.add(Pair(R.string.advisory_irrigate_bad, AlertOrange))
                works.add(Pair(R.string.advisory_spray_bad, AlertOrange))
            } else {
                works.add(Pair(R.string.advisory_irrigate_good, ForestGreen))
                works.add(Pair(R.string.advisory_spray_good, ForestGreen))
            }
            
            if (condition.contains("sunny") || condition.contains("clear")) {
                works.add(Pair(R.string.safe_for_harvesting, ForestGreen))
            }
            
            works
        } else {
            listOf(
                Pair(R.string.good_day_irrigation, ForestGreen),
                Pair(R.string.safe_for_harvesting, ForestGreen)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SectionHeader(title = stringResource(id = R.string.field_workability))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recommendations) { item ->
                Card(
                    modifier = Modifier.width(220.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(item.second.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = item.second,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = item.first),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText,
                                fontSize = 14.sp,
                                lineHeight = 18.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyTimelineRow(
    points: List<HourlyPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f))
    ) {
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(points) { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(65.dp)
                ) {
                    Text(
                        text = point.time,
                        style = MaterialTheme.typography.labelMedium.copy(color = MediumGrayText)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SoftOlive, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getWeatherIcon(point.conditionIcon),
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = point.temp,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertBlock(
    alert: WeatherAlert,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Alert surface
        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AlertRed.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = AlertRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AlertRed,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkGrayText.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.date,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MediumGrayText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MetricsGrid(
    metrics: List<MetricDetail>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val chunked = metrics.chunked(3)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { metric ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhite),
                        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SoftOlive, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = metric.icon,
                                    contentDescription = metric.label,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MediumGrayText,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = metric.value,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGrayText,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyForecastColumn(
    forecasts: List<WeeklyDayForecast>,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            forecasts.forEachIndexed { index, day ->
                val formattedDay = remember(day.date, locale) {
                    try {
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val dateObj = format.parse(day.date)
                        val outFormat = SimpleDateFormat("EEEE", locale)
                        outFormat.format(dateObj ?: Date()).replaceFirstChar { it.uppercase() }
                    } catch (e: Exception) {
                        day.day
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(SoftOlive, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getWeatherIcon(day.conditionIcon),
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = formattedDay,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGrayText,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "${getLocalizedCondition(day.conditionText)} • ${stringResource(id = R.string.rain_chance)}: ${day.rainProb}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MediumGrayText,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = day.tempRange,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                fontSize = 13.sp
                            )
                        )
                        if (!day.windowBadgeText.isNullOrEmpty()) {
                            val isBest = day.windowBadgeColorType == "best"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isBest) ForestGreen else AlertRed,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = if (isBest) "Best: ${day.windowBadgeText}" else "Risk: ${day.windowBadgeText}",
                                    color = White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(id = day.farmingAdvisoryRes),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (day.farmingAdvisoryRes == R.string.advisory_avoid_spraying || day.farmingAdvisoryRes == R.string.advisory_avoid_fields || day.farmingAdvisoryRes == R.string.advisory_avoid_pesticides) AlertRed else ForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
                if (index < forecasts.size - 1) {
                    HorizontalDivider(color = LightBorderGreen.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun CropAdvisoryBlock(
    advisories: List<CropAdvisory>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(advisories) { item ->
            Card(
                modifier = Modifier.width(220.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SoftOlive),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val cropTitle = when {
                        !item.cropNameText.isNullOrEmpty() -> item.cropNameText
                        item.cropNameRes != null -> stringResource(id = item.cropNameRes)
                        else -> "Crop Advisory"
                    }
                    Text(
                        text = cropTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (!item.titleText.isNullOrEmpty() || !item.descriptionText.isNullOrEmpty()) {
                        if (!item.titleText.isNullOrEmpty()) {
                            Text(
                                text = item.titleText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGrayText,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (!item.descriptionText.isNullOrEmpty()) {
                            Text(
                                text = item.descriptionText,
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkGrayText, fontSize = 11.sp)
                            )
                        }
                    } else {
                        item.irrigationRes?.let { res ->
                            Text(
                                text = stringResource(id = res),
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkGrayText, fontSize = 11.sp)
                            )
                        }
                        item.harvestRes?.let { res ->
                            Text(
                                text = stringResource(id = res),
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkGrayText, fontSize = 11.sp)
                            )
                        }
                        item.pesticideRes?.let { res ->
                            val pestText = stringResource(id = res)
                            Text(
                                text = pestText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (res == R.string.advisory_pesticide_avoid) AlertRed else ForestGreen,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryPlaceholderBlock(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SoftOlive, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = R.string.weather_history),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DarkGrayText)
            )
            Text(
                text = stringResource(id = R.string.history_placeholder),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MediumGrayText,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DecorativeBackground(
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(screenWidth * 0.7f)
            .offset(x = screenWidth * 0.5f, y = -screenHeight * 0.05f)
            .blur(80.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SageGreen.copy(alpha = 0.08f), Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 120.dp, bottomEnd = 120.dp)
            )
    )

    Box(
        modifier = Modifier
            .size(screenWidth * 0.6f)
            .offset(x = -screenWidth * 0.2f, y = screenHeight * 0.3f)
            .blur(70.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(ForestGreen.copy(alpha = 0.04f), Color.Transparent)
                ),
                shape = RoundedCornerShape(topEnd = 100.dp, bottomStart = 100.dp)
            )
    )
}

@Composable
private fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        SageGreen.copy(alpha = 0.3f),
        SoftOlive.copy(alpha = 0.4f),
        SageGreen.copy(alpha = 0.3f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    return this.background(brush)
}

@Composable
private fun WeatherScreenLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Hero Card Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .shimmer()
        )
        // Recommendations Shimmer
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shimmer()
            )
        }
        // Hourly Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .shimmer()
        )
        // Metrics Grid Shimmer
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(20.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(20.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(20.dp)).shimmer())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(20.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(20.dp)).shimmer())
                Box(modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(20.dp)).shimmer())
            }
        }
        // 7-Day Forecast Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(26.dp))
                .shimmer()
        )
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AlertOrange.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = "Offline Mode",
                tint = White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Offline Mode — Displaying cached weather data",
                color = White,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun EmptyStateBlock(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(SoftOlive, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_results),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.no_results_suggestions),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DarkGrayText,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = stringResource(R.string.use_current_location), color = Color.White)
        }
    }
}

@Composable
private fun OfflineBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SoftOlive),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.showing_last_updated),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
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
private fun getWeatherIcon(iconCode: String): ImageVector {
    return when (iconCode) {
        "113" -> Icons.Outlined.WbSunny
        "116", "119", "122" -> Icons.Outlined.Cloud
        "176", "293", "296", "302" -> Icons.Outlined.WaterDrop
        "305", "308", "353", "356", "359" -> Icons.Outlined.Thunderstorm
        else -> Icons.Outlined.WbSunny
    }
}

@Composable
private fun getLocalizedCondition(condition: String): String {
    val resId = when (condition.lowercase()) {
        "sunny", "clear" -> R.string.weather_sunny
        "cloudy", "overcast" -> R.string.weather_cloudy
        "partly cloudy" -> R.string.weather_partly_cloudy
        "heavy rain", "torrential rain" -> R.string.weather_heavy_rain
        "light rain", "patchy light rain", "showers", "scattered showers" -> R.string.weather_scattered_showers
        else -> null
    }
    return if (resId != null) stringResource(resId) else condition
}

private data class MetricDetail(
    val label: String,
    val value: String,
    val icon: ImageVector
)

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
