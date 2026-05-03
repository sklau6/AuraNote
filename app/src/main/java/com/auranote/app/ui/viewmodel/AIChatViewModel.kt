package com.auranote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auranote.app.data.api.ChatApiMessage
import com.auranote.app.data.model.ChatMessage
import com.auranote.app.data.model.MessageRole
import com.auranote.app.data.preferences.AppPreferences
import com.auranote.app.data.repository.AIRepository
import com.auranote.app.data.repository.GeminiRepository
import com.auranote.app.data.repository.OnDeviceAIRepository
import com.auranote.app.data.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val apiKeyMissing: Boolean = false
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val geminiRepository: GeminiRepository,
    private val onDeviceAI: OnDeviceAIRepository,
    private val recordingRepository: RecordingRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState

    private var transcriptText: String = ""
    private val conversationHistory = mutableListOf<ChatApiMessage>()

    fun initWithRecording(recordingId: Long) {
        viewModelScope.launch {
            transcriptText = recordingRepository.getFullTranscriptText(recordingId) ?: ""
            if (transcriptText.isBlank()) {
                val segments = recordingRepository.getTranscriptSegmentsSync(recordingId)
                transcriptText = segments.joinToString(" ") { it.text }
            }
            if (transcriptText.isNotBlank()) {
                val welcome = ChatMessage(
                    content = "Hi! I've read your transcript. Ask me anything about the recording — topics discussed, specific statements, action items, or request a summary.",
                    role = MessageRole.ASSISTANT
                )
                _uiState.update { it.copy(messages = listOf(welcome)) }
            } else {
                val noTranscript = ChatMessage(
                    content = "I don't have a transcript yet. Please transcribe the recording first, then I can answer questions about it.",
                    role = MessageRole.ASSISTANT
                )
                _uiState.update { it.copy(messages = listOf(noTranscript)) }
            }
        }
    }

    fun setInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            val apiKey = preferences.apiKey.first()

            val userMessage = ChatMessage(content = text, role = MessageRole.USER)
            _uiState.update { state ->
                state.copy(
                    messages = state.messages + userMessage,
                    inputText = "",
                    isLoading = true,
                    error = null,
                    apiKeyMissing = false
                )
            }

            // Try Gemini first (built-in keys, no user config needed)
            val geminiResult = geminiRepository.chat(
                transcriptText = transcriptText,
                conversationHistory = conversationHistory,
                userMessage = text
            )

            if (geminiResult.isSuccess) {
                val response = geminiResult.getOrThrow()
                val assistantMessage = ChatMessage(content = response, role = MessageRole.ASSISTANT)
                conversationHistory.add(ChatApiMessage("user", text))
                conversationHistory.add(ChatApiMessage("assistant", response))
                _uiState.update { state ->
                    state.copy(messages = state.messages + assistantMessage, isLoading = false)
                }
            } else if (apiKey.isNotBlank()) {
                // Fall back to OpenAI if Gemini fails
                aiRepository.chatWithRecording(
                    apiKey = apiKey,
                    transcriptText = transcriptText,
                    conversationHistory = conversationHistory,
                    userMessage = text
                ).fold(
                    onSuccess = { response ->
                        val assistantMessage = ChatMessage(content = response, role = MessageRole.ASSISTANT)
                        conversationHistory.add(ChatApiMessage("user", text))
                        conversationHistory.add(ChatApiMessage("assistant", response))
                        _uiState.update { state ->
                            state.copy(messages = state.messages + assistantMessage, isLoading = false)
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { state ->
                            state.copy(isLoading = false, error = "Failed to get response: ${e.message}")
                        }
                    }
                )
            } else {
                // Last resort: on-device AI
                val prompt = buildString {
                    if (transcriptText.isNotBlank()) {
                        append("You are a helpful assistant. The user has this transcript:\n\n")
                        append(transcriptText.take(3000))
                        append("\n\nAnswer the following question based on the transcript:\n")
                    }
                    append(text)
                }
                onDeviceAI.generateWithPrompt(prompt).fold(
                    onSuccess = { response ->
                        val assistantMessage = ChatMessage(content = response, role = MessageRole.ASSISTANT)
                        _uiState.update { state ->
                            state.copy(messages = state.messages + assistantMessage, isLoading = false)
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = "All AI backends failed: ${e.message}"
                            )
                        }
                    }
                )
            }
        }
    }

    fun sendSuggestedQuestion(question: String) {
        _uiState.update { it.copy(inputText = question) }
        sendMessage()
    }

    fun clearError() = _uiState.update { it.copy(error = null, apiKeyMissing = false) }

    fun clearChat() {
        conversationHistory.clear()
        _uiState.update { it.copy(messages = emptyList()) }
        initWithRecording(0L)
    }
}
