package com.auranote.app.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Streams live, on-device speech-to-text by chaining together repeated
 * [SpeechRecognizer] sessions. Each session ends naturally on silence
 * (or onError) and we immediately restart so the transcript appears
 * continuous to the user.
 *
 * Falls back gracefully if the device has no speech recogniser — the
 * regular MediaRecorder + post-recording transcription path still works.
 *
 * Must be created and used on the main thread (Android requirement).
 */
@Singleton
class LiveTranscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LiveTranscription"

        /** Gboard's built-in voice recognition service — supports 100+ languages without separate downloads. */
        private val GBOARD_COMPONENT = ComponentName(
            "com.google.android.inputmethod.latin",
            "com.google.android.inputmethod.latin.voice.VoiceRecognitionService"
        )

        /** Normalize locale variants that Google engines don't recognise as-is. */
        fun normalizeLocale(tag: String): String = when {
            tag.startsWith("zh-HK") || tag.startsWith("zh-MO") -> "zh-TW"
            tag.startsWith("zh-SG") -> "zh-CN"
            else -> tag
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var languageTag: String = "en-US"
    private var fallbackLanguageTag: String? = null
    private var currentLanguageTag: String = "en-US"
    private var usingFallback: Boolean = false
    private var preferOffline: Boolean = false
    private var running: Boolean = false
    private var useGboard: Boolean = false

    /** Concatenated finalised text produced so far. */
    private val _finalText = MutableStateFlow("")
    val finalText: StateFlow<String> = _finalText.asStateFlow()

    /** Most-recent partial (interim) hypothesis being spoken now. */
    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _available = MutableStateFlow(false)
    val available: StateFlow<Boolean> = _available.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start(language: String = "auto", preferOffline: Boolean = false) {
        if (running) return
        if (!isAvailable()) {
            _available.value = false
            _error.value = "Speech recognition unavailable on this device"
            return
        }
        val raw = if (language == "auto") java.util.Locale.getDefault().toLanguageTag() else language
        languageTag = normalizeLocale(raw)
        // Build fallback chain: zh-TW → zh-CN → en-US
        fallbackLanguageTag = when {
            languageTag.startsWith("zh-TW") || languageTag.startsWith("zh-HK") -> "zh-CN"
            languageTag != "en-US" -> "en-US"
            else -> null
        }
        currentLanguageTag = languageTag
        usingFallback = false
        // Gboard handles online multilingual better; don't force offline
        this.preferOffline = preferOffline
        useGboard = isGboardAvailable()
        Log.d(TAG, "Starting live transcription: lang=$languageTag, gboard=$useGboard")
        _available.value = true
        _finalText.value = ""
        _partialText.value = ""
        running = true
        handler.post { startSession() }
    }

    fun stop() {
        running = false
        handler.post {
            try { recognizer?.stopListening() } catch (_: Exception) {}
            try { recognizer?.destroy() } catch (_: Exception) {}
            recognizer = null
            // Promote any in-flight partial text to final on stop.
            val tail = _partialText.value
            if (tail.isNotBlank()) {
                _finalText.value = (_finalText.value + " " + tail).trim()
                _partialText.value = ""
            }
        }
    }

    fun reset() {
        _finalText.value = ""
        _partialText.value = ""
        _error.value = null
    }

    /** Check if Gboard's voice recognition service is installed. */
    private fun isGboardAvailable(): Boolean = runCatching {
        val pm = context.packageManager
        // Check Gboard package exists and its voice service is resolvable
        pm.getPackageInfo("com.google.android.inputmethod.latin", 0)
        val serviceIntent = android.content.Intent().setComponent(GBOARD_COMPONENT)
        pm.resolveService(serviceIntent, 0) != null
    }.getOrDefault(false)

    private fun createRecognizer(): SpeechRecognizer {
        // Try Gboard first, then on-device (API 31+), then system default
        if (useGboard) {
            runCatching {
                val r = SpeechRecognizer.createSpeechRecognizer(context, GBOARD_COMPONENT)
                Log.d(TAG, "Using Gboard recognizer")
                return r
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                val r = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                Log.d(TAG, "Using on-device recognizer")
                return r
            }
        }
        Log.d(TAG, "Using system default recognizer")
        return SpeechRecognizer.createSpeechRecognizer(context)
    }

    private fun startSession() {
        if (!running) return
        try {
            recognizer?.destroy()
            recognizer = createRecognizer().apply {
                setRecognitionListener(listener)
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
                }
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "startSession failed", e)
            scheduleRestart(800L)
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        if (!running) return
        handler.postDelayed({ if (running) startSession() }, delayMs)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.let { _partialText.value = it }
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
                .trim()
            if (text.isNotEmpty()) {
                val combined = if (_finalText.value.isBlank()) text
                else _finalText.value.trimEnd() + " " + text
                _finalText.value = combined
            }
            _partialText.value = ""
            scheduleRestart(150L)
        }

        override fun onError(error: Int) {
            // 12 = language not supported → try fallback language
            // 13 = language pack not downloaded → try fallback
            // 7  = no match, 6 = speech timeout, 8 = busy → restart silently
            // 9  = insufficient permissions, 4 = server, 2 = network → surface
            Log.d(TAG, "onError: code=$error, lang=$currentLanguageTag")
            if (error == 12 || error == 13) {
                val next = if (!usingFallback && currentLanguageTag != fallbackLanguageTag && fallbackLanguageTag != null) {
                    usingFallback = true
                    fallbackLanguageTag!!
                } else if (currentLanguageTag != "en-US") {
                    "en-US"
                } else null
                if (next != null) {
                    Log.i(TAG, "Language error $error for '$currentLanguageTag', retrying with '$next'")
                    currentLanguageTag = next
                    scheduleRestart(300L)
                    return
                } else {
                    _error.value = "Language not supported for live transcription. Try selecting English in Settings."
                }
            }
            val msg = when (error) {
                SpeechRecognizer.ERROR_NETWORK -> "Network error (live transcription)"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout (live transcription)"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                else -> null
            }
            if (msg != null) _error.value = msg
            scheduleRestart(if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) 2000L else 400L)
        }
    }
}
