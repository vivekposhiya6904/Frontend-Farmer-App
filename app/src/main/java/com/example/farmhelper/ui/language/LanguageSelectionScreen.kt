package com.example.farmhelper.ui.language


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Palette ───────────────────────────────────────────────────────────────────
private val LangPrimaryGreen   = Color(0xFF2E7D32)
private val LangSecondaryGreen = Color(0xFF4CAF50)
private val LangAccentGreen    = Color(0xFF8BC34A)
private val LangBackground     = Color(0xFFF8FFF8)
private val LangCardBg         = Color(0xFFFFFFFF)
private val LangDarkGray       = Color(0xFF1A1A1A)
private val LangMediumGray     = Color(0xFF616161)
private val LangBorderIdle     = Color(0xFFE0F0E0)
private val LangSubtleFill     = Color(0xFFF3FAF3)

// ── Language Data Model ───────────────────────────────────────────────────────
private data class Language(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String,
    val tagline: String
)

private val supportedLanguages = listOf(
    Language("en",  "English",    "English",  "🇬🇧", "Default language"),
    Language("hi",  "हिन्दी",      "Hindi",    "🇮🇳", "भारत की राजभाषा"),
    Language("gu",  "ગુજરાતી",    "Gujarati", "🇮🇳", "ગુજરાત ની ભાષા")
)

// ── Main Screen ───────────────────────────────────────────────────────────────
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit = {}
) {
    var selectedCode by remember { mutableStateOf<String?>(null) }

    // ── Entry visibility ──────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    val headerAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "headerAlpha"
    )
    val headerSlide by animateFloatAsState(
        targetValue = if (visible) 0f else (-30f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "headerSlide"
    )
    val cardsAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, delayMillis = 180),
        label = "cardsAlpha"
    )
    val cardsSlide by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = tween(700, delayMillis = 180, easing = FastOutSlowInEasing),
        label = "cardsSlide"
    )
    val btnAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, delayMillis = 350),
        label = "btnAlpha"
    )

    // Logo pulse
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "ringPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(LangBackground, Color(0xFFE8F5E9), Color(0xFFD9EDD9))
                )
            )
    ) {
        // ── Decorative background blobs ───────────────────────────────────────
        LangBackgroundDecor()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = headerSlide.dp)
                    .alpha(headerAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .scale(ringPulse)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(LangAccentGreen.copy(alpha = 0.11f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    LangSecondaryGreen.copy(alpha = 0.22f),
                                    LangPrimaryGreen.copy(alpha = 0.08f)
                                )
                            )
                        )
                )
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = "Farmer",
                    tint = LangPrimaryGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App name
            Text(
                text = "FARMER",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = LangPrimaryGreen,
                letterSpacing = 9.sp,
                modifier = Modifier
                    .offset(y = headerSlide.dp)
                    .alpha(headerAlpha)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Subtitle card ─────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LangPrimaryGreen.copy(alpha = 0.07f),
                modifier = Modifier
                    .offset(y = headerSlide.dp)
                    .alpha(headerAlpha)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🌐", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Your Language",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LangPrimaryGreen,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose the language you're most comfortable with",
                fontSize = 13.sp,
                color = LangMediumGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .offset(y = headerSlide.dp)
                    .alpha(headerAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Language Cards ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = cardsSlide.dp)
                    .alpha(cardsAlpha),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                supportedLanguages.forEachIndexed { index, language ->
                    val delayMs = index * 70
                    var cardVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { delay((200 + delayMs).toLong()); cardVisible = true }

                    val cardSlideIn by animateFloatAsState(
                        targetValue = if (cardVisible) 0f else 40f,
                        animationSpec = tween(450, easing = FastOutSlowInEasing),
                        label = "cardSlide$index"
                    )
                    val cardAlphaIn by animateFloatAsState(
                        targetValue = if (cardVisible) 1f else 0f,
                        animationSpec = tween(450),
                        label = "cardAlpha$index"
                    )

                    LanguageCard(
                        language = language,
                        isSelected = selectedCode == language.code,
                        onSelect = { selectedCode = language.code },
                        modifier = Modifier
                            .offset(y = cardSlideIn.dp)
                            .alpha(cardAlphaIn)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Hint text ─────────────────────────────────────────────────────
            if (selectedCode == null) {
                Text(
                    text = "Please select a language to continue",
                    fontSize = 12.sp,
                    color = LangMediumGray.copy(0.55f),
                    letterSpacing = 0.3.sp,
                    modifier = Modifier
                        .alpha(btnAlpha)
                        .padding(bottom = 10.dp)
                )
            }

            // ── Continue Button ───────────────────────────────────────────────
            val btnEnabled = selectedCode != null
            val btnScale by animateFloatAsState(
                targetValue = if (btnEnabled) 1f else 0.97f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "btnScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 0.dp)
                    .alpha(btnAlpha)
                    .scale(btnScale)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (btnEnabled)
                            Brush.horizontalGradient(
                                listOf(LangPrimaryGreen, LangSecondaryGreen, LangAccentGreen)
                            )
                        else
                            Brush.horizontalGradient(
                                listOf(Color(0xFFB2DFDB), Color(0xFFB2DFDB))
                            )
                    )
                    .clickable(
                        enabled = btnEnabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectedCode?.let { onLanguageSelected(it) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (selectedCode) {
                            "en" -> "Continue  →"
                            "hi" -> "जारी रखें  →"
                            "gu" -> "આગળ વધો  →"
                            else -> "Continue  →"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (btnEnabled) Color.White else Color.White.copy(0.55f),
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Bottom tagline
            Text(
                text = "🌱 Empowering Indian Farmers",
                fontSize = 12.sp,
                color = LangMediumGray.copy(0.45f),
                letterSpacing = 0.4.sp,
                modifier = Modifier.alpha(btnAlpha)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ── Language Card ─────────────────────────────────────────────────────────────
@Composable
private fun LanguageCard(
    language: Language,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.025f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale_${language.code}"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(250),
        label = "borderAlpha_${language.code}"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "checkScale_${language.code}"
    )
    val bgTint by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "bgTint_${language.code}"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
    ) {
        // Card surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            LangSecondaryGreen.copy(alpha = borderAlpha),
                            LangAccentGreen.copy(alpha = borderAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSelect
                ),
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected)
                Color(0xFFEDF7ED).copy(alpha = 0.5f + bgTint * 0.5f)
            else
                LangCardBg,
            shadowElevation = if (isSelected) 6.dp else 3.dp,
            tonalElevation = if (isSelected) 2.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag + label circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                Brush.radialGradient(
                                    listOf(LangAccentGreen.copy(0.28f), LangPrimaryGreen.copy(0.10f))
                                )
                            else
                                Brush.radialGradient(
                                    listOf(LangSubtleFill, LangBorderIdle.copy(0.4f))
                                )
                        )
                ) {
                    Text(text = language.flag, fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = language.nativeName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) LangPrimaryGreen else LangDarkGray,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = language.tagline,
                        fontSize = 12.sp,
                        color = if (isSelected) LangSecondaryGreen else LangMediumGray,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }

                // Check icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .scale(checkScale),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = LangSecondaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (!isSelected) {
                    // Idle radio ring
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, LangBorderIdle, CircleShape)
                    )
                }
            }
        }

        // Selected glow rim
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LangAccentGreen.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

// ── Background Decorations ────────────────────────────────────────────────────
@Composable
private fun LangBackgroundDecor() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-right circle
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 220.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(LangAccentGreen.copy(alpha = 0.065f))
        )
        // Top-left small
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-40).dp, y = 80.dp)
                .clip(CircleShape)
                .background(LangSecondaryGreen.copy(alpha = 0.055f))
        )
        // Bottom-right circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 240.dp, y = 640.dp)
                .clip(CircleShape)
                .background(LangPrimaryGreen.copy(alpha = 0.05f))
        )
        // Bottom-left circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-50).dp, y = 700.dp)
                .clip(CircleShape)
                .background(LangAccentGreen.copy(alpha = 0.06f))
        )
        // Centre subtle dot
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 160.dp, y = 420.dp)
                .clip(CircleShape)
                .background(LangSecondaryGreen.copy(alpha = 0.04f))
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LanguageSelectionScreenPreview() {
    LanguageSelectionScreen()
}