package com.example.farmhelper.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlinx.coroutines.delay

// ─── Color Palette ────────────────────────────────────────────────────────────
private val PrimaryGreen   = Color(0xFF2E7D32)
private val SecondaryGreen = Color(0xFF4CAF50)
private val AccentGreen    = Color(0xFF8BC34A)
private val BackgroundTint = Color(0xFFF8FFF8)
private val DarkGray       = Color(0xFF212121)
private val MediumGray     = Color(0xFF757575)
private val LightGray      = Color(0xFFBDBDBD)
private val GlowColor      = Color(0x554CAF50)
private val TrackColor     = Color(0x334CAF50)
private val LeafOpacity    = Color(0x1A2E7D32)

// ─── Floating Leaf Data ───────────────────────────────────────────────────────
private data class FloatingLeaf(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val speed: Float,
    val phase: Float,
    val rotation: Float
)

private val floatingLeaves = listOf(
    FloatingLeaf(0.08f, 0.15f, 18f, 0.6f, 0.0f,  15f),
    FloatingLeaf(0.88f, 0.22f, 12f, 0.8f, 0.3f, -20f),
    FloatingLeaf(0.05f, 0.55f, 22f, 0.5f, 0.6f,  30f),
    FloatingLeaf(0.92f, 0.60f, 14f, 0.7f, 0.9f, -10f),
    FloatingLeaf(0.15f, 0.82f, 10f, 0.9f, 0.2f,  45f),
    FloatingLeaf(0.80f, 0.78f, 16f, 0.6f, 0.5f, -35f),
    FloatingLeaf(0.50f, 0.06f, 11f, 0.75f,0.8f,  25f),
)

// ─── Main Splash Composable ───────────────────────────────────────────────────
@Composable
fun FarmerSplashScreen(onFinished: () -> Unit = {}) {

    LaunchedEffect(Unit) {
        delay(3000)
        onFinished()
    }

    // ── Screen-level fade-in ──────────────────────────────────────────────────
    val screenAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "screenAlpha"
    )

    // ── Infinite transition hub ───────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splashInfinite")

    // Tractor orbit angle  0 → 360 in 3.2 s
    val tractorAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tractorAngle"
    )

    // Glow / pulse radius modifier  0 → 1 → 0 in 2 s
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Floating-leaf drift  0 → 1 in 6 s
    val leafDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "leafDrift"
    )

    // Loading dots  0 → 3 cycling every 600 ms
    val dotIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    // Branding scale-in pop  0.7 → 1.0
    val brandScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "brandScale"
    )

    // Centre leaf gentle rotation
    val centerLeafRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue  =  8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "centerLeafRot"
    )

    // ─────────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundTint,
                        Color(0xFFECF5E8),
                        Color(0xFFDEEFD6)
                    )
                )
            )
            .graphicsLayer { alpha = screenAlpha },
        contentAlignment = Alignment.Center
    ) {

        // ── Floating leaves layer (full screen canvas) ──────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFloatingLeaves(leafDrift)
        }

        // ── Central content column ───────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            Spacer(modifier = Modifier.weight(1f))

            // ── Tractor orbit loader ─────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawOrbitLoader(
                        tractorAngle  = tractorAngle,
                        pulseProgress = pulseProgress
                    )
                }

                // Centre leaf / plant icon drawn via Canvas
                Canvas(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer { rotationZ = centerLeafRotation }
                ) {
                    drawCenterPlantIcon()
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── App name ─────────────────────────────────────────────────────
            Text(
                text     = "FARMER",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color    = PrimaryGreen,
                letterSpacing = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = brandScale
                    scaleY = brandScale
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            Text(
                text      = "Smart Farming Companion",
                fontSize  = 15.sp,
                fontWeight = FontWeight.Normal,
                color     = MediumGray,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Subtle separator ─────────────────────────────────────────────
            Canvas(modifier = Modifier.size(width = 48.dp, height = 2.dp)) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, AccentGreen, Color.Transparent)
                    ),
                    start       = Offset(0f, size.height / 2),
                    end         = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Animated loading text ─────────────────────────────────────────
            val dots = ".".repeat((dotIndex.toInt()).coerceIn(0, 3))
            val fadeFraction = dotIndex - dotIndex.toInt()

            Text(
                text      = "Loading$dots",
                fontSize  = 13.sp,
                fontWeight = FontWeight.Medium,
                color     = LightGray.copy(alpha = 0.6f + fadeFraction * 0.4f),
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ─── Orbit Loader Drawing ─────────────────────────────────────────────────────
private fun DrawScope.drawOrbitLoader(tractorAngle: Float, pulseProgress: Float) {
    val cx = size.width  / 2f
    val cy = size.height / 2f

    val trackRadius   = size.minDimension / 2f * 0.82f
    val tractorRadius = 14.dp.toPx()

    // ── Multi-layer glow rings (pulse) ───────────────────────────────────────
    val glowLayers = listOf(
        Pair(trackRadius + 12f + pulseProgress * 6f,  0.06f),
        Pair(trackRadius +  8f + pulseProgress * 4f,  0.10f),
        Pair(trackRadius +  4f + pulseProgress * 2f,  0.15f),
    )
    glowLayers.forEach { (r, alpha) ->
        drawCircle(
            color  = SecondaryGreen.copy(alpha = alpha),
            radius = r,
            center = Offset(cx, cy),
            style  = Stroke(width = 6.dp.toPx())
        )
    }

    // ── Track dashes (dashed circle) ─────────────────────────────────────────
    val dashCount = 36
    for (i in 0 until dashCount) {
        val startDeg = i * (360f / dashCount)
        val sweepDeg = (360f / dashCount) * 0.55f
        val dashAlpha = if (i % 3 == 0) 0.45f else 0.18f
        drawArc(
            color     = AccentGreen.copy(alpha = dashAlpha),
            startAngle = startDeg,
            sweepAngle = sweepDeg,
            useCenter  = false,
            topLeft    = Offset(cx - trackRadius, cy - trackRadius),
            size       = Size(trackRadius * 2f, trackRadius * 2f),
            style      = Stroke(
                width  = 3.dp.toPx(),
                cap    = StrokeCap.Round
            )
        )
    }

    // ── Main glowing track ────────────────────────────────────────────────────
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                AccentGreen.copy(alpha = 0.3f),
                SecondaryGreen.copy(alpha = 0.7f),
                PrimaryGreen,
                SecondaryGreen.copy(alpha = 0.7f),
                AccentGreen.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = Offset(cx, cy)
        ),
        radius = trackRadius,
        center = Offset(cx, cy),
        style  = Stroke(width = 3.5.dp.toPx())
    )

    // ── Tractor trail arc ─────────────────────────────────────────────────────
    val trailSweep = 60f
    val trailStart = tractorAngle - 90f - trailSweep
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                PrimaryGreen.copy(alpha = 0.5f),
                PrimaryGreen,
                Color.Transparent
            ),
            center = Offset(cx, cy)
        ),
        startAngle = trailStart,
        sweepAngle = trailSweep,
        useCenter  = false,
        topLeft    = Offset(cx - trackRadius, cy - trackRadius),
        size       = Size(trackRadius * 2f, trackRadius * 2f),
        style      = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // ── Tractor position on orbit ─────────────────────────────────────────────
    val angleRad = Math.toRadians((tractorAngle - 90.0))
    val tx = cx + trackRadius * cos(angleRad).toFloat()
    val ty = cy + trackRadius * sin(angleRad).toFloat()

    // Tractor glow halo
    drawCircle(
        color  = SecondaryGreen.copy(alpha = 0.25f + pulseProgress * 0.15f),
        radius = tractorRadius * 2.4f,
        center = Offset(tx, ty)
    )
    drawCircle(
        color  = PrimaryGreen.copy(alpha = 0.15f),
        radius = tractorRadius * 1.8f,
        center = Offset(tx, ty)
    )

    // Outer tractor disc
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(AccentGreen, PrimaryGreen),
            center = Offset(tx, ty),
            radius = tractorRadius
        ),
        radius = tractorRadius,
        center = Offset(tx, ty)
    )

    // Tractor icon (simplified top-view silhouette using shapes)
    val directionAngle = tractorAngle.toDouble()
    val dirRad = Math.toRadians(directionAngle)

    withTransform({
        translate(tx, ty)
        rotate(degrees = tractorAngle, pivot = Offset(0f, 0f))
    }) {
        drawTractorIcon(tractorRadius)
    }

    // Tractor border ring
    drawCircle(
        color  = Color.White.copy(alpha = 0.8f),
        radius = tractorRadius,
        center = Offset(tx, ty),
        style  = Stroke(width = 1.5.dp.toPx())
    )
}

// ─── Tractor Icon (top-view silhouette) ──────────────────────────────────────
private fun DrawScope.drawTractorIcon(r: Float) {
    val bodyW = r * 0.85f
    val bodyH = r * 1.3f

    // Body
    drawRoundRect(
        color      = Color.White.copy(alpha = 0.92f),
        topLeft    = Offset(-bodyW / 2, -bodyH / 2),
        size       = Size(bodyW, bodyH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.2f),
        style      = Fill
    )

    // Cabin (smaller rect at top of body)
    val cabinW = bodyW * 0.7f
    val cabinH = bodyH * 0.38f
    drawRoundRect(
        color      = Color(0xFF1B5E20).copy(alpha = 0.85f),
        topLeft    = Offset(-cabinW / 2, -bodyH / 2 + bodyH * 0.08f),
        size       = Size(cabinW, cabinH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.12f),
        style      = Fill
    )

    // Front large wheel (top)
    val fWheelR = r * 0.34f
    drawCircle(
        color  = Color(0xFF33691E),
        radius = fWheelR,
        center = Offset(0f, -bodyH / 2 - fWheelR * 0.3f)
    )
    drawCircle(
        color  = Color.White.copy(alpha = 0.25f),
        radius = fWheelR,
        center = Offset(0f, -bodyH / 2 - fWheelR * 0.3f),
        style  = Stroke(width = 1.2.dp.toPx())
    )

    // Rear large wheel (bottom)
    val rWheelR = r * 0.42f
    drawCircle(
        color  = Color(0xFF33691E),
        radius = rWheelR,
        center = Offset(0f, bodyH / 2 + rWheelR * 0.2f)
    )
    drawCircle(
        color  = Color.White.copy(alpha = 0.25f),
        radius = rWheelR,
        center = Offset(0f, bodyH / 2 + rWheelR * 0.2f),
        style  = Stroke(width = 1.2.dp.toPx())
    )

    // Exhaust pipe dot
    drawCircle(
        color  = Color(0xFF558B2F),
        radius = r * 0.10f,
        center = Offset(bodyW * 0.28f, -bodyH / 2 + bodyH * 0.06f)
    )
}

// ─── Centre Plant Icon ────────────────────────────────────────────────────────
private fun DrawScope.drawCenterPlantIcon() {
    val cx = size.width  / 2f
    val cy = size.height / 2f
    val s  = size.minDimension * 0.38f

    // Stem
    drawLine(
        color       = PrimaryGreen,
        start       = Offset(cx, cy + s * 0.9f),
        end         = Offset(cx, cy - s * 0.2f),
        strokeWidth = s * 0.14f,
        cap         = StrokeCap.Round
    )

    // Left leaf
    val leftPath = Path().apply {
        moveTo(cx, cy + s * 0.25f)
        cubicTo(
            cx - s * 0.95f, cy + s * 0.10f,
            cx - s * 1.0f,  cy - s * 0.55f,
            cx,             cy - s * 0.20f
        )
        close()
    }
    drawPath(
        path  = leftPath,
        brush = Brush.linearGradient(
            colors = listOf(AccentGreen, SecondaryGreen),
            start  = Offset(cx - s, cy),
            end    = Offset(cx, cy - s * 0.5f)
        )
    )

    // Right leaf
    val rightPath = Path().apply {
        moveTo(cx, cy + s * 0.0f)
        cubicTo(
            cx + s * 0.95f, cy - s * 0.15f,
            cx + s * 1.0f,  cy - s * 0.75f,
            cx,             cy - s * 0.55f
        )
        close()
    }
    drawPath(
        path  = rightPath,
        brush = Brush.linearGradient(
            colors = listOf(SecondaryGreen, PrimaryGreen),
            start  = Offset(cx, cy),
            end    = Offset(cx + s, cy - s * 0.7f)
        )
    )

    // Small bud at top
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(AccentGreen, PrimaryGreen),
            center = Offset(cx, cy - s * 0.55f),
            radius = s * 0.22f
        ),
        radius = s * 0.22f,
        center = Offset(cx, cy - s * 0.55f)
    )

    // Soil / ground circle
    drawArc(
        color      = PrimaryGreen.copy(alpha = 0.25f),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter  = true,
        topLeft    = Offset(cx - s * 0.55f, cy + s * 0.65f),
        size       = Size(s * 1.10f, s * 0.50f)
    )
}

// ─── Floating Leaves ──────────────────────────────────────────────────────────
private fun DrawScope.drawFloatingLeaves(driftProgress: Float) {
    floatingLeaves.forEach { leaf ->
        val phase = (driftProgress + leaf.phase) % 1f
        val x = size.width  * leaf.startX + sin(phase * 2 * PI.toFloat() + leaf.phase * 10f) * 18f
        val y = size.height * leaf.startY - phase * size.height * 0.22f * leaf.speed

        withTransform({
            translate(x, y)
            rotate(leaf.rotation + phase * 30f, Offset(0f, 0f))
        }) {
            drawSmallLeaf(leaf.size)
        }
    }
}

private fun DrawScope.drawSmallLeaf(size: Float) {
    val leafPath = Path().apply {
        moveTo(0f, size * 0.5f)
        cubicTo(-size * 0.5f, size * 0.15f, -size * 0.5f, -size * 0.3f, 0f, -size * 0.5f)
        cubicTo( size * 0.5f, -size * 0.3f,  size * 0.5f,  size * 0.15f, 0f,  size * 0.5f)
        close()
    }
    drawPath(
        path  = leafPath,
        color = PrimaryGreen.copy(alpha = 0.10f)
    )
    // Midrib
    drawLine(
        color       = PrimaryGreen.copy(alpha = 0.08f),
        start       = Offset(0f, -size * 0.5f),
        end         = Offset(0f,  size * 0.5f),
        strokeWidth = 1f,
        cap         = StrokeCap.Round
    )
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun FarmerSplashScreenPreview() {
    FarmerSplashScreen()
}