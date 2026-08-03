package com.example.farmhelper.ui.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmhelper.ui.ai.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AIAssistantViewModel : ViewModel() {

    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    private val _settingsState = MutableStateFlow(AISettings())
    val settingsState: StateFlow<AISettings> = _settingsState.asStateFlow()

    val availableLanguages = listOf(
        LanguageOption("en", "English", "English"),
        LanguageOption("gu", "Gujarati", "ગુજરાતી"),
        LanguageOption("hi", "Hindi", "हिन्दी")
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val quickPrompts = listOf(
            QuickPromptChip("1", "Weather Advice", "How is the weather affecting cotton crops this week?"),
            QuickPromptChip("2", "Crop Disease", "Why are my cotton leaves turning yellow?"),
            QuickPromptChip("3", "Crop Prices", "What is the latest APMC market price for wheat?"),
            QuickPromptChip("4", "Fertilizer", "Best fertilizer schedule for wheat crop"),
            QuickPromptChip("5", "Irrigation", "How much irrigation does sugarcane need in monsoon?"),
            QuickPromptChip("6", "Government Schemes", "PM Kisan eligibility and application steps"),
            QuickPromptChip("7", "Market Advice", "Should I sell groundnut now or hold?"),
            QuickPromptChip("8", "Organic Farming", "How to prepare organic bio-pesticide at home?"),
            QuickPromptChip("9", "Pest Control", "Effective organic remedy for pink bollworm in cotton")
        )

        val suggestions = listOf(
            SuggestionCard(
                id = "s1",
                title = "Diagnose Crop Disease",
                subtitle = "Upload leaf photo for instant AI analysis",
                promptText = "Help me identify a disease on my crop leaves.",
                category = "Disease Identification"
            ),
            SuggestionCard(
                id = "s2",
                title = "Weather Recommendation",
                subtitle = "Custom field advice based on local rain & temp",
                promptText = "Give me today's weather-based farming advisory.",
                category = "Weather"
            ),
            SuggestionCard(
                id = "s3",
                title = "Today's Market Advice",
                subtitle = "Price trends & optimal selling timing",
                promptText = "Which crop gives the best APMC market returns today?",
                category = "Market"
            ),
            SuggestionCard(
                id = "s4",
                title = "Government Schemes",
                subtitle = "Subsidies & financial support for farmers",
                promptText = "Tell me about available agricultural subsidies for Gujarat farmers.",
                category = "Schemes"
            )
        )

        val initialHistory = listOf(
            Conversation("c1", "Cotton Leaf Yellowing Analysis", "Cotton leaf yellowing is often caused by nitrogen deficiency...", "Today 10:30 AM", isPinned = true, group = ConversationGroup.TODAY),
            Conversation("c2", "APMC Wheat Market Price Forecast", "Rajkot APMC wheat price is currently ₹2,250/quintal...", "Yesterday 4:15 PM", isPinned = false, group = ConversationGroup.YESTERDAY),
            Conversation("c3", "Organic Neem Pest Spray Recipe", "To make neem oil spray, mix 50ml neem oil with 10L water...", "Jul 28", isPinned = false, group = ConversationGroup.LAST_WEEK)
        )

        _chatUiState.update {
            it.copy(
                quickPrompts = quickPrompts,
                suggestedCards = suggestions
            )
        }

        _historyUiState.update {
            it.copy(conversations = initialHistory)
        }
    }

    fun onInputChange(text: String) {
        _chatUiState.update { it.copy(currentInput = text) }
    }

    fun onSelectQuickPrompt(promptText: String) {
        _chatUiState.update { it.copy(currentInput = promptText) }
    }

    fun onAddAttachment(attachment: Attachment) {
        _chatUiState.update { state ->
            val updated = state.selectedAttachments + attachment
            state.copy(selectedAttachments = updated)
        }
    }

    fun onRemoveAttachment(attachmentId: String) {
        _chatUiState.update { state ->
            val updated = state.selectedAttachments.filterNot { it.id == attachmentId }
            state.copy(selectedAttachments = updated)
        }
    }

    fun onSendMessage() {
        val currentState = _chatUiState.value
        val input = currentState.currentInput.trim()
        val attachments = currentState.selectedAttachments

        if (input.isEmpty() && attachments.isEmpty()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.USER,
            content = input,
            timestamp = getCurrentTimeString(),
            attachments = attachments
        )

        _chatUiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                currentInput = "",
                selectedAttachments = emptyList(),
                isThinking = true
            )
        }

        // Simulate local Assistant response for UI preview without backend API calls
        viewModelScope.launch {
            delay(1200) // Thinking indicator duration
            _chatUiState.update { it.copy(isThinking = false, isTyping = true) }

            val responseText = generateLocalAssistantResponse(input, attachments)
            delay(800) // Typing simulation

            val assistantMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = Sender.ASSISTANT,
                content = responseText,
                timestamp = getCurrentTimeString()
            )

            _chatUiState.update { state ->
                state.copy(
                    messages = state.messages + assistantMessage,
                    isTyping = false
                )
            }
        }
    }

    private fun generateLocalAssistantResponse(query: String, attachments: List<Attachment>): String {
        val lower = query.lowercase()
        return when {
            attachments.isNotEmpty() -> {
                "📷 **Media Attachment Received**: Analyzed `${attachments.first().name}`.\n\n" +
                        "Based on visual analysis:\n" +
                        "- **Condition**: Healthy crop growth pattern detected.\n" +
                        "- **Recommendation**: Maintain current irrigation schedule. Inspect leaves every 3 days for early aphid signs."
            }
            lower.contains("yellow") || lower.contains("cotton") -> {
                "🌿 **Cotton Leaf Yellowing Advisory**\n\n" +
                        "Cotton leaf yellowing can occur due to key nutrient factors:\n\n" +
                        "1. **Nitrogen Deficiency**: Older leaves turn pale yellow starting from tips.\n" +
                        "2. **Waterlogging**: Excessive irrigation or poor drainage suffocates roots.\n" +
                        "3. **Sucking Pests**: Jassids or whiteflies under leaves.\n\n" +
                        "| Cause | Action Steps | Dosage |\n" +
                        "| :--- | :--- | :--- |\n" +
                        "| Nitrogen Lack | Apply Urea top dressing | 25 kg/acre |\n" +
                        "| Waterlogging | Ensure soil drainage channels | Immediate |\n" +
                        "| Pest Attack | Neem oil spray (10,000 PPM) | 5ml / Liter |\n\n" +
                        "💡 *Tip: Spray in early morning or late evening for best efficacy.*"
            }
            lower.contains("weather") -> {
                "🌤️ **Local Agricultural Weather Guidance**\n\n" +
                        "- **Current Temp**: 31°C | **Humidity**: 68%\n" +
                        "- **Rain Chance**: 20% today, light showers expected in 48h.\n\n" +
                        "✅ **Farming Action**: Safe for pesticide spraying today morning. Avoid heavy irrigation before rain."
            }
            lower.contains("price") || lower.contains("apmc") || lower.contains("market") -> {
                "📊 **Market Price Overview**\n\n" +
                        "- **Cotton**: ₹7,200 - ₹7,650 / quintal\n" +
                        "- **Wheat**: ₹2,200 - ₹2,380 / quintal\n" +
                        "- **Groundnut**: ₹6,100 - ₹6,500 / quintal\n\n" +
                        "📈 *Trend*: Prices expected to remain steady over the next week."
            }
            else -> {
                "Hello! I am your **AI Farming Assistant** 🌾.\n\n" +
                        "I can help you with:\n" +
                        "- Crop Disease Diagnosis & Treatment\n" +
                        "- Weather & Irrigation Schedules\n" +
                        "- APMC Market Price Updates\n" +
                        "- Organic Farming Recipes & Subsidy Schemes\n\n" +
                        "Ask me anything about your farm or upload a leaf photo to begin!"
            }
        }
    }

    fun onOpenVoiceSheet() {
        _chatUiState.update { it.copy(isVoiceSheetOpen = true, voiceState = VoiceState.LISTENING) }
    }

    fun onCloseVoiceSheet() {
        _chatUiState.update { it.copy(isVoiceSheetOpen = false, voiceState = VoiceState.IDLE) }
    }

    fun onToggleVoiceState() {
        _chatUiState.update { state ->
            val nextState = when (state.voiceState) {
                VoiceState.IDLE -> VoiceState.LISTENING
                VoiceState.LISTENING -> VoiceState.PROCESSING
                VoiceState.PROCESSING -> VoiceState.SPEAKING
                VoiceState.SPEAKING -> VoiceState.IDLE
                VoiceState.ERROR -> VoiceState.LISTENING
            }
            state.copy(voiceState = nextState)
        }
    }

    fun onOpenLanguageSheet() {
        _chatUiState.update { it.copy(isLanguageSheetOpen = true) }
    }

    fun onCloseLanguageSheet() {
        _chatUiState.update { it.copy(isLanguageSheetOpen = false) }
    }

    fun onSelectLanguage(lang: LanguageOption) {
        _chatUiState.update { it.copy(currentLanguage = lang, isLanguageSheetOpen = false) }
        _settingsState.update { it.copy(languageCode = lang.code) }
    }

    fun onOpenAttachmentSheet() {
        _chatUiState.update { it.copy(isAttachmentSheetOpen = true) }
    }

    fun onCloseAttachmentSheet() {
        _chatUiState.update { it.copy(isAttachmentSheetOpen = false) }
    }

    fun onClearMessages() {
        _chatUiState.update { it.copy(messages = emptyList()) }
    }

    // --- HISTORY HANDLERS ---
    fun onSearchHistory(query: String) {
        _historyUiState.update { it.copy(searchQuery = query) }
    }

    fun onDeleteConversation(id: String) {
        _historyUiState.update { state ->
            val updated = state.conversations.filterNot { it.id == id }
            state.copy(conversations = updated)
        }
    }

    fun onPinConversation(id: String) {
        _historyUiState.update { state ->
            val updated = state.conversations.map {
                if (it.id == id) it.copy(isPinned = !it.isPinned) else it
            }
            state.copy(conversations = updated)
        }
    }

    // --- SETTINGS HANDLERS ---
    fun onUpdateSettings(
        voiceLanguageCode: String? = null,
        speechSpeed: Float? = null,
        speechGender: String? = null,
        autoSpeak: Boolean? = null,
        saveHistory: Boolean? = null,
        darkModeEnabled: Boolean? = null
    ) {
        _settingsState.update { state ->
            state.copy(
                voiceLanguageCode = voiceLanguageCode ?: state.voiceLanguageCode,
                speechSpeed = speechSpeed ?: state.speechSpeed,
                speechGender = speechGender ?: state.speechGender,
                autoSpeak = autoSpeak ?: state.autoSpeak,
                saveHistory = saveHistory ?: state.saveHistory,
                darkModeEnabled = darkModeEnabled ?: state.darkModeEnabled
            )
        }
    }

    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
