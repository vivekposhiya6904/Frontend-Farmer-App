package com.example.farmhelper.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.farmhelper.session.SessionManager
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.farmhelper.ui.auth.models.AuthState
import com.example.farmhelper.ui.auth.models.AuthStateManager
import com.example.farmhelper.ui.auth.LoginScreen
import com.example.farmhelper.ui.auth.SignUpScreen
import com.example.farmhelper.ui.home.HomeScreen
import com.example.farmhelper.ui.language.LanguageSelectionScreen
import com.example.farmhelper.ui.localization.LanguageManager
import com.example.farmhelper.ui.onboarding.OnboardingScreen
import com.example.farmhelper.ui.splash.FarmerSplashScreen
import androidx.activity.compose.BackHandler
import com.example.farmhelper.R

enum class AppScreen {
    SPLASH,
    LANGUAGE,
    ONBOARDING,
    LOGIN,
    SIGNUP,
    HOME,
    WEATHER
}

@Composable
fun AppFlow() {

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val authState by AuthStateManager.authState.collectAsState()

    var currentScreen by remember {
        mutableStateOf(AppScreen.SPLASH)
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SessionExpired -> {
                if (currentScreen == AppScreen.HOME || currentScreen == AppScreen.WEATHER) {
                    Toast.makeText(context, context.getString(R.string.session_expired_message), Toast.LENGTH_LONG).show()
                    currentScreen = AppScreen.LOGIN
                    AuthStateManager.setUnauthenticated()
                }
            }
            is AuthState.Unauthenticated -> {
                if (currentScreen == AppScreen.HOME || currentScreen == AppScreen.WEATHER) {
                    currentScreen = AppScreen.LOGIN
                }
            }
            is AuthState.Authenticated -> {
                if (currentScreen == AppScreen.LOGIN || currentScreen == AppScreen.SIGNUP || currentScreen == AppScreen.SPLASH) {
                    currentScreen = AppScreen.HOME
                }
            }
            else -> {}
        }
    }

    when (currentScreen) {

        AppScreen.SPLASH -> {
            FarmerSplashScreen(
                onNavigateToHome = {
                    currentScreen = AppScreen.HOME
                },
                onNavigationToLanguage = {
                    currentScreen = AppScreen.LANGUAGE
                }

            )
        }

        AppScreen.LANGUAGE -> {
            LanguageSelectionScreen(
                onLanguageSelected = { language ->
                    coroutineScope.launch {
                        LanguageManager.saveLanguage(context, language)
                    }
                    currentScreen = AppScreen.ONBOARDING
                }
            )
        }

        AppScreen.ONBOARDING -> {
            OnboardingScreen(
                onGetStarted = {
                    currentScreen = AppScreen.LOGIN
                },
                onSkip = {
                    currentScreen = AppScreen.LOGIN
                }
            )
        }

        AppScreen.LOGIN -> {
            LoginScreen(
                onNavigateToSignUp = {
                    currentScreen = AppScreen.SIGNUP
                },
                onLoginSuccess = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.SIGNUP -> {
            BackHandler {
                currentScreen = AppScreen.LOGIN
            }
            SignUpScreen(
                onNavigateToLogin = {
                    currentScreen = AppScreen.LOGIN
                },
                onSignUpSuccess  = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.HOME -> {
            HomeScreen(
                onLogoutClick = {
                    coroutineScope.launch {
                        sessionManager.logout()
                        AuthStateManager.setUnauthenticated()
                    }
                },
                onFeatureClick = { feature ->
                    if (feature == "weather") {
                        currentScreen = AppScreen.WEATHER
                    }
                }
            )
        }

        AppScreen.WEATHER -> {
            BackHandler {
                currentScreen = AppScreen.HOME
            }
            com.example.farmhelper.ui.weather.WeatherScreen(
                onBackClick = {
                    currentScreen = AppScreen.HOME
                }
            )
        }
    }
}