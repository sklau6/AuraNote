package com.auranote.app.data.api

import com.google.gson.annotations.SerializedName

// ── Whisper transcription ──────────────────────────────────────────────────

data class WhisperResponse(
    val text: String,
    val language: String? = null,
    val duration: Float? = null,
    val segments: List<WhisperSegment>? = null
)

data class WhisperSegment(
    val id: Int,
    val start: Float,
    val end: Float,
    val text: String,
    @SerializedName("avg_logprob") val avgLogprob: Float? = null
)

// ── Chat completions ───────────────────────────────────────────────────────

data class ChatCompletionRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<ChatApiMessage>,
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens") val maxTokens: Int = 2000
)

data class ChatApiMessage(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<ChatChoice>,
    val usage: TokenUsage? = null
)

data class ChatChoice(
    val index: Int,
    val message: ChatApiMessage,
    @SerializedName("finish_reason") val finishReason: String
)

data class TokenUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)

// ── AI response structures (parsed from GPT JSON) ──────────────────────────

data class SummaryResponse(
    val overview: String = "",
    @SerializedName("key_points") val keyPoints: List<String> = emptyList(),
    @SerializedName("action_items") val actionItems: List<String> = emptyList(),
    val decisions: List<String> = emptyList(),
    @SerializedName("next_steps") val nextSteps: List<String> = emptyList()
)

data class FlashcardsResponse(
    val flashcards: List<FlashcardItem> = emptyList()
)

data class FlashcardItem(
    val question: String,
    val answer: String
)

data class QuizResponse(
    val questions: List<QuizItem> = emptyList()
)

data class QuizItem(
    val question: String,
    val options: List<String>,
    @SerializedName("correct_index") val correctIndex: Int,
    val explanation: String = ""
)
