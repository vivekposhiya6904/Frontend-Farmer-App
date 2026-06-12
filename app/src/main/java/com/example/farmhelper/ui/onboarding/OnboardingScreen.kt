package com.example.farmhelper.ui.onboarding

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import com.example.farmhelper.ui.localization.LanguageManager

// Color constants matching the existing design system
private val PrimaryGreen = Color(0xFF2E7D32)
private val SecondaryGreen = Color(0xFF4CAF50)
private val AccentGreen = Color(0xFF8BC34A)
private val BackgroundGreen = Color(0xFFF8FFF8)
private val DarkText = Color(0xFF1A1A1A)
private val MediumGray = Color(0xFF616161)
private val BorderGreen = Color(0xFFE0F0E0)
private val FieldBackground = Color(0xFFF3FAF3)

// Onboarding page data
data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconDescription: String
)

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = LanguageManager.get("weather"),
                subtitle = "Get accurate weather forecasts to plan your farming activities and protect your crops from unexpected weather conditions.",
                icon = Icons.Filled.WbSunny,
                iconDescription = "Weather"
            ),
            OnboardingPage(
                title = LanguageManager.get("prices"),
                subtitle = "Track real-time crop market prices and make better selling decisions for maximum profitability.",
                icon = Icons.Filled.TrendingUp,
                iconDescription = "Prices"
            ),
            OnboardingPage(
                title = LanguageManager.get("smart"),
                subtitle = "Use modern technology and smart farming insights to improve productivity and increase profits.",
                icon = Icons.Outlined.Eco,
                iconDescription = "Smart Farming"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, BackgroundGreen),
                    startY = 0f,
                    endY = screenHeight.value * 0.6f
                )
            )
    ) {
        // Decorative background circles matching LoginScreen style
        DecorativeBackgroundCircles(screenWidth, screenHeight)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button for first two pages
            if (pagerState.currentPage < pages.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, end = 24.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    TextButton(
                        onClick = onSkip,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MediumGray
                        )
                    ) {
                        Text(
                            "Skip",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    screenHeight = screenHeight,
                    screenWidth = screenWidth
                )
            }

            // Bottom section with indicators and buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicators
                PageIndicators(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Action buttons
                if (pagerState.currentPage < pages.size - 1) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(PrimaryGreen, SecondaryGreen, AccentGreen)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                LanguageManager.get("next"),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(PrimaryGreen, SecondaryGreen, AccentGreen)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                LanguageManager.get("start"),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DecorativeBackgroundCircles(
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    // Top-right circle
    Box(
        modifier = Modifier
            .size(screenWidth * 0.5f)
            .offset(
                x = screenWidth * 0.6f,
                y = -screenHeight * 0.1f
            )
            .background(
                color = PrimaryGreen.copy(alpha = 0.05f),
                shape = CircleShape
            )
    )

    // Bottom-left circle
    Box(
        modifier = Modifier
            .size(screenWidth * 0.4f)
            .offset(
                x = -screenWidth * 0.15f,
                y = screenHeight * 0.7f
            )
            .background(
                color = SecondaryGreen.copy(alpha = 0.05f),
                shape = CircleShape
            )
    )

    // Center decorative circle
    Box(
        modifier = Modifier
            .size(screenWidth * 0.3f)
            .offset(
                x = screenWidth * 0.4f,
                y = screenHeight * 0.3f
            )
            .background(
                color = AccentGreen.copy(alpha = 0.03f),
                shape = CircleShape
            )
    )

    // Small accent circles
    Box(
        modifier = Modifier
            .size(80.dp)
            .offset(
                x = screenWidth * 0.7f,
                y = screenHeight * 0.5f
            )
            .background(
                color = PrimaryGreen.copy(alpha = 0.03f),
                shape = CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .offset(
                x = screenWidth * 0.2f,
                y = screenHeight * 0.15f
            )
            .background(
                color = SecondaryGreen.copy(alpha = 0.04f),
                shape = CircleShape
            )
    )
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    // Animations
    val animationDelay = 300

    // Fade and scale animation
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = animationDelay,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            delay(animationDelay.toLong())

            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration area
        AnimatedIllustration(
            icon = page.icon,
            iconDescription = page.iconDescription,
            screenWidth = screenWidth
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle
        Text(
            text = page.subtitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MediumGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

@Composable
private fun AnimatedIllustration(
    icon: ImageVector,
    iconDescription: String,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    // Pulse animation for outer ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    val containerSize = screenWidth * 0.45f

    Box(
        modifier = Modifier
            .size(containerSize)
            .offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulse ring
        Box(
            modifier = Modifier
                .size(containerSize * pulseScale)
                .background(
                    color = SecondaryGreen.copy(alpha = pulseAlpha),
                    shape = CircleShape
                )
        )

        // Middle circle
        Box(
            modifier = Modifier
                .size(containerSize * 0.85f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SecondaryGreen.copy(alpha = 0.15f),
                            PrimaryGreen.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Inner gradient circle
        Box(
            modifier = Modifier
                .size(containerSize * 0.7f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGreen, SecondaryGreen)
                    ),
                    shape = CircleShape
                )
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = PrimaryGreen.copy(alpha = 0.3f),
                    spotColor = PrimaryGreen.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                modifier = Modifier.size(containerSize * 0.35f),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isCurrentPage = index == currentPage

            // Animation for indicator
            val width by animateDpAsState(
                targetValue = if (isCurrentPage) 32.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "indicatorWidth"
            )

            val indicatorColor by animateColorAsState(
                targetValue = if (isCurrentPage) PrimaryGreen else BorderGreen,
                animationSpec = tween(300),
                label = "indicatorColor"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(width)
                    .height(8.dp)
                    .background(
                        brush = if (isCurrentPage) {
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryGreen, SecondaryGreen, AccentGreen)
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(indicatorColor, indicatorColor)
                            )
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

// Preview for development
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewOnboardingScreen() {
    MaterialTheme {
        OnboardingScreen(
            onGetStarted = {},
            onSkip = {}
        )
    }
}