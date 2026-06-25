package com.example.farmhelper.ui.navigation

import androidx.compose.runtime.*
import com.example.farmhelper.ui.auth.LoginScreen
import com.example.farmhelper.ui.auth.SignUpScreen
import com.example.farmhelper.ui.home.HomeScreen
import com.example.farmhelper.ui.language.LanguageSelectionScreen
import com.example.farmhelper.ui.localization.LanguageManager
import com.example.farmhelper.ui.onboarding.OnboardingScreen
import com.example.farmhelper.ui.splash.FarmerSplashScreen

enum class AppScreen {
    SPLASH,
    LANGUAGE,
    ONBOARDING,
    LOGIN,
    SIGNUP,
    HOME
}

@Composable
fun AppFlow() {

    var currentScreen by remember {
        mutableStateOf(AppScreen.SPLASH)
    }

    when (currentScreen) {

        AppScreen.SPLASH -> {
            FarmerSplashScreen(
                onFinished = {
                    currentScreen = AppScreen.LANGUAGE
                }
            )
        }

        AppScreen.LANGUAGE -> {
            LanguageSelectionScreen(
                onLanguageSelected = { language ->

                    LanguageManager.currentLanguage = language

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
                }
            )
        }

        AppScreen.SIGNUP -> {
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
            HomeScreen()
        }
    }
}