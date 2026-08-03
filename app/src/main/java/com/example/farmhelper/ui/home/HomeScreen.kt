package com.example.farmhelper.ui.home

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.R
import com.example.farmhelper.session.SessionManager
import com.example.farmhelper.ui.home.components.*
import com.example.farmhelper.ui.home.models.*
import com.example.farmhelper.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmhelper.ui.home.viewmodel.HomeViewModel
import com.example.farmhelper.ui.home.viewmodel.HomeUiState
import com.example.farmhelper.ui.community.viewmodel.CommunityViewModel
import com.example.farmhelper.ui.community.viewmodel.CommunityFeedUiState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onFeatureClick: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userFullName by sessionManager.userFullName.collectAsState(initial = "Farmer")
    val userName = userFullName ?: "Farmer"

    var showLogoutDialog by remember { mutableStateOf(false) }
    val languageFlow = remember { com.example.farmhelper.ui.localization.LanguageManager.getLanguageFlow(context) }
    val currentLanguageCode by languageFlow.collectAsState(initial = com.example.farmhelper.ui.localization.LanguageManager.currentLanguage)
    val coroutineScope = rememberCoroutineScope()
    var selectedCropId by remember { mutableStateOf("wheat") }
    var selectedNavTab by remember { mutableStateOf("Home") }

    val uiState by viewModel.uiState.collectAsState()
    val communityState by communityViewModel.uiState.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is HomeUiState.Loading) {
            isRefreshing = false
        }
    }

    BackHandler(enabled = selectedNavTab != "Home") {
        selectedNavTab = "Home"
    }

    // Government Schemes
    val governmentSchemes = remember(currentLanguageCode) {
        listOf(
            GovernmentScheme(
                id = "scheme_1",
                title = context.getString(R.string.scheme_pm_kisan_title),
                description = context.getString(R.string.scheme_pm_kisan_desc),
                eligibility = context.getString(R.string.scheme_pm_kisan_eligibility),
                benefit = context.getString(R.string.scheme_pm_kisan_benefit),
                applyUrl = "https://pmkisan.gov.in",
                status = "Eligible"
            ),
            GovernmentScheme(
                id = "scheme_2",
                title = context.getString(R.string.scheme_bima_title),
                description = context.getString(R.string.scheme_bima_desc),
                eligibility = context.getString(R.string.scheme_bima_eligibility),
                benefit = context.getString(R.string.scheme_bima_benefit),
                applyUrl = "https://pmfby.gov.in",
                status = "Popular"
            ),
            GovernmentScheme(
                id = "scheme_3",
                title = context.getString(R.string.scheme_sinchayee_title),
                description = context.getString(R.string.scheme_sinchayee_desc),
                eligibility = context.getString(R.string.scheme_sinchayee_eligibility),
                benefit = context.getString(R.string.scheme_sinchayee_benefit),
                applyUrl = "https://pmksy.gov.in",
                status = "NEW"
            )
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.logout),
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.logout_confirmation),
                    color = DarkGrayText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = White)
                ) {
                    Text(stringResource(id = R.string.logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(id = R.string.cancel), color = MediumGrayText)
                }
            },
            containerColor = WarmWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige) // Premium Warm Beige Background
    ) {
        DecorativeBackground(screenWidth, screenHeight)

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigation(
                    selectedTab = selectedNavTab,
                    onTabSelected = { tab ->
                        selectedNavTab = tab
                        when (tab) {
                            "Community" -> onFeatureClick("community")
                            "Profile" -> onFeatureClick("profile")
                            "AI Assistant" -> onFeatureClick("ai_assistant")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (selectedNavTab == "Market") {
                    com.example.farmhelper.ui.market.MarketScreen()
                } else {
                    when (val state = uiState) {
                        is HomeUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = ForestGreen)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(id = R.string.loading_label),
                                        color = ForestGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        is HomeUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.2f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.error_fetching_data),
                                            color = AlertRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = state.message,
                                            color = DarkGrayText,
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { viewModel.fetchHomeData() },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                        ) {
                                            Text(stringResource(id = R.string.retry_button), color = White)
                                        }
                                    }
                                }
                            }
                        }
                        is HomeUiState.Success -> {
                            val pullRefreshState = rememberPullToRefreshState()
                            
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = {
                                    isRefreshing = true
                                    viewModel.fetchHomeData()
                                },
                                state = pullRefreshState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Greeting Header
                                    item {
                                        GreetingHeader(
                                            userName = userName,
                                            currentLanguageCode = currentLanguageCode,
                                            onLanguageSelected = { code ->
                                                coroutineScope.launch {
                                                    com.example.farmhelper.ui.localization.LanguageManager.saveLanguage(context, code)
                                                }
                                            },
                                            onProfileClick = { showLogoutDialog = true },
                                            onNotificationsClick = {
                                                Toast.makeText(context, "Opening Notifications", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    // 2. Weather Summary Card
                                    item {
                                        val weather = state.weather
                                        val rainChance = weather.sevenDayForecast.firstOrNull()?.rainProb ?: "0%"
                                        val rainChanceInt = rainChance.removeSuffix("%").toIntOrNull() ?: 0
                                        val isRainy = rainChanceInt > 50
                                        val advisoryTitle = if (isRainy) stringResource(id = R.string.advisory_rain) else stringResource(id = R.string.advisory_irrigate_good)
                                        val advisorySubtitle = if (isRainy) {
                                            "${stringResource(id = R.string.advisory_irrigate_bad)} • ${stringResource(id = R.string.advisory_spray_bad)}"
                                        } else {
                                            stringResource(id = R.string.advisory_spray_good)
                                        }
                                        val advisoryColor = if (isRainy) AlertRed else ForestGreen

                                        WeatherCard(
                                            currentTemp = weather.currentTemp.toInt(),
                                            condition = weather.condition,
                                            rainChance = rainChance,
                                            advisoryTitle = advisoryTitle,
                                            advisorySubtitle = advisorySubtitle,
                                            advisoryColor = advisoryColor,
                                            onViewFullWeatherClick = {
                                                onFeatureClick("weather")
                                            }
                                        )
                                    }

                                    // 3. Crop Price Dashboard
                                    item {
                                        val cropList = state.cropPrices
                                        val selectedCrop = remember(selectedCropId, cropList) {
                                            cropList.find { it.id == selectedCropId } ?: cropList[0]
                                        }

                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            SectionHeader(title = stringResource(id = R.string.crop_price_dashboard))
                                            
                                            CropTabs(
                                                crops = cropList,
                                                selectedCropId = selectedCropId,
                                                onCropSelected = { selectedCropId = it }
                                            )
                                            
                                            PriceDashboard(
                                                selectedCrop = selectedCrop,
                                                onViewMarketDetailsClick = {
                                                    selectedNavTab = "Market"
                                                }
                                            )
                                        }
                                    }

                                    // 4. Weather Alerts
                                    val alerts = state.weather.alerts
                                    if (alerts.isNotEmpty()) {
                                        item {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                SectionHeader(title = stringResource(id = R.string.weather_alerts))
                                                alerts.forEach { alert ->
                                                    AlertCard(alert = alert)
                                                }
                                            }
                                        }
                                    }

                                    // 5. AI Farm Assistant
                                    item {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            SectionHeader(title = stringResource(id = R.string.ai_assistant))
                                            AIAssistantCard(
                                                onClick = { onFeatureClick("ai_assistant") }
                                            )
                                        }
                                    }

                                    // 6. Farmer Community (Top 2 real backend posts + View All button)
                                    item {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            SectionHeader(
                                                title = stringResource(id = R.string.farmer_community)
                                            )
                                            
                                            when (val feedState = communityState) {
                                                is CommunityFeedUiState.Success -> {
                                                    val realPosts = feedState.posts.take(2)
                                                    if (realPosts.isNotEmpty()) {
                                                        realPosts.forEach { post ->
                                                            CommunityItemCard(
                                                                post = post,
                                                                onLikeClick = { communityViewModel.toggleLike(post.id) },
                                                                onCommentClick = { onFeatureClick("community") },
                                                                onAuthorClick = { onFeatureClick("community") }
                                                            )
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 20.dp, vertical = 12.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "No community posts yet. Share an update!",
                                                                color = MediumGrayText,
                                                                fontSize = 13.sp
                                                            )
                                                        }
                                                    }
                                                }
                                                is CommunityFeedUiState.Loading -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(20.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(
                                                            color = ForestGreen,
                                                            strokeWidth = 2.dp,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                                            contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "Connect with fellow farmers in the community",
                                                            color = MediumGrayText,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            
                                            Button(
                                                onClick = {
                                                    onFeatureClick("community")
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = SoftOlive, contentColor = ForestGreen),
                                                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f))
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.view_all),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // 7. Government Schemes (Top 2 schemes)
                                    item {
                                        GovernmentSchemeCard(
                                            schemes = governmentSchemes.take(2),
                                            onViewAllSchemesClick = {
                                                Toast.makeText(context, context.getString(R.string.toast_opening_schemes), Toast.LENGTH_SHORT).show()
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
}

@Composable
private fun DecorativeBackground(
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    // Leaf shape representation: offset and rotated rounded rectangles (Gradients of Sage & Forest green)
    Box(
        modifier = Modifier
            .size(screenWidth * 0.7f)
            .offset(x = screenWidth * 0.4f, y = -screenHeight * 0.05f)
            .blur(80.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SageGreen.copy(alpha = 0.08f), Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 120.dp, bottomEnd = 120.dp) // Organic leaf curve
            )
    )

    Box(
        modifier = Modifier
            .size(screenWidth * 0.6f)
            .offset(x = -screenWidth * 0.3f, y = screenHeight * 0.2f)
            .blur(70.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(ForestGreen.copy(alpha = 0.05f), Color.Transparent)
                ),
                shape = RoundedCornerShape(topEnd = 100.dp, bottomStart = 100.dp) // Organic leaf curve
            )
    )

    Box(
        modifier = Modifier
            .size(screenWidth * 0.8f)
            .offset(x = screenWidth * 0.1f, y = screenHeight * 0.65f)
            .blur(90.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SageGreen.copy(alpha = 0.06f), Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 150.dp, bottomEnd = 150.dp) // Organic leaf curve
            )
    )
}