package com.auranote.app.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class RecorderState { IDLE, RECORDING, PAUSED, STOPPED }

@Singleton
class AudioRecorderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AudioRecorderManager"
        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = 128000
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    private val _state = MutableStateFlow(RecorderState.IDLE)
    val state: StateFlow<RecorderState> = _state

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs

    private var startTimeMs = 0L
    private var accumulatedMs = 0L

    fun startRecording(): File? {
        return try {
            val outputDir = File(context.filesDir, "recordings").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outputFile = File(outputDir, "REC_$timestamp.m4a")

            mediaRecorder = createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(SAMPLE_RATE)
                setAudioEncodingBitRate(BIT_RATE)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            currentOutputFile = outputFile
            startTimeMs = System.currentTimeMillis()
            accumulatedMs = 0L
            _state.value = RecorderState.RECORDING
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            releaseRecorder()
            null
        }
    }

    fun pauseRecording() {
        if (_state.value != RecorderState.RECORDING) return
        try {
            mediaRecorder?.pause()
            accumulatedMs += System.currentTimeMillis() - startTimeMs
            _state.value = RecorderState.PAUSED
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause recording", e)
        }
    }

    fun resumeRecording() {
        if (_state.value != RecorderState.PAUSED) return
        try {
            mediaRecorder?.resume()
            startTimeMs = System.currentTimeMillis()
            _state.value = RecorderState.RECORDING
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume recording", e)
        }
    }

    fun stopRecording(): File? {
        if (_state.value == RecorderState.IDLE) return null
        return try {
            if (_state.value == RecorderState.RECORDING) {
                accumulatedMs += System.currentTimeMillis() - startTimeMs
            }
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _state.value = RecorderState.STOPPED
            _amplitude.value = 0
            currentOutputFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            releaseRecorder()
            null
        }
    }

    fun cancelRecording() {
        releaseRecorder()
        currentOutputFile?.delete()
        currentOutputFile = null
        _state.value = RecorderState.IDLE
        _amplitude.value = 0
        _durationMs.value = 0L
        accumulatedMs = 0L
    }

    fun pollAmplitude() {
        if (_state.value == RecorderState.RECORDING) {
            _amplitude.value = mediaRecorder?.maxAmplitude ?: 0
            val elapsed = System.currentTimeMillis() - startTimeMs
            _durationMs.value = accumulatedMs + elapsed
        } else if (_state.value == RecorderState.PAUSED) {
            _durationMs.value = accumulatedMs
        }
    }

    fun getCurrentDurationMs(): Long {
        return if (_state.value == RecorderState.RECORDING) {
            accumulatedMs + (System.currentTimeMillis() - startTimeMs)
        } else {
            accumulatedMs
        }
    }

    private fun createMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.apply {
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recorder", e)
        } finally {
            mediaRecorder = null
            _state.value = RecorderState.IDLE
        }
    }

    fun reset() {
        releaseRecorder()
        _amplitude.value = 0
        _durationMs.value = 0L
        accumulatedMs = 0L
    }
}
