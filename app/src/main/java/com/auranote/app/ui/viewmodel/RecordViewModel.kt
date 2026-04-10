package com.auranote.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.RecordingType
import com.auranote.app.data.model.TranscriptionStatus
import com.auranote.app.data.preferences.AppPreferences
import com.auranote.app.data.repository.RecordingRepository
import com.auranote.app.service.AudioRecorderManager
import com.auranote.app.service.RecorderState
import com.auranote.app.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val savedRecordingId: Long? = null
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recorderManager: AudioRecorderManager,
    private val repository: RecordingRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
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
    }

    fun startRecording() {
        val file = recorderManager.startRecording()
        if (file != null) {
            currentFile = file
            amplitudeHistory.clear()
            startPolling()
        } else {
            _uiState.update { it.copy(error = "Failed to start recording. Check microphone permission.") }
        }
    }

    fun pauseRecording() {
        recorderManager.pauseRecording()
        pollJob?.cancel()
    }

    fun resumeRecording() {
        recorderManager.resumeRecording()
        startPolling()
    }

    fun stopRecording() {
        pollJob?.cancel()
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
        recorderManager.cancelRecording()
        _uiState.value = RecordUiState()
    }

    fun setRecordingType(type: RecordingType) {
        _uiState.update { it.copy(recordingType = type) }
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title) }
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

            val recording = Recording(
                title = title,
                filePath = file.absolutePath,
                durationMs = durationMs,
                fileSizeBytes = file.length(),
                type = _uiState.value.recordingType,
                transcriptionStatus = TranscriptionStatus.PENDING
            )
            val id = repository.insertRecording(recording)
            _uiState.update { it.copy(savedRecordingId = id) }
            recorderManager.reset()
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
    }
}
