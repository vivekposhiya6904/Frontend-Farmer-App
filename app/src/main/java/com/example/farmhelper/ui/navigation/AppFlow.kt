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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmhelper.R
import com.example.farmhelper.ui.community.CommunityFeedScreen
import com.example.farmhelper.ui.community.screens.FarmerProfileScreen
import com.example.farmhelper.ui.community.viewmodel.CommunityViewModel

enum class AppScreen {
    SPLASH,
    LANGUAGE,
    ONBOARDING,
    LOGIN,
    SIGNUP,
    HOME,
    WEATHER,
    COMMUNITY,
    COMMUNITY_SEARCH,
    COMMUNITY_NOTIFICATIONS,
    COMMUNITY_BLOCKED_USERS,
    PROFILE,
    AI_ASSISTANT,
    AI_HISTORY,
    AI_SETTINGS
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

    var selectedProfileUserId by remember {
        mutableStateOf<String?>(null)
    }

    val communityViewModel: CommunityViewModel = viewModel()
    val notificationViewModel: com.example.farmhelper.ui.community.viewmodel.CommunityNotificationViewModel = viewModel()
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SessionExpired -> {
                if (currentScreen == AppScreen.HOME || currentScreen == AppScreen.WEATHER || currentScreen == AppScreen.COMMUNITY || currentScreen == AppScreen.PROFILE) {
                    Toast.makeText(context, context.getString(R.string.session_expired_message), Toast.LENGTH_LONG).show()
                    currentScreen = AppScreen.LOGIN
                    AuthStateManager.setUnauthenticated()
                }
            }
            is AuthState.Unauthenticated -> {
                if (currentScreen == AppScreen.HOME || currentScreen == AppScreen.WEATHER || currentScreen == AppScreen.COMMUNITY || currentScreen == AppScreen.PROFILE) {
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
                    when (feature) {
                        "weather" -> currentScreen = AppScreen.WEATHER
                        "community" -> currentScreen = AppScreen.COMMUNITY
                        "ai_assistant", "ai" -> currentScreen = AppScreen.AI_ASSISTANT
                        "profile" -> {
                            selectedProfileUserId = null
                            currentScreen = AppScreen.PROFILE
                        }
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

        AppScreen.COMMUNITY -> {
            BackHandler {
                currentScreen = AppScreen.HOME
            }
            CommunityFeedScreen(
                onBackClick = {
                    currentScreen = AppScreen.HOME
                },
                onOpenProfile = { targetUserId ->
                    selectedProfileUserId = targetUserId
                    currentScreen = AppScreen.PROFILE
                },
                onOpenSearch = {
                    currentScreen = AppScreen.COMMUNITY_SEARCH
                },
                onOpenNotifications = {
                    currentScreen = AppScreen.COMMUNITY_NOTIFICATIONS
                },
                unreadNotificationCount = unreadNotificationCount,
                viewModel = communityViewModel
            )
        }

        AppScreen.COMMUNITY_SEARCH -> {
            BackHandler {
                currentScreen = AppScreen.COMMUNITY
            }
            com.example.farmhelper.ui.community.screens.CommunitySearchScreen(
                onBackClick = {
                    currentScreen = AppScreen.COMMUNITY
                },
                onOpenProfile = { targetUserId ->
                    selectedProfileUserId = targetUserId
                    currentScreen = AppScreen.PROFILE
                }
            )
        }

        AppScreen.COMMUNITY_NOTIFICATIONS -> {
            BackHandler {
                currentScreen = AppScreen.COMMUNITY
            }
            com.example.farmhelper.ui.community.screens.CommunityNotificationScreen(
                onBackClick = {
                    currentScreen = AppScreen.COMMUNITY
                },
                onNavigateToTarget = { postId, commentId, actorUserId, type ->
                    if (!actorUserId.isNullOrEmpty() && (type == "system" || type == "profile")) {
                        selectedProfileUserId = actorUserId
                        currentScreen = AppScreen.PROFILE
                    } else {
                        currentScreen = AppScreen.COMMUNITY
                    }
                },
                viewModel = notificationViewModel
            )
        }

        AppScreen.COMMUNITY_BLOCKED_USERS -> {
            BackHandler {
                currentScreen = AppScreen.COMMUNITY
            }
            com.example.farmhelper.ui.community.screens.BlockedUsersScreen(
                onBackClick = {
                    currentScreen = AppScreen.COMMUNITY
                }
            )
        }

        AppScreen.PROFILE -> {
            BackHandler {
                currentScreen = AppScreen.COMMUNITY
            }
            FarmerProfileScreen(
                targetUserId = selectedProfileUserId,
                onBackClick = {
                    currentScreen = AppScreen.COMMUNITY
                },
                onOpenBlockedUsers = {
                    currentScreen = AppScreen.COMMUNITY_BLOCKED_USERS
                },
                viewModel = communityViewModel
            )
        }

        AppScreen.AI_ASSISTANT -> {
            BackHandler {
                currentScreen = AppScreen.HOME
            }
            com.example.farmhelper.ui.ai.AIAssistantScreen(
                onBackClick = {
                    currentScreen = AppScreen.HOME
                },
                onOpenHistory = {
                    currentScreen = AppScreen.AI_HISTORY
                },
                onOpenSettings = {
                    currentScreen = AppScreen.AI_SETTINGS
                }
            )
        }

        AppScreen.AI_HISTORY -> {
            BackHandler {
                currentScreen = AppScreen.AI_ASSISTANT
            }
            com.example.farmhelper.ui.ai.AIHistoryScreen(
                onBackClick = {
                    currentScreen = AppScreen.AI_ASSISTANT
                },
                onSelectConversation = { id ->
                    currentScreen = AppScreen.AI_ASSISTANT
                }
            )
        }

        AppScreen.AI_SETTINGS -> {
            BackHandler {
                currentScreen = AppScreen.AI_ASSISTANT
            }
            com.example.farmhelper.ui.ai.AISettingsScreen(
                onBackClick = {
                    currentScreen = AppScreen.AI_ASSISTANT
                }
            )
        }
    }
}