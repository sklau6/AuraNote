package com.auranote.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auranote.app.data.api.ChatApiMessage
import com.auranote.app.data.model.AISummary
import com.auranote.app.data.model.Flashcard
import com.auranote.app.data.model.QuizQuestion
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.TranscriptSegment
import com.auranote.app.data.model.TranscriptionStatus
import com.auranote.app.data.preferences.AppPreferences
import com.auranote.app.data.repository.AIRepository
import com.auranote.app.data.repository.OnDeviceAIRepository
import com.auranote.app.data.repository.RecordingRepository
import com.auranote.app.util.FileUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DetailUiState(
    val recording: Recording? = null,
    val segments: List<TranscriptSegment> = emptyList(),
    val summary: AISummary? = null,
    val flashcards: List<Flashcard> = emptyList(),
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val isTranscribing: Boolean = false,
    val isGeneratingSummary: Boolean = false,
    val isGeneratingStudy: Boolean = false,
    val isAssigningSpeakers: Boolean = false,
    val playbackPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val error: String? = null,
    val transcriptSearchQuery: String = "",
    val apiKeyMissing: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val recordingRepository: RecordingRepository,
    private val aiRepository: AIRepository,
    private val onDeviceAI: OnDeviceAIRepository,
    private val preferences: AppPreferences,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    fun loadRecording(recordingId: Long) {
        viewModelScope.launch {
            val recording = recordingRepository.getRecordingById(recordingId) ?: return@launch
            _uiState.update { it.copy(recording = recording) }

            recordingRepository.getTranscriptSegments(recordingId).collect { segments ->
                _uiState.update { it.copy(segments = segments) }
            }
        }
        viewModelScope.launch {
            aiRepository.getSummary(recordingId).collect { summary ->
                _uiState.update { s ->
                    s.copy(
                        summary = summary,
                        flashcards = parseFlashcards(summary?.flashcardsJson),
                        quizQuestions = parseQuiz(summary?.quizJson)
                    )
                }
            }
        }
    }

    fun transcribeRecording() {
        val recording = _uiState.value.recording ?: return
        viewModelScope.launch {
            val apiKey = preferences.apiKey.first()
            val file = File(recording.filePath)
            if (!file.exists()) {
                _uiState.update { it.copy(error = "Audio file not found") }
                return@launch
            }

            _uiState.update { it.copy(isTranscribing = true, error = null, apiKeyMissing = false) }
            recordingRepository.updateTranscriptionStatus(recording.id, TranscriptionStatus.IN_PROGRESS.name)

            if (apiKey.isBlank()) {
                // Fallback: on-device Gemini Nano speech recognition
                onDeviceAI.transcribeAudioFile(file).fold(
                    onSuccess = { text ->
                        val segments = buildTranscriptSegments(recording.id, text, null)
                        recordingRepository.deleteTranscriptSegments(recording.id)
                        recordingRepository.insertTranscriptSegments(segments)
                        recordingRepository.updateTranscriptionStatus(recording.id, TranscriptionStatus.COMPLETED.name)
                        _uiState.update { it.copy(isTranscribing = false) }
                        generateSummaryOnDevice(recording.id)
                    },
                    onFailure = { e ->
                        recordingRepository.updateTranscriptionStatus(recording.id, TranscriptionStatus.ERROR.name)
                        _uiState.update {
                            it.copy(
                                isTranscribing = false,
                                error = "On-device transcription failed: ${e.message}. Add an OpenAI API key in Settings for cloud transcription."
                            )
                        }
                    }
                )
            } else {
                val language = preferences.transcriptionLanguage.first()
                aiRepository.transcribeAudio(apiKey, file, language).fold(
                    onSuccess = { response ->
                        val segments = buildTranscriptSegments(recording.id, response.text, response.segments)
                        recordingRepository.deleteTranscriptSegments(recording.id)
                        recordingRepository.insertTranscriptSegments(segments)
                        recordingRepository.updateTranscriptionStatus(recording.id, TranscriptionStatus.COMPLETED.name)

                        val speakerDetection = preferences.speakerDetection.first()
                        if (speakerDetection && segments.size > 1) {
                            assignSpeakers(recording.id, segments, apiKey)
                        } else {
                            _uiState.update { it.copy(isTranscribing = false) }
                            generateSummaryInternal(recording.id, apiKey)
                        }
                    },
                    onFailure = { e ->
                        recordingRepository.updateTranscriptionStatus(recording.id, TranscriptionStatus.ERROR.name)
                        _uiState.update {
                            it.copy(
                                isTranscribing = false,
                                error = "Transcription failed: ${e.message}"
                            )
                        }
                    }
                )
            }
        }
    }

    private suspend fun assignSpeakers(recordingId: Long, segments: List<TranscriptSegment>, apiKey: String) {
        _uiState.update { it.copy(isAssigningSpeakers = true) }
        val fullText = segments.joinToString(" ") { it.text }
        aiRepository.assignSpeakers(apiKey, segments, fullText).fold(
            onSuccess = { labeled ->
                recordingRepository.insertTranscriptSegments(labeled)
                val speakerCount = labeled.map { it.speakerLabel }.distinct().size
                recordingRepository.getRecordingById(recordingId)?.let { rec ->
                    recordingRepository.updateRecording(rec.copy(speakerCount = speakerCount))
                }
            },
            onFailure = { }
        )
        _uiState.update { it.copy(isAssigningSpeakers = false, isTranscribing = false) }
        generateSummaryInternal(recordingId, apiKey)
    }

    fun generateSummary() {
        val recording = _uiState.value.recording ?: return
        viewModelScope.launch {
            val apiKey = preferences.apiKey.first()
            if (apiKey.isBlank()) {
                generateSummaryOnDevice(recording.id)
            } else {
                generateSummaryInternal(recording.id, apiKey)
            }
        }
    }

    private suspend fun generateSummaryOnDevice(recordingId: Long) {
        val transcriptText = recordingRepository.getFullTranscriptText(recordingId) ?: return
        if (transcriptText.isBlank()) return
        _uiState.update { it.copy(isGeneratingSummary = true) }
        onDeviceAI.summarizeText(transcriptText).fold(
            onSuccess = { summary ->
                val aiSummary = com.auranote.app.data.model.AISummary(
                    recordingId = recordingId,
                    overview = summary
                )
                aiRepository.saveSummaryDirect(aiSummary)
            },
            onFailure = { e ->
                _uiState.update { it.copy(error = "On-device summary failed: ${e.message}") }
            }
        )
        _uiState.update { it.copy(isGeneratingSummary = false) }
    }

    private suspend fun generateSummaryInternal(recordingId: Long, apiKey: String) {
        val transcriptText = recordingRepository.getFullTranscriptText(recordingId) ?: return
        if (transcriptText.isBlank()) return

        _uiState.update { it.copy(isGeneratingSummary = true) }
        aiRepository.generateSummary(apiKey, recordingId, transcriptText).fold(
            onSuccess = { },
            onFailure = { e ->
                _uiState.update { it.copy(error = "Summary generation failed: ${e.message}") }
            }
        )
        _uiState.update { it.copy(isGeneratingSummary = false) }
    }

    fun generateStudyContent() {
        val recording = _uiState.value.recording ?: return
        viewModelScope.launch {
            val apiKey = preferences.apiKey.first()
            if (apiKey.isBlank()) {
                generateStudyContentOnDevice(recording.id)
                return@launch
            }
            val transcriptText = recordingRepository.getFullTranscriptText(recording.id) ?: return@launch
            if (transcriptText.isBlank()) return@launch

            _uiState.update { it.copy(isGeneratingStudy = true) }
            launch {
                aiRepository.generateFlashcards(apiKey, recording.id, transcriptText).fold(
                    onSuccess = { },
                    onFailure = { e -> _uiState.update { it.copy(error = "Flashcards failed: ${e.message}") } }
                )
            }
            launch {
                aiRepository.generateQuiz(apiKey, recording.id, transcriptText).fold(
                    onSuccess = { },
                    onFailure = { e -> _uiState.update { it.copy(error = "Quiz generation failed: ${e.message}") } }
                )
            }
            _uiState.update { it.copy(isGeneratingStudy = false) }
        }
    }

    fun shareTranscript(context: Context) {
        val segments = _uiState.value.segments
        val recording = _uiState.value.recording ?: return
        val text = buildString {
            appendLine(recording.title)
            appendLine("Recorded: ${java.text.SimpleDateFormat("MMM d, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date(recording.createdAt))}")
            appendLine()
            appendLine("=== TRANSCRIPT ===")
            appendLine()
            segments.forEach { seg ->
                appendLine("[${seg.speakerLabel}] (${formatTime(seg.startTimeSeconds)})")
                appendLine(seg.text)
                appendLine()
            }
            _uiState.value.summary?.let { s ->
                if (s.overview.isNotBlank()) {
                    appendLine("=== SUMMARY ===")
                    appendLine(s.overview)
                }
            }
        }
        FileUtils.shareText(context, text, "Share: ${recording.title}")
    }

    fun shareAudio(context: Context) {
        val recording = _uiState.value.recording ?: return
        FileUtils.shareAudioFile(context, recording.filePath, recording.title)
    }

    fun updateTitle(newTitle: String) {
        val recording = _uiState.value.recording ?: return
        viewModelScope.launch {
            recordingRepository.updateTitle(recording.id, newTitle)
            _uiState.update { it.copy(recording = recording.copy(title = newTitle)) }
        }
    }

    fun setTranscriptSearchQuery(query: String) {
        _uiState.update { it.copy(transcriptSearchQuery = query) }
    }

    private suspend fun generateStudyContentOnDevice(recordingId: Long) {
        val transcriptText = recordingRepository.getFullTranscriptText(recordingId) ?: return
        if (transcriptText.isBlank()) return
        _uiState.update { it.copy(isGeneratingStudy = true) }
        val flashcardPrompt = """
Create 6-8 study flashcards from this content. Return each as "Q: ...\nA: ..." pairs separated by blank lines.

Content:
${transcriptText.take(3000)}
        """.trimIndent()
        onDeviceAI.generateWithPrompt(flashcardPrompt).fold(
            onSuccess = { raw ->
                val pairs = raw.split("\n\n").mapNotNull { block ->
                    val q = block.lines().firstOrNull { it.startsWith("Q:") }?.removePrefix("Q:")?.trim()
                    val a = block.lines().firstOrNull { it.startsWith("A:") }?.removePrefix("A:")?.trim()
                    if (q != null && a != null) mapOf("question" to q, "answer" to a) else null
                }
                val json = "{\"flashcards\":${gson.toJson(pairs)}}"
                val existing = aiRepository.getSummarySync(recordingId)
                if (existing != null) {
                    aiRepository.updateSummaryFlashcards(existing, json)
                } else {
                    aiRepository.saveSummaryDirect(
                        com.auranote.app.data.model.AISummary(recordingId = recordingId, flashcardsJson = json)
                    )
                }
            },
            onFailure = { e -> _uiState.update { it.copy(error = "On-device flashcards failed: ${e.message}") } }
        )
        _uiState.update { it.copy(isGeneratingStudy = false) }
    }

    fun clearError() = _uiState.update { it.copy(error = null, apiKeyMissing = false) }

    private fun buildTranscriptSegments(
        recordingId: Long,
        fullText: String,
        whisperSegments: List<com.auranote.app.data.api.WhisperSegment>?
    ): List<TranscriptSegment> {
        return if (!whisperSegments.isNullOrEmpty()) {
            whisperSegments.mapIndexed { i, seg ->
                TranscriptSegment(
                    recordingId = recordingId,
                    speakerLabel = "Speaker 1",
                    text = seg.text.trim(),
                    startTimeSeconds = seg.start,
                    endTimeSeconds = seg.end,
                    segmentIndex = i
                )
            }
        } else {
            val words = fullText.split(". ", "? ", "! ")
            words.mapIndexed { i, sentence ->
                TranscriptSegment(
                    recordingId = recordingId,
                    speakerLabel = "Speaker 1",
                    text = sentence.trim(),
                    startTimeSeconds = i * 5f,
                    endTimeSeconds = (i + 1) * 5f,
                    segmentIndex = i
                )
            }.filter { it.text.isNotBlank() }
        }
    }

    private fun parseFlashcards(json: String?): List<Flashcard> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<Map<String, List<Flashcard>>>() {}.type
            val map: Map<String, List<Flashcard>> = gson.fromJson(json, type)
            map["flashcards"] ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseQuiz(json: String?): List<QuizQuestion> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<Map<String, List<QuizQuestion>>>() {}.type
            val map: Map<String, List<QuizQuestion>> = gson.fromJson(json, type)
            map["questions"] ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun formatTime(seconds: Float): String {
        val m = (seconds / 60).toInt()
        val s = (seconds % 60).toInt()
        return String.format(java.util.Locale.US, "%d:%02d", m, s)
    }
}
