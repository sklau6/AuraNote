package com.auranote.app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ── Gemini REST API models ─────────────────────────────────────────────────

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @SerializedName("systemInstruction") val systemInstruction: GeminiContent? = null
)

data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 8192
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerializedName("finishReason") val finishReason: String? = null
)

data class GeminiError(
    val code: Int = 0,
    val message: String = "",
    val status: String = ""
)

// ── Retrofit interface ─────────────────────────────────────────────────────

interface GeminiService {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
