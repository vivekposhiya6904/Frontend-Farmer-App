package com.example.farmhelper.ui.ai.models

enum class Sender {
    USER,
    ASSISTANT
}

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT
}

data class Attachment(
    val id: String,
    val uri: String? = null,
    val type: AttachmentType = AttachmentType.IMAGE,
    val name: String,
    val size: String? = null,
    val thumbnailUrl: String? = null
)

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val content: String,
    val timestamp: String,
    val attachments: List<Attachment> = emptyList(),
    val isThinking: Boolean = false,
    val isError: Boolean = false
)
