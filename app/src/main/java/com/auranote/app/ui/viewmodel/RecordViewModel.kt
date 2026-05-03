package com.auranote.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.RecordingType
import com.auranote.app.data.model.TranscriptSegment
import com.auranote.app.data.model.TranscriptionStatus
import com.auranote.app.data.preferences.AppPreferences
import com.auranote.app.data.repository.RecordingRepository
import com.auranote.app.service.AudioRecorderManager
import com.auranote.app.service.LiveTranscriptionManager
import com.auranote.app.service.RecorderState
import com.auranote.app.service.RecordingService
import com.auranote.app.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class RecordUiState(
    val recorderState: RecorderState = RecorderState.IDLE,
    val durationMs: Long = 0L,
    val amplitudes: List<Float> = emptyList(),
    val recordingType: RecordingType = RecordingType.MEETING,
    val title: String = "",
    val error: String? = null,
    val savedRecordingId: Long? = null,
    val liveTranscriptFinal: String = "",
    val liveTranscriptPartial: String = "",
    val liveTranscriptionAvailable: Boolean = false,
    val liveTranscriptionEnabled: Boolean = true
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val recorderManager: AudioRecorderManager,
    private val liveTranscription: LiveTranscriptionManager,
    private val repository: RecordingRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RecordUiState(liveTranscriptionAvailable = liveTranscription.isAvailable())
    )
    val uiState: StateFlow<RecordUiState> = _uiState

    private var currentFile: File? = null
    private var pollJob: Job? = null
    private val amplitudeHistory = mutableListOf<Float>()
    private val maxBars = 60

    init {
        viewModelScope.launch {
            recorderManager.state.collect { state ->
                _uiState.update { it.copy(recorderState = state) }
            }
        }
        viewModelScope.launch {
            liveTranscription.finalText.collect { t ->
                _uiState.update { it.copy(liveTranscriptFinal = t) }
            }
        }
        viewModelScope.launch {
            liveTranscription.partialText.collect { t ->
                _uiState.update { it.copy(liveTranscriptPartial = t) }
            }
        }
        viewModelScope.launch {
            liveTranscription.error.collect { msg ->
                if (msg != null) _uiState.update { it.copy(error = msg) }
            }
        }
    }

    fun startRecording() {
        // Foreground service drives the recorder; it gives us notification + wake-lock + audio focus
        RecordingService.start(appContext)
        amplitudeHistory.clear()
        startPolling()
        if (_uiState.value.liveTranscriptionEnabled && liveTranscription.isAvailable()) {
            viewModelScope.launch {
                val lang = preferences.transcriptionLanguage.first()
                liveTranscription.reset()
                liveTranscription.start(language = lang, preferOffline = false)
            }
        }
    }

    fun pauseRecording() {
        RecordingService.pause(appContext)
        liveTranscription.stop()
        pollJob?.cancel()
    }

    fun resumeRecording() {
        RecordingService.resume(appContext)
        if (_uiState.value.liveTranscriptionEnabled && liveTranscription.isAvailable()) {
            viewModelScope.launch {
                val lang = preferences.transcriptionLanguage.first()
                liveTranscription.start(language = lang, preferOffline = false)
            }
        }
        startPolling()
    }

    fun stopRecording() {
        pollJob?.cancel()
        liveTranscription.stop()
        RecordingService.stop(appContext)
        // recorderManager is owned by the service but state is shared via @Singleton
        val file = recorderManager.stopRecording()
        if (file != null && file.exists()) {
            currentFile = file
            saveRecording(file)
        } else {
            _uiState.update { it.copy(error = "Recording file not found") }
        }
    }

    fun cancelRecording() {
        pollJob?.cancel()
        liveTranscription.stop()
        liveTranscription.reset()
        RecordingService.stop(appContext)
        recorderManager.cancelRecording()
        _uiState.value = RecordUiState(
            liveTranscriptionAvailable = liveTranscription.isAvailable()
        )
    }

    fun setRecordingType(type: RecordingType) {
        _uiState.update { it.copy(recordingType = type) }
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun setLiveTranscriptionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(liveTranscriptionEnabled = enabled) }
    }

    fun importAudioFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val destDir = FileUtils.importedAudioDir(context)
                val fileName = "IMPORT_${System.currentTimeMillis()}.m4a"
                val destFile = File(destDir, fileName)
                val copied = FileUtils.copyUriToFile(context, uri, destFile)
                if (copied) {
                    currentFile = destFile
                    saveRecording(destFile)
                } else {
                    _uiState.update { it.copy(error = "Failed to import audio file") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Import error: ${e.message}") }
            }
        }
    }

    private fun saveRecording(file: File) {
        viewModelScope.launch {
            val title = _uiState.value.title.ifBlank {
                val type = _uiState.value.recordingType.label
                "$type – ${java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault()).format(java.util.Date())}"
            }
            val durationMs = recorderManager.getCurrentDurationMs().takeIf { it > 0 }
                ?: file.length().let { if (it > 0) (it / 16000) * 1000 else 0L }

            val liveText = _uiState.value.liveTranscriptFinal.trim()

            val recording = Recording(
                title = title,
                filePath = file.absolutePath,
                durationMs = durationMs,
                fileSizeBytes = file.length(),
                type = _uiState.value.recordingType,
                transcriptionStatus = if (liveText.isNotBlank())
                    TranscriptionStatus.COMPLETED else TranscriptionStatus.PENDING
            )
            val id = repository.insertRecording(recording)

            if (liveText.isNotBlank()) {
                val seg = TranscriptSegment(
                    recordingId = id,
                    speakerLabel = "Speaker 1",
                    text = liveText,
                    startTimeSeconds = 0f,
                    endTimeSeconds = (durationMs / 1000f),
                    confidence = 0.85f,
                    segmentIndex = 0
                )
                repository.insertTranscriptSegments(listOf(seg))
            }

            _uiState.update { it.copy(savedRecordingId = id) }
            recorderManager.reset()
            liveTranscription.reset()
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                recorderManager.pollAmplitude()
                val rawAmp = recorderManager.amplitude.value
                val normalized = (rawAmp / 32767f).coerceIn(0.05f, 1f)
                amplitudeHistory.add(normalized)
                if (amplitudeHistory.size > maxBars) amplitudeHistory.removeAt(0)
                _uiState.update {
                    it.copy(
                        durationMs = recorderManager.durationMs.value,
                        amplitudes = amplitudeHistory.toList()
                    )
                }
                delay(80L)
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
        liveTranscription.stop()
    }
}
