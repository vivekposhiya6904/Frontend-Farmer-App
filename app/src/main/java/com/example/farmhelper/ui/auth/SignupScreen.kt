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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmhelper.ui.auth.viewmodel.AuthViewModel
import com.example.farmhelper.ui.localization.LanguageManager
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState


// ── Palette ──────────────────────────────────────────────────────────────────
private val SignUpPrimaryGreen   = Color(0xFF2E7D32)
private val SignUpSecondaryGreen = Color(0xFF4CAF50)
private val SignUpAccentGreen    = Color(0xFF8BC34A)
private val SignUpBackground     = Color(0xFFF8FFF8)
private val SignUpCardBg         = Color(0xFFFFFFFF)
private val SignUpDarkGray       = Color(0xFF1A1A1A)
private val SignUpMediumGray     = Color(0xFF616161)
private val SignUpBorderColor    = Color(0xFFE0F0E0)
private val SignUpFieldBg        = Color(0xFFF3FAF3)
private val SignUpErrorColor     = Color(0xFFE53935)

// ── Sign Up Screen ────────────────────────────────────────────────────────────
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = viewModel()
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val registerResponse by authViewModel.registerResponse.collectAsState()

    LaunchedEffect(registerResponse) {
        registerResponse?.let {
            onSignUpSuccess()
        }
    }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordStrength = when {
        password.length >= 12 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 3
        password.length >= 8 && password.any { it.isDigit() } -> 2
        password.length >= 6 -> 1
        else -> 0
    }

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
        targetValue = if (visible) 0f else 100f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "cardOffsetY"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(650, delayMillis = 100),
        label = "cardAlpha"
    )

    val btnScale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btnScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringPulse"
    )

    // Strength bar animated widths
    val strength1Alpha by animateFloatAsState(targetValue = if (passwordStrength >= 1) 1f else 0.18f, animationSpec = tween(300), label = "s1")
    val strength2Alpha by animateFloatAsState(targetValue = if (passwordStrength >= 2) 1f else 0.18f, animationSpec = tween(300), label = "s2")
    val strength3Alpha by animateFloatAsState(targetValue = if (passwordStrength >= 3) 1f else 0.18f, animationSpec = tween(300), label = "s3")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SignUpBackground, Color(0xFFE8F5E9), Color(0xFFDCEDDC))
                )
            )
    ) {
        SignUpBackgroundDecor()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Logo Block ────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .scale(ringPulse)
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(SignUpAccentGreen.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(SignUpSecondaryGreen.copy(0.22f), SignUpPrimaryGreen.copy(0.10f))
                            )
                        )
                )
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = "Farmer Logo",
                    tint = SignUpPrimaryGreen,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FARMER",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = SignUpPrimaryGreen,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Join the Smart Farming Revolution",
                fontSize = 13.sp,
                color = SignUpMediumGray,
                letterSpacing = 0.3.sp,
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Form Card ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = cardOffsetY.dp)
                    .alpha(cardAlpha),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SignUpCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                                        listOf(SignUpAccentGreen, SignUpPrimaryGreen)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text =  LanguageManager.get("create_account"),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignUpDarkGray
                        )
                    }

                    Text(
                        text = "Start your farming journey today",
                        fontSize = 13.sp,
                        color = SignUpMediumGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, top = 4.dp, bottom = 24.dp)
                    )

                    // Full Name
                    SignUpTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        placeholder = "Ramesh Kumar",
                        leadingIcon = {
                            Icon(Icons.Filled.Person, null, tint = SignUpSecondaryGreen, modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email
                    SignUpTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        placeholder = "you@example.com",
                        leadingIcon = {
                            Icon(Icons.Filled.Email, null, tint = SignUpSecondaryGreen, modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mobile NUmber
                    SignUpTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = "Mobile NUmber",
                        placeholder = "+91 98765 XXXXX",
                        leadingIcon = {
                            Icon(Icons.Filled.Phone, null, tint = SignUpSecondaryGreen, modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    SignUpTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Min. 8 characters",
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, null, tint = SignUpSecondaryGreen, modifier = Modifier.size(20.dp))
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = SignUpMediumGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Password strength bar
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val strengthColor = when (passwordStrength) {
                                1 -> Color(0xFFF44336)
                                2 -> Color(0xFFFF9800)
                                3 -> SignUpSecondaryGreen
                                else -> SignUpBorderColor
                            }
                            repeat(3) { i ->
                                val segAlpha = when (i) { 0 -> strength1Alpha; 1 -> strength2Alpha; else -> strength3Alpha }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(strengthColor.copy(alpha = segAlpha))
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (passwordStrength) { 1 -> "Weak"; 2 -> "Fair"; 3 -> "Strong"; else -> "" },
                                fontSize = 11.sp,
                                color = when (passwordStrength) {
                                    1 -> Color(0xFFF44336); 2 -> Color(0xFFFF9800); else -> SignUpSecondaryGreen
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirm Password
                    SignUpTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        placeholder = "Re-enter password",
                        leadingIcon = {
                            Icon(Icons.Filled.LockOpen, null, tint = if (passwordMismatch) SignUpErrorColor else SignUpSecondaryGreen, modifier = Modifier.size(20.dp))
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (confirmPassword.isNotEmpty() && !passwordMismatch) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = SignUpSecondaryGreen, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(2.dp))
                                }
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = SignUpMediumGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        isError = passwordMismatch,
                        borderColor = if (passwordMismatch) SignUpErrorColor else SignUpBorderColor
                    )

                    if (passwordMismatch) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Passwords do not match",
                            fontSize = 12.sp,
                            color = SignUpErrorColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Create Account button
                    val formValid = fullName.isNotBlank() && email.isNotBlank()
                            && password.length >= 6 && !passwordMismatch

                    Button(
                        onClick = {
                            authViewModel.registerUser(
                                fullName = fullName,
                                email = email,
                                mobile = mobile,
                                password = password,
                                confirmPassword = confirmPassword
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(btnScale),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled = formValid
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (formValid)
                                        Brush.horizontalGradient(
                                            listOf(SignUpPrimaryGreen, SignUpSecondaryGreen, SignUpAccentGreen)
                                        )
                                    else
                                        Brush.horizontalGradient(listOf(Color(0xFFB2DFDB), Color(0xFFB2DFDB))),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Filled.PersonAdd, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text =  LanguageManager.get("create_account"),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                    error?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = SignUpBorderColor)
                        Text("  or  ", fontSize = 12.sp, color = SignUpMediumGray.copy(0.5f))
                        Divider(modifier = Modifier.weight(1f), color = SignUpBorderColor)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Navigate to Login
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already a farmer? ",
                            fontSize = 14.sp,
                            color = SignUpMediumGray
                        )
                        Text(
                            text = LanguageManager.get("login"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignUpPrimaryGreen,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNavigateToLogin
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "🌾 Growing Together, Harvesting Success",
                fontSize = 12.sp,
                color = SignUpMediumGray.copy(0.50f),
                letterSpacing = 0.3.sp,
                modifier = Modifier.alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Reusable Text Field ───────────────────────────────────────────────────────
@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    borderColor: Color = SignUpBorderColor
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
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) SignUpErrorColor else SignUpSecondaryGreen,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = if (isError) SignUpErrorColor else SignUpSecondaryGreen,
            unfocusedLabelColor = SignUpMediumGray,
            focusedContainerColor = SignUpFieldBg,
            unfocusedContainerColor = SignUpFieldBg,
            cursorColor = SignUpPrimaryGreen,
            errorBorderColor = SignUpErrorColor,
            errorLabelColor = SignUpErrorColor
        )
    )
}

// ── Background Decorations ────────────────────────────────────────────────────
@Composable
private fun SignUpBackgroundDecor() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(SignUpAccentGreen.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 280.dp, y = 600.dp)
                .clip(CircleShape)
                .background(SignUpSecondaryGreen.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = 300.dp, y = 180.dp)
                .clip(CircleShape)
                .background(SignUpPrimaryGreen.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 10.dp, y = 700.dp)
                .clip(CircleShape)
                .background(SignUpAccentGreen.copy(alpha = 0.05f))
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}