package com.auranote.app.data.repository

import android.util.Log
import com.auranote.app.data.api.GeminiContent
import com.auranote.app.data.api.GeminiGenerationConfig
import com.auranote.app.data.api.GeminiPart
import com.auranote.app.data.api.GeminiRequest
import com.auranote.app.data.api.GeminiService
import com.auranote.app.data.api.SummaryResponse
import com.auranote.app.data.db.SummaryDao
import com.auranote.app.data.model.AISummary
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val geminiService: GeminiService,
    private val summaryDao: SummaryDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "GeminiRepository"

        /**
         * Gemini model priority list.
         * When a model fails (quota / preview unavailability), the next is tried.
         */
        val MODELS = listOf(
            "gemini-2.5-flash-preview-05-20",   // Gemini 2.5 Flash — fast + reasoning
            "gemini-2.5-flash-lite-preview-06-17", // Gemini 2.5 Flash-Lite — high volume
            "gemini-2.5-pro-preview-06-05",     // Gemini 2.5 Pro — complex reasoning
            "gemini-2.0-flash",                  // Stable fallback
            "gemini-1.5-flash"                   // Last-resort stable fallback
        )

        /** All provided API keys — rotated randomly on each failure. */
        val API_KEYS = listOf(
            "AIzaSyBFfmj1onm5eZCtMxs5Z-l9AbGLVG7oIKw",
            "AIzaSyCTG6roDq5dI54e5gnhZv7LyLunfM_PU8c",
            "AIzaSyCq-AZHt6ti6cwuDZlnVu_9nzcDbP2saWI",
            "AIzaSyDxWCm00C0hwzcIHkiR_vxaNsyHABRaI1U",
            "AIzaSyChtFkka_G6RoWkADu5D39nirD0X0NO_Ns",
            "AIzaSyCUtBMMpjtCutfG1iiPjMFo-fc3PotIdkw",
            "AIzaSyCo7OF10lmXBA9uOIZPkiy5fvsFXjfNWZ0",
            "AIzaSyCfL1YTXzRNy1B1mhOFyG55USC3gxZHdVg",
            "AIzaSyBUv_2O8ebV2LEwmReq8-ZRSNxc8t29nmM",
            "AIzaSyCeEfAVZE1AcqTS2JzI6239BEFHyrdFPiw",
            "AIzaSyB97zCfewDVVqBgWhJ_8XOh2tSLFsQ5EkY",
            "AIzaSyBwS1bB75Y1NcHjzY2CytHEqWlYPS4YixI",
            "AIzaSyDSyQLi8ZswWOpldpkRrqz5quWH0aXsKrk",
            "AIzaSyA6gTIy0H0f2CF_U1jDZAVRrIE8xkfOoSo",
            "AIzaSyAmO2_Lstydy7ZSYKkyJdG5ao1cfjKgh-A"
        )
    }

    /**
     * Send a prompt to Gemini, rotating keys and models until one succeeds.
     * Returns the text content of the first candidate.
     */
    suspend fun generate(
        systemPrompt: String? = null,
        userPrompt: String,
        maxOutputTokens: Int = 8192
    ): Result<String> {
        val keyList = API_KEYS.shuffled()
        val modelList = MODELS

        for (model in modelList) {
            for (key in keyList) {
                try {
                    val contents = mutableListOf<GeminiContent>()
                    if (systemPrompt != null) {
                        // system instruction is passed separately; user content only in contents
                        contents.add(GeminiContent(parts = listOf(GeminiPart(userPrompt)), role = "user"))
                    } else {
                        contents.add(GeminiContent(parts = listOf(GeminiPart(userPrompt)), role = "user"))
                    }

                    val request = GeminiRequest(
                        contents = contents,
                        systemInstruction = systemPrompt?.let {
                            GeminiContent(parts = listOf(GeminiPart(it)), role = "user")
                        },
                        generationConfig = GeminiGenerationConfig(
                            maxOutputTokens = maxOutputTokens,
                            temperature = 0.7f
                        )
                    )

                    val response = geminiService.generateContent(
                        model = model,
                        apiKey = key,
                        request = request
                    )

                    if (response.error != null && response.error.code != 0) {
                        Log.w(TAG, "Gemini error model=$model key=...${key.takeLast(4)}: ${response.error.message}")
                        continue
                    }

                    val text = response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text
                        .orEmpty()
                        .trim()

                    if (text.isBlank()) {
                        Log.w(TAG, "Empty response from model=$model")
                        continue
                    }

                    Log.d(TAG, "Success model=$model key=...${key.takeLast(4)}, ${text.length} chars")
                    return Result.success(text)
                } catch (e: Exception) {
                    Log.w(TAG, "Exception model=$model key=...${key.takeLast(4)}: ${e.message}")
                }
            }
        }

        return Result.failure(IllegalStateException("All Gemini models and keys exhausted"))
    }

    // ── Meeting minutes ──────────────────────────────────────────────────────

    suspend fun generateMeetingMinutes(
        recordingId: Long,
        transcriptText: String,
        languageHint: String = "auto"
    ): Result<AISummary> = runCatching {
        val systemPrompt = """
You are a professional meeting-minutes writer. Convert the raw transcript into clean executive-ready minutes.

ABSOLUTE RULES:
1. Output a SINGLE JSON object. No prose, no markdown, no code fences.
2. Use this exact schema (snake_case field names):
{
  "overview": "EXECUTIVE SUMMARY: 2-4 crisp sentences capturing purpose, outcome, and tone.",
  "key_points": ["Notable discussion point.", "..."],
  "decisions": ["KEY DECISION: complete declarative sentence.", "..."],
  "action_items": ["TASK — OWNER (or 'Unassigned') — DUE (ISO date or 'TBD')", "..."],
  "next_steps": ["Concrete follow-up planned.", "..."]
}
3. Be concise and professional. Strip filler words.
4. Never invent participants, dates, or commitments not in the transcript.
5. Preserve the transcript's language: ${if (languageHint == "auto") "match transcript" else languageHint}.
        """.trimIndent()

        val maxAttempts = 3
        var lastError: Throwable? = null
        var summaryResponse: SummaryResponse? = null

        for (attempt in 1..maxAttempts) {
            generate(systemPrompt = systemPrompt, userPrompt = "TRANSCRIPT:\n\n${transcriptText.trim()}")
                .onSuccess { text ->
                    val json = extractJson(text)
                    try {
                        summaryResponse = gson.fromJson(json, SummaryResponse::class.java)
                    } catch (e: JsonSyntaxException) {
                        Log.w(TAG, "JSON parse failed attempt $attempt: ${e.message}")
                        lastError = e
                    }
                }
                .onFailure { e ->
                    lastError = e
                    Log.w(TAG, "Meeting minutes attempt $attempt failed: ${e.message}")
                }
            if (summaryResponse != null) break
            if (attempt < maxAttempts) kotlinx.coroutines.delay(1500L * attempt)
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

    // ── Flashcards ───────────────────────────────────────────────────────────

    suspend fun generateFlashcards(recordingId: Long, transcriptText: String): Result<String> =
        runCatching {
            val prompt = """
Create 8-12 study flashcards from this transcript/lecture content.
Return ONLY valid JSON (no markdown, no code fences):
{
  "flashcards": [
    {"question": "What is...?", "answer": "The answer is..."},
    ...
  ]
}

Content:
$transcriptText
            """.trimIndent()

            val text = generate(userPrompt = prompt).getOrThrow()
            val json = extractJson(text)

            val existing = summaryDao.getSummarySync(recordingId)
            if (existing != null) summaryDao.updateSummary(existing.copy(flashcardsJson = json))
            else summaryDao.insertSummary(AISummary(recordingId = recordingId, flashcardsJson = json))
            json
        }

    // ── Quiz ─────────────────────────────────────────────────────────────────

    suspend fun generateQuiz(recordingId: Long, transcriptText: String): Result<String> =
        runCatching {
            val prompt = """
Create 5-8 multiple-choice quiz questions from this content.
Return ONLY valid JSON (no markdown, no code fences):
{
  "questions": [
    {
      "question": "What...?",
      "options": ["A) Option 1", "B) Option 2", "C) Option 3", "D) Option 4"],
      "correct_index": 0,
      "explanation": "Because..."
    }
  ]
}

Content:
$transcriptText
            """.trimIndent()

            val text = generate(userPrompt = prompt).getOrThrow()
            val json = extractJson(text)

            val existing = summaryDao.getSummarySync(recordingId)
            if (existing != null) summaryDao.updateSummary(existing.copy(quizJson = json))
            else summaryDao.insertSummary(AISummary(recordingId = recordingId, quizJson = json))
            json
        }

    // ── Chat ─────────────────────────────────────────────────────────────────

    suspend fun chat(
        transcriptText: String,
        conversationHistory: List<com.auranote.app.data.api.ChatApiMessage>,
        userMessage: String
    ): Result<String> {
        val systemPrompt = """You are an intelligent meeting assistant with full access to the following transcript.
Answer questions accurately based on this content. Be concise and helpful.
If information is not in the transcript, say so clearly.

TRANSCRIPT:
$transcriptText"""

        val historyText = conversationHistory.takeLast(10).joinToString("\n") {
            "${it.role.uppercase()}: ${it.content}"
        }
        val userPrompt = if (historyText.isNotBlank())
            "Conversation so far:\n$historyText\n\nUSER: $userMessage"
        else
            userMessage

        return generate(systemPrompt = systemPrompt, userPrompt = userPrompt, maxOutputTokens = 2048)
    }

    // ── Speaker diarization ──────────────────────────────────────────────────

    suspend fun assignSpeakers(
        segments: List<com.auranote.app.data.model.TranscriptSegment>,
        fullText: String
    ): Result<List<com.auranote.app.data.model.TranscriptSegment>> = runCatching {
        val segmentLines = segments.mapIndexed { i, seg ->
            "[$i] (${seg.startTimeSeconds}s-${seg.endTimeSeconds}s): ${seg.text}"
        }.joinToString("\n")

        val prompt = """
You are a speaker diarization assistant. Analyze this transcript and assign speaker labels.
Rules:
- Use labels like "Speaker 1", "Speaker 2", etc.
- Be consistent: same person → same label
- Return ONLY a JSON array: [{"index": 0, "speaker": "Speaker 1"}, ...]

Transcript segments:
$segmentLines
        """.trimIndent()

        val text = generate(userPrompt = prompt, maxOutputTokens = 2048).getOrThrow()
        val json = extractJson(text)

        data class SpeakerAssignment(val index: Int, val speaker: String)

        val assignments = try {
            gson.fromJson(json, Array<SpeakerAssignment>::class.java).associateBy { it.index }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse speaker assignments", e)
            emptyMap()
        }

        segments.mapIndexed { i, seg ->
            seg.copy(speakerLabel = assignments[i]?.speaker ?: "Speaker 1")
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun extractJson(text: String): String {
        val start = text.indexOf('{').takeIf { it >= 0 }
            ?: text.indexOf('[').takeIf { it >= 0 }
            ?: return text
        val end = if (text[start] == '{') text.lastIndexOf('}') else text.lastIndexOf(']')
        return if (end > start) text.substring(start, end + 1) else text
    }
}
