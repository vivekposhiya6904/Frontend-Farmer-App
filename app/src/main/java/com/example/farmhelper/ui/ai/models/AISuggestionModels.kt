package com.example.farmhelper.ui.ai.models

data class SuggestionCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val promptText: String,
    val category: String,
    val iconName: String = "smart_toy"
)

data class QuickPromptChip(
    val id: String,
    val label: String,
    val promptText: String
)
