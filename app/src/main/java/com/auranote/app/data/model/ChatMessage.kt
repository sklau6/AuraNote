package com.auranote.app.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole { USER, ASSISTANT }

val suggestedQuestions = listOf(
    "What were the main topics discussed?",
    "What action items were assigned?",
    "What decisions were made?",
    "Who said what about the budget?",
    "What are the next steps?",
    "Summarize the key takeaways"
)
