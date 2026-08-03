package com.example.farmhelper.ui.ai.models

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

enum class ConversationGroup {
    TODAY,
    YESTERDAY,
    LAST_WEEK,
    OLDER
}

data class Conversation(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: String,
    val isPinned: Boolean = false,
    val group: ConversationGroup = ConversationGroup.TODAY,
    val category: String = "General"
)

data class LanguageOption(
    val code: String,
    val displayName: String,
    val nativeName: String
)

data class AISettings(
    val languageCode: String = "en",
    val voiceLanguageCode: String = "en-IN",
    val speechSpeed: Float = 1.0f,
    val speechGender: String = "Female",
    val autoSpeak: Boolean = false,
    val saveHistory: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val selectedModel: String = "CropIntelligence v2.4"
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentInput: String = "",
    val selectedAttachments: List<Attachment> = emptyList(),
    val isTyping: Boolean = false,
    val isThinking: Boolean = false,
    val voiceState: VoiceState = VoiceState.IDLE,
    val currentLanguage: LanguageOption = LanguageOption("en", "English", "English"),
    val suggestedCards: List<SuggestionCard> = emptyList(),
    val quickPrompts: List<QuickPromptChip> = emptyList(),
    val isVoiceSheetOpen: Boolean = false,
    val isLanguageSheetOpen: Boolean = false,
    val isAttachmentSheetOpen: Boolean = false,
    val activeConversationId: String? = null
)

data class HistoryUiState(
    val conversations: List<Conversation> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
