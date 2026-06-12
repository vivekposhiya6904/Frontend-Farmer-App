package com.example.farmhelper.ui.home



import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Exact same color system as existing screens
private val PrimaryGreen = Color(0xFF2E7D32)
private val SecondaryGreen = Color(0xFF4CAF50)
private val AccentGreen = Color(0xFF8BC34A)
private val Background = Color(0xFFF8FFF8)
private val DarkText = Color(0xFF1A1A1A)
private val MediumGray = Color(0xFF616161)
private val BorderGreen = Color(0xFFE0F0E0)
private val FieldBackground = Color(0xFFF3FAF3)
private val White = Color(0xFFFFFFFF)

// Feature data class
data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

// Weather data class
data class WeatherInfo(
    val day: String,
    val temperature: String,
    val icon: ImageVector,
    val condition: String
)

// Price data class
data class CropPrice(
    val cropName: String,
    val price: String,
    val change: String,
    val isPositive: Boolean,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onFeatureClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Same decorative background as existing screens
        DecorativeBackground(screenWidth, screenHeight)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HomeTopBar(
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                    isVisible = isVisible
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Welcome Section
                item {
                    WelcomeSection(isVisible = isVisible)
                }

                // Weather Section
                item {
                    WeatherSection(isVisible = isVisible)
                }

                // Features Grid
                item {
                    FeaturesSection(
                        isVisible = isVisible,
                        onFeatureClick = onFeatureClick
                    )
                }

                // Crop Prices Section
                item {
                    CropPricesSection(isVisible = isVisible)
                }

                // Smart Tips Section
                item {
                    SmartTipsSection(isVisible = isVisible)
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
    // Same decorative circles as LoginScreen, LanguageSelection, etc.
    Box(
        modifier = Modifier
            .size(screenWidth * 0.6f)
            .offset(x = screenWidth * 0.5f, y = -screenHeight * 0.05f)
            .blur(80.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SecondaryGreen.copy(alpha = 0.08f),
                        PrimaryGreen.copy(alpha = 0.03f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(screenWidth * 0.5f)
            .offset(x = -screenWidth * 0.2f, y = screenHeight * 0.15f)
            .blur(60.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentGreen.copy(alpha = 0.06f),
                        SecondaryGreen.copy(alpha = 0.02f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(screenWidth * 0.4f)
            .offset(x = screenWidth * 0.3f, y = screenHeight * 0.4f)
            .blur(70.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PrimaryGreen.copy(alpha = 0.04f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )

    // Subtle green glow at bottom
    Box(
        modifier = Modifier
            .size(screenWidth * 0.7f)
            .offset(x = screenWidth * 0.15f, y = screenHeight * 0.8f)
            .blur(90.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentGreen.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    isVisible: Boolean
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Same logo style as SplashScreen
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FARMER",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        fontSize = 22.sp
                    )
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .animateFadeIn(
                        animationSpec = tween(800, delayMillis = 400)
                    )
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = PrimaryGreen,
                            contentColor = White
                        ) {
                            Text("3")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = DarkText
                    )
                }
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .animateFadeIn(
                        animationSpec = tween(800, delayMillis = 600)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryGreen, SecondaryGreen)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = DarkText
        )
    )
}

@Composable
private fun WelcomeSection(isVisible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .animateFadeIn(
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
    ) {
        Text(
            text = "Good Morning,",
            color = MediumGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Rajesh Kumar 👋",
            color = DarkText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your farm is ready for today's activities",
            color = MediumGray,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun WeatherSection(isVisible: Boolean) {
    val weatherData = remember {
        listOf(
            WeatherInfo("Today", "32°C", Icons.Filled.WbSunny, "Sunny"),
            WeatherInfo("Tomorrow", "28°C", Icons.Filled.Cloud, "Cloudy"),
            WeatherInfo("Wed", "30°C", Icons.Filled.WbSunny, "Clear"),
            WeatherInfo("Thu", "26°C", Icons.Filled.Thunderstorm, "Rain"),
            WeatherInfo("Fri", "29°C", Icons.Filled.CloudQueue, "Partly")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .animateFadeIn(
                animationSpec = tween(800, delayMillis = 400)
            )
    ) {
        // Section Header - Same style as LanguageSelection
        Text(
            text = "Weather Forecast",
            color = PrimaryGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(weatherData) { weather ->
                WeatherCard(weather = weather)
            }
        }
    }
}

@Composable
private fun WeatherCard(weather: WeatherInfo) {
    // Same card style as Language cards and Login card
    Card(
        modifier = Modifier
            .width(100.dp)
            .animateFadeIn(
                animationSpec = tween(600)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        border = BorderStroke(1.dp, BorderGreen),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.day,
                color = MediumGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryGreen.copy(alpha = 0.1f),
                                SecondaryGreen.copy(alpha = 0.05f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = weather.icon,
                    contentDescription = weather.condition,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = weather.temperature,
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weather.condition,
                color = MediumGray,
                fontSize = 11.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeaturesSection(
    isVisible: Boolean,
    onFeatureClick: (String) -> Unit
) {
    val features = remember {
        listOf(
            Feature("Weather", "Forecast", Icons.Filled.WbSunny, listOf(PrimaryGreen, SecondaryGreen)),
            Feature("Prices", "Market Rates", Icons.Filled.TrendingUp, listOf(SecondaryGreen, AccentGreen)),
            Feature("Insights", "Smart Tips", Icons.Outlined.Eco, listOf(PrimaryGreen, AccentGreen)),
            Feature("Assist", "Get Help", Icons.Filled.SupportAgent, listOf(SecondaryGreen, PrimaryGreen))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .animateFadeIn(
                animationSpec = tween(800, delayMillis = 600)
            )
    ) {
        Text(
            text = "Quick Actions",
            color = PrimaryGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            features.forEach { feature ->
                FeatureCard(
                    feature = feature,
                    onClick = { onFeatureClick(feature.title) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: Feature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Same card style and button gradient as existing screens
    Card(
        modifier = modifier
            .animateScaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        border = BorderStroke(1.dp, BorderGreen),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = feature.gradientColors
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = PrimaryGreen.copy(alpha = 0.2f),
                        spotColor = PrimaryGreen.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = feature.title,
                color = DarkText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = feature.description,
                color = MediumGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CropPricesSection(isVisible: Boolean) {
    val prices = remember {
        listOf(
            CropPrice("Wheat", "₹2,150/quintal", "+₹45", true, Icons.Filled.Grain),
            CropPrice("Rice", "₹1,980/quintal", "+₹32", true, Icons.Filled.Landscape),
            CropPrice("Cotton", "₹6,320/quintal", "-₹120", false, Icons.Filled.Nature),
            CropPrice("Sugarcane", "₹315/quintal", "+₹8", true, Icons.Filled.Park)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .animateFadeIn(
                animationSpec = tween(800, delayMillis = 800)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Crop Prices",
                color = PrimaryGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = { /* View all */ },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = SecondaryGreen
                )
            ) {
                Text(
                    text = "View All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Same card style as Login/SignUp cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = White
            ),
            border = BorderStroke(1.dp, BorderGreen),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column {
                prices.forEachIndexed { index, price ->
                    CropPriceItem(price = price)
                    if (index < prices.lastIndex) {
                        HorizontalDivider(
                            color = BorderGreen,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CropPriceItem(price: CropPrice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryGreen.copy(alpha = 0.1f),
                            SecondaryGreen.copy(alpha = 0.05f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = price.icon,
                contentDescription = price.cropName,
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = price.cropName,
                color = DarkText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = price.price,
            color = DarkText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = price.change,
            color = if (price.isPositive) PrimaryGreen else Color.Red,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SmartTipsSection(isVisible: Boolean) {
    val tips = remember {
        listOf(
            "Use drip irrigation to save water and increase crop yield by 30%.",
            "Check soil pH regularly for optimal nutrient absorption.",
            "Rotate crops seasonally to maintain soil fertility."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .animateFadeIn(
                animationSpec = tween(800, delayMillis = 1000)
            )
    ) {
        Text(
            text = "Smart Farming Tips",
            color = PrimaryGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Same card style as other sections
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = White
            ),
            border = BorderStroke(1.dp, BorderGreen),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                tips.forEachIndexed { index, tip ->
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(24.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryGreen, SecondaryGreen)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tip,
                            color = DarkText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index < tips.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// Custom animation modifiers to match existing app animations
fun Modifier.animateFadeIn(
    animationSpec: AnimationSpec<Float> = spring()
): Modifier = composed {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "fadeIn"
    )
    this.graphicsLayer { this.alpha = alpha }
}

fun Modifier.animateScaleIn(
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "scaleIn"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}