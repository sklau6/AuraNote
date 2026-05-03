package com.auranote.app.data.repository

import android.util.Log
import com.auranote.app.data.api.ChatApiMessage
import com.auranote.app.data.api.ChatCompletionRequest
import com.auranote.app.data.api.FlashcardsResponse
import com.auranote.app.data.api.OpenAIService
import com.auranote.app.data.api.QuizResponse
import com.auranote.app.data.api.SummaryResponse
import com.auranote.app.data.api.WhisperResponse
import com.auranote.app.data.db.SummaryDao
import com.auranote.app.data.model.AISummary
import com.auranote.app.data.model.TranscriptSegment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val openAIService: OpenAIService,
    private val summaryDao: SummaryDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "AIRepository"
        private const val WHISPER_MODEL = "whisper-1"
        private const val GPT_MODEL = "gpt-4o-mini"
    }

    fun getSummary(recordingId: Long): Flow<AISummary?> =
        summaryDao.getSummaryByRecordingId(recordingId)

    suspend fun transcribeAudio(
        apiKey: String,
        audioFile: File,
        language: String = "auto"
    ): Result<WhisperResponse> = runCatching {
        val mediaType = "audio/m4a".toMediaTypeOrNull()
        val requestFile = audioFile.asRequestBody(mediaType)
        val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
        val modelBody = WHISPER_MODEL.toRequestBody("text/plain".toMediaTypeOrNull())
        val formatBody = "verbose_json".toRequestBody("text/plain".toMediaTypeOrNull())
        val langBody = if (language != "auto") {
            language.toRequestBody("text/plain".toMediaTypeOrNull())
        } else null

        openAIService.transcribeAudio(
            authorization = "Bearer $apiKey",
            file = filePart,
            model = modelBody,
            responseFormat = formatBody,
            language = langBody
        )
    }

    suspend fun assignSpeakers(
        apiKey: String,
        segments: List<TranscriptSegment>,
        fullText: String
    ): Result<List<TranscriptSegment>> = runCatching {
        val segmentLines = segments.mapIndexed { i, seg ->
            "[$i] (${seg.startTimeSeconds}s-${seg.endTimeSeconds}s): ${seg.text}"
        }.joinToString("\n")

        val prompt = """
You are a speaker diarization assistant. Analyze this transcript and assign speaker labels.
Rules:
- Use labels like "Speaker 1", "Speaker 2", etc.
- Be consistent: if the same person speaks again, use the same label
- Base assignment on conversational context and speaking patterns
- Return a JSON array: [{"index": 0, "speaker": "Speaker 1"}, ...]

Transcript segments:
$segmentLines
        """.trimIndent()

        val response = openAIService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = ChatCompletionRequest(
                model = GPT_MODEL,
                messages = listOf(ChatApiMessage("user", prompt)),
                maxTokens = 1000
            )
        )

        val jsonContent = response.choices.firstOrNull()?.message?.content ?: "[]"
        val cleanJson = extractJson(jsonContent)

        data class SpeakerAssignment(val index: Int, val speaker: String)

        val assignments = try {
            gson.fromJson(cleanJson, Array<SpeakerAssignment>::class.java)
                .associateBy { it.index }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse speaker assignments", e)
            emptyMap()
        }

        segments.mapIndexed { i, seg ->
            val speaker = assignments[i]?.speaker ?: "Speaker 1"
            seg.copy(speakerLabel = speaker)
        }
    }

    /**
     * Generate professional meeting minutes from a raw transcript.
     *
     * Uses a strict system prompt that pins the model to a fixed JSON schema:
     *   { executive_summary, key_decisions[], action_items[{task, owner, due}], next_steps[] }
     *
     * Transparently retries on transient network/JSON failures (up to 3 attempts).
     * Persists into the existing AISummary row, populating `overview`, `decisions`,
     * `actionItems`, and `nextSteps`.
     */
    suspend fun generateMeetingMinutes(
        apiKey: String,
        recordingId: Long,
        transcriptText: String,
        languageHint: String = "auto"
    ): Result<AISummary> = runCatching {
        val systemPrompt = """
You are a professional meeting-minutes writer. Your job is to convert a raw, possibly messy transcript into clean, executive-ready minutes.

ABSOLUTE RULES — FOLLOW EXACTLY:
1. Output a SINGLE JSON object. No prose, no markdown, no code fences.
2. Use this exact schema (field names lowercase, snake_case, do not rename):
{
  "overview": "EXECUTIVE SUMMARY: 2-4 crisp sentences capturing purpose, outcome, and tone of the meeting.",
  "key_points": ["Notable discussion point or topic raised.", "..."],
  "decisions": ["KEY DECISION: a complete declarative sentence describing something the group agreed on.", "..."],
  "action_items": ["TASK — OWNER (or 'Unassigned') — DUE (ISO date or 'TBD')", "..."],
  "next_steps": ["Concrete follow-up planned for after this meeting.", "..."]
}
3. Be concise and professional. Strip filler ("um", "you know"). Merge redundant statements.
4. Never invent participants, dates, or commitments not present in the transcript. If unknown, write "Unassigned" / "TBD".
5. Preserve the transcript's original language: ${if (languageHint == "auto") "match transcript" else languageHint}.
6. If the transcript is too short or off-topic, still output the schema; leave arrays empty rather than fabricate.
        """.trimIndent()

        val userPrompt = "TRANSCRIPT:\n\n${transcriptText.trim()}"

        val maxAttempts = 3
        var lastError: Throwable? = null
        var summaryResponse: SummaryResponse? = null

        for (attempt in 1..maxAttempts) {
            try {
                val response = openAIService.chatCompletion(
                    authorization = "Bearer $apiKey",
                    request = ChatCompletionRequest(
                        model = GPT_MODEL,
                        messages = listOf(
                            ChatApiMessage("system", systemPrompt),
                            ChatApiMessage("user", userPrompt)
                        ),
                        maxTokens = 2000
                    )
                )
                val content = response.choices.firstOrNull()?.message?.content ?: "{}"
                val cleanJson = extractJson(content)
                summaryResponse = gson.fromJson(cleanJson, SummaryResponse::class.java)
                break
            } catch (t: Throwable) {
                lastError = t
                Log.w(TAG, "Meeting minutes attempt $attempt failed: ${t.message}")
                if (attempt < maxAttempts) kotlinx.coroutines.delay(1500L * attempt)
            }
        }

        val parsed = summaryResponse
            ?: throw lastError ?: IllegalStateException("Could not generate meeting minutes")

        val summary = AISummary(
            recordingId = recordingId,
            overview = parsed.overview,
            keyPoints = gson.toJson(parsed.keyPoints),
            actionItems = gson.toJson(parsed.actionItems),
            decisions = gson.toJson(parsed.decisions),
            nextSteps = gson.toJson(parsed.nextSteps)
        )

        val existing = summaryDao.getSummarySync(recordingId)
        if (existing != null) {
            summaryDao.updateSummary(
                existing.copy(
                    overview = summary.overview,
                    keyPoints = summary.keyPoints,
                    actionItems = summary.actionItems,
                    decisions = summary.decisions,
                    nextSteps = summary.nextSteps,
                    generatedAt = System.currentTimeMillis()
                )
            )
        } else {
            summaryDao.insertSummary(summary)
        }
        summary
    }

    suspend fun generateSummary(
        apiKey: String,
        recordingId: Long,
        transcriptText: String
    ): Result<AISummary> = runCatching {
        val prompt = """
You are an expert meeting assistant. Analyze this transcript and provide a structured summary.

Return ONLY valid JSON in this exact format:
{
  "overview": "2-3 sentence summary of the meeting/session",
  "key_points": ["point 1", "point 2", "point 3"],
  "action_items": ["action 1 - assignee if known", "action 2"],
  "decisions": ["decision 1", "decision 2"],
  "next_steps": ["next step 1", "next step 2"]
}

Transcript:
$transcriptText
        """.trimIndent()

        val response = openAIService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = ChatCompletionRequest(
                model = GPT_MODEL,
                messages = listOf(ChatApiMessage("user", prompt)),
                maxTokens = 2000
            )
        )

        val content = response.choices.firstOrNull()?.message?.content ?: "{}"
        val cleanJson = extractJson(content)
        val summaryResponse = try {
            gson.fromJson(cleanJson, SummaryResponse::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse summary response", e)
            SummaryResponse(overview = content)
        }

        val summary = AISummary(
            recordingId = recordingId,
            overview = summaryResponse.overview,
            keyPoints = gson.toJson(summaryResponse.keyPoints),
            actionItems = gson.toJson(summaryResponse.actionItems),
            decisions = gson.toJson(summaryResponse.decisions),
            nextSteps = gson.toJson(summaryResponse.nextSteps)
        )
        summaryDao.insertSummary(summary)
        summary
    }

    suspend fun generateFlashcards(
        apiKey: String,
        recordingId: Long,
        transcriptText: String
    ): Result<String> = runCatching {
        val prompt = """
Create 8-12 study flashcards from this transcript/lecture content.

Return ONLY valid JSON:
{
  "flashcards": [
    {"question": "What is...?", "answer": "The answer is..."},
    ...
  ]
}

Content:
$transcriptText
        """.trimIndent()

        val response = openAIService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = ChatCompletionRequest(
                model = GPT_MODEL,
                messages = listOf(ChatApiMessage("user", prompt)),
                maxTokens = 2000
            )
        )

        val content = response.choices.firstOrNull()?.message?.content ?: "{}"
        val cleanJson = extractJson(content)

        val existing = summaryDao.getSummarySync(recordingId)
        if (existing != null) {
            summaryDao.updateSummary(existing.copy(flashcardsJson = cleanJson))
        } else {
            summaryDao.insertSummary(AISummary(recordingId = recordingId, flashcardsJson = cleanJson))
        }
        cleanJson
    }

    suspend fun generateQuiz(
        apiKey: String,
        recordingId: Long,
        transcriptText: String
    ): Result<String> = runCatching {
        val prompt = """
Create 5-8 multiple-choice quiz questions from this content. 

Return ONLY valid JSON:
{
  "questions": [
    {
      "question": "What...?",
      "options": ["A) Option 1", "B) Option 2", "C) Option 3", "D) Option 4"],
      "correct_index": 0,
      "explanation": "Because..."
    },
    ...
  ]
}

Content:
$transcriptText
        """.trimIndent()

        val response = openAIService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = ChatCompletionRequest(
                model = GPT_MODEL,
                messages = listOf(ChatApiMessage("user", prompt)),
                maxTokens = 2000
            )
        )

        val content = response.choices.firstOrNull()?.message?.content ?: "{}"
        val cleanJson = extractJson(content)

        val existing = summaryDao.getSummarySync(recordingId)
        if (existing != null) {
            summaryDao.updateSummary(existing.copy(quizJson = cleanJson))
        } else {
            summaryDao.insertSummary(AISummary(recordingId = recordingId, quizJson = cleanJson))
        }
        cleanJson
    }

    suspend fun chatWithRecording(
        apiKey: String,
        transcriptText: String,
        conversationHistory: List<ChatApiMessage>,
        userMessage: String
    ): Result<String> = runCatching {
        val systemMessage = ChatApiMessage(
            role = "system",
            content = """You are an intelligent meeting assistant with full access to the following transcript.
Answer questions accurately based on this content. Be concise and helpful.
If information is not in the transcript, say so clearly.

TRANSCRIPT:
$transcriptText"""
        )

        val messages = buildList {
            add(systemMessage)
            addAll(conversationHistory.takeLast(10))
            add(ChatApiMessage(role = "user", content = userMessage))
        }

        val response = openAIService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = ChatCompletionRequest(
                model = GPT_MODEL,
                messages = messages,
                maxTokens = 1000
            )
        )

        response.choices.firstOrNull()?.message?.content
            ?: "I couldn't generate a response. Please try again."
    }

    suspend fun saveSummaryDirect(summary: AISummary) {
        val existing = summaryDao.getSummarySync(summary.recordingId)
        if (existing != null) {
            summaryDao.updateSummary(existing.copy(overview = summary.overview.ifBlank { existing.overview }))
        } else {
            summaryDao.insertSummary(summary)
        }
    }

    suspend fun getSummarySync(recordingId: Long): AISummary? =
        summaryDao.getSummarySync(recordingId)

    suspend fun updateSummaryFlashcards(existing: AISummary, flashcardsJson: String) {
        summaryDao.updateSummary(existing.copy(flashcardsJson = flashcardsJson))
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{').takeIf { it >= 0 }
            ?: text.indexOf('[').takeIf { it >= 0 }
            ?: return text
        val end = if (text[start] == '{') text.lastIndexOf('}') else text.lastIndexOf(']')
        return if (end > start) text.substring(start, end + 1) else text
    }
}
