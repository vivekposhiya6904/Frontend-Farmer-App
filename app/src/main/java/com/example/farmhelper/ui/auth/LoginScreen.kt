package com.example.farmhelper.ui.auth


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lint.kotlin.metadata.Visibility
import com.example.farmhelper.ui.localization.LanguageManager
import kotlinx.coroutines.delay

// ── Palette ──────────────────────────────────────────────────────────────────
private val LoginPrimaryGreen    = Color(0xFF2E7D32)
private val LoginSecondaryGreen  = Color(0xFF4CAF50)
private val LoginAccentGreen     = Color(0xFF8BC34A)
private val LoginBackground      = Color(0xFFF8FFF8)
private val LoginCardBg          = Color(0xFFFFFFFF)
private val LoginDarkGray        = Color(0xFF1A1A1A)
private val LoginMediumGray      = Color(0xFF616161)
private val LoginBorderColor     = Color(0xFFE0F0E0)
private val LoginFieldBg         = Color(0xFFF3FAF3)
private val LoginDisabledBtn     = Color(0xFFB2DFDB)

// ── Login Screen ──────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // ── Entry animations ──────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "logoAlpha"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 80f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "cardOffsetY"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 120),
        label = "cardAlpha"
    )

    // Button press scale
    val btnScale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btnScale"
    )

    // Infinite subtle pulse on logo ring
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(LoginBackground, Color(0xFFE8F5E9), Color(0xFFDCEDDC))
                )
            )
    ) {
        // ── Decorative background blobs ──────────────────────────────────────
        LoginBackgroundDecor()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // ── Logo Block ────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                // Outer pulse ring
                Box(
                    modifier = Modifier
                        .scale(ringPulse)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(LoginAccentGreen.copy(alpha = 0.12f))
                )
                // Inner ring
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(LoginSecondaryGreen.copy(0.22f), LoginPrimaryGreen.copy(0.10f))
                            )
                        )
                )
                // Icon
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = "Farmer Logo",
                    tint = LoginPrimaryGreen,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "FARMER",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = LoginPrimaryGreen,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Welcome back to Smart Farming",
                fontSize = 14.sp,
                color = LoginMediumGray,
                letterSpacing = 0.3.sp,
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Form Card ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = cardOffsetY.dp)
                    .alpha(cardAlpha),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = LoginCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(LoginAccentGreen, LoginPrimaryGreen)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LanguageManager.get("login"),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoginDarkGray
                        )
                    }

                    Text(
                        text = "Access your farming dashboard",
                        fontSize = 13.sp,
                        color = LoginMediumGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, top = 4.dp, bottom = 24.dp)
                    )

                    // Email
                    FarmerTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        placeholder = "you@example.com",
                        leadingIcon = {
                            Icon(Icons.Filled.Email, null, tint = LoginSecondaryGreen, modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    FarmerTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Enter your password",
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, null, tint = LoginSecondaryGreen, modifier = Modifier.size(20.dp))
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = LoginMediumGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    // Forgot password
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = {}) {
                            Text(
                                text = LanguageManager.get("forgot_password"),
                                fontSize = 13.sp,
                                color = LoginSecondaryGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login button
                    Button(
                        onClick = { isLoading = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(btnScale),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = LoginDisabledBtn
                        ),
                        contentPadding = PaddingValues(0.dp),
                        enabled = email.isNotBlank() && password.isNotBlank()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(LoginPrimaryGreen, LoginSecondaryGreen, LoginAccentGreen)
                                    ),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = LanguageManager.get("login"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = LoginBorderColor)
                        Text(
                            "  or  ",
                            fontSize = 12.sp,
                            color = LoginMediumGray.copy(0.5f)
                        )
                        Divider(modifier = Modifier.weight(1f), color = LoginBorderColor)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Navigate to Sign Up
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New to Farmer? ",
                            fontSize = 14.sp,
                            color = LoginMediumGray
                        )
                        Text(
                            text = LanguageManager.get("create_account"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoginPrimaryGreen,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNavigateToSignUp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom tagline
            Text(
                text = "🌱 Empowering Indian Farmers",
                fontSize = 12.sp,
                color = LoginMediumGray.copy(0.55f),
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Reusable Text Field ───────────────────────────────────────────────────────
@Composable
private fun FarmerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFFBDBDBD)) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LoginSecondaryGreen,
            unfocusedBorderColor = LoginBorderColor,
            focusedLabelColor = LoginSecondaryGreen,
            unfocusedLabelColor = LoginMediumGray,
            focusedContainerColor = LoginFieldBg,
            unfocusedContainerColor = LoginFieldBg,
            cursorColor = LoginPrimaryGreen
        )
    )
}

// ── Background Decorations ────────────────────────────────────────────────────
@Composable
private fun LoginBackgroundDecor() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-right soft blob
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 260.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(LoginAccentGreen.copy(alpha = 0.07f))
        )
        // Bottom-left blob
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-60).dp, y = 560.dp)
                .clip(CircleShape)
                .background(LoginSecondaryGreen.copy(alpha = 0.06f))
        )
        // Mid small circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 20.dp, y = 300.dp)
                .clip(CircleShape)
                .background(LoginPrimaryGreen.copy(alpha = 0.04f))
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}