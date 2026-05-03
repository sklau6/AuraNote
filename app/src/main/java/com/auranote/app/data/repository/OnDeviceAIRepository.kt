package com.auranote.app.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.auranote.app.util.PcmDecoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device AI helpers that don't require an OpenAI API key.
 *
 * The transcription path uses Android's on-device [SpeechRecognizer]
 * (the same engine that powers Google's "Live Transcribe" / "Recorder"
 * apps). Because that engine only accepts raw 16-bit PCM, we first
 * decode the m4a/aac/mp3/wav recording with [PcmDecoder] and then feed
 * the bytes through `EXTRA_AUDIO_SOURCE` (a [ParcelFileDescriptor]).
 *
 * Officially this file-source flow only works on Android 13+ (API 33);
 * on older devices we surface a clean error so the app never crashes.
 */
@Singleton
class OnDeviceAIRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "OnDeviceAIRepository"
        private const val MIN_API_FOR_FILE_SOURCE = Build.VERSION_CODES.TIRAMISU // 33
        const val NEEDS_LANGUAGE_PACK = "NEEDS_LANGUAGE_PACK"

        /** Map locale variants the Google engine doesn't recognise to ones it does. */
        private fun normalizeLanguageTag(tag: String): String = when {
            tag.startsWith("zh-HK") || tag.startsWith("zh-MO") -> "zh-TW"
            tag.startsWith("zh-SG") -> "zh-CN"
            else -> tag
        }

        /** Ordered list of language tags to try for a given user selection. */
        private fun fallbackChain(resolvedTag: String): List<String> {
            val normalized = normalizeLanguageTag(resolvedTag)
            return buildList {
                add(normalized)
                if (normalized.startsWith("zh-TW") || normalized.startsWith("zh-HK")) add("zh-CN")
                if (normalized != "en-US") add("en-US")
            }.distinct()
        }
    }

    /**
     * Transcribe audio file using Google's cloud speech recognition via Gboard.
     * This uses RecognizerIntent which launches Google's voice input dialog.
     * More reliable than playback-based approach.
     */
    suspend fun transcribeAudioFile(audioFile: File, languageTag: String = "auto"): Result<String> {
        if (!audioFile.exists() || audioFile.length() == 0L)
            return Result.failure(IllegalStateException("Audio file is missing or empty"))
        if (!SpeechRecognizer.isRecognitionAvailable(context))
            return Result.failure(IllegalStateException(
                "Speech recognition is not available. Install Google app or add an OpenAI API key."
            ))
        val raw = if (languageTag == "auto" || languageTag.isBlank())
            Locale.getDefault().toLanguageTag() else languageTag
        val chain = fallbackChain(raw)
        Log.d(TAG, "Starting single-pass playback transcription, chain=$chain")
        return withContext(Dispatchers.Main) { runSinglePassTranscription(audioFile, chain) }
    }

    /**
     * Single continuous playback + recognition session.
     * Uses one long SpeechRecognizer session covering the entire audio duration.
     */
    private suspend fun runSinglePassTranscription(
        audioFile: File,
        langChain: List<String>
    ): Result<String> = suspendCancellableCoroutine { cont ->
        val handler = Handler(Looper.getMainLooper())
        val transcript = StringBuilder()
        val partialBuffer = StringBuilder()
        var finished = false
        var mediaPlayer: MediaPlayer? = null
        var recognizer: SpeechRecognizer? = null
        var langIndex = 0
        var lastResultTime = System.currentTimeMillis()

        fun finish(r: Result<String>) {
            if (finished) return
            finished = true
            handler.removeCallbacksAndMessages(null)
            runCatching { recognizer?.destroy() }
            runCatching { mediaPlayer?.stop() }
            runCatching { mediaPlayer?.release() }
            if (cont.isActive) cont.resume(r)
        }

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) {
                Log.d(TAG, "Recognizer ready")
                lastResultTime = System.currentTimeMillis()
            }
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech detected")
                lastResultTime = System.currentTimeMillis()
            }
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d(TAG, "End of speech segment")
            }
            override fun onEvent(t: Int, p: Bundle?) {}

            override fun onPartialResults(r: Bundle?) {
                val text = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty().trim()
                if (text.isNotBlank()) {
                    partialBuffer.setLength(0)
                    partialBuffer.append(text)
                    lastResultTime = System.currentTimeMillis()
                    Log.d(TAG, "Partial: $text")
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty().trim()
                if (text.isNotBlank()) {
                    if (transcript.isNotBlank()) transcript.append(' ')
                    transcript.append(text)
                    partialBuffer.setLength(0)
                    lastResultTime = System.currentTimeMillis()
                    Log.d(TAG, "Final segment: $text, total: ${transcript.length} chars")
                }
                // Don't restart - continue listening for more speech
            }

            override fun onError(error: Int) {
                Log.w(TAG, "Recognizer error $error, lang=${langChain.getOrElse(langIndex){"en-US"}}")
                when (error) {
                    6 -> { // Speech timeout - no speech detected, continue
                        handler.postDelayed({
                            if (!finished && isStillPlaying(mediaPlayer)) {
                                // Keep listening
                            }
                        }, 500)
                    }
                    7 -> { // No match - continue listening
                        handler.postDelayed({
                            if (!finished && isStillPlaying(mediaPlayer)) {
                                // Continue
                            }
                        }, 200)
                    }
                    12, 13 -> { // Language not supported / pack missing
                        if (langIndex + 1 < langChain.size) {
                            langIndex++
                            Log.i(TAG, "Switching to ${langChain[langIndex]}")
                            restartRecognizer()
                        } else {
                            finish(Result.failure(IllegalStateException(
                                "$NEEDS_LANGUAGE_PACK: Language not supported. Please select English."
                            )))
                        }
                    }
                    else -> {
                        // Other errors, try to continue
                        if (isStillPlaying(mediaPlayer)) {
                            restartRecognizer()
                        } else {
                            val t = transcript.toString().trim()
                            finish(if (t.isBlank()) Result.failure(IllegalStateException("Recognition error $error"))
                                   else Result.success(t))
                        }
                    }
                }
            }
        }

        fun isStillPlaying(mp: MediaPlayer?): Boolean = runCatching {
            mp?.isPlaying == true
        }.getOrDefault(false)

        fun restartRecognizer() {
            if (finished) return
            runCatching { recognizer?.destroy() }
            val lang = langChain.getOrElse(langIndex) { "en-US" }
            runCatching {
                recognizer = createRecognizerForPlayback().also { r ->
                    r.setRecognitionListener(listener)
                    r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5_000L)
                        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2_000L)
                        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
                        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    })
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to restart recognizer", e)
                val t = transcript.toString().trim()
                finish(if (t.isBlank()) Result.failure(e)
                       else Result.success(t))
            }
        }

        // Start audio playback
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                // Max volume for playback so mic can hear it
                setVolume(1.0f, 1.0f)
                setOnCompletionListener {
                    Log.d(TAG, "Playback complete, transcript length: ${transcript.length}")
                    handler.postDelayed({
                        val t = transcript.toString().trim()
                        finish(if (t.isBlank()) {
                            // Try partial buffer as fallback
                            val partial = partialBuffer.toString().trim()
                            if (partial.isNotBlank()) Result.success(partial)
                            else Result.failure(IllegalStateException("No speech detected"))
                        } else Result.success(t))
                    }, 2_000) // Wait for final results
                }
                setOnErrorListener { _, what, _ ->
                    Log.e(TAG, "MediaPlayer error: $what")
                    val t = transcript.toString().trim()
                    finish(if (t.isBlank()) Result.failure(IllegalStateException("Audio playback error"))
                           else Result.success(t))
                    true
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            finish(Result.failure(IllegalStateException("Cannot play audio: ${e.message}")))
            return@suspendCancellableCoroutine
        }

        // Start recognizer
        restartRecognizer()

        // Watchdog: ensure we finish even if things get stuck
        val durationMs = runCatching { mediaPlayer!!.duration.toLong() }.getOrDefault(60_000L).coerceAtLeast(10_000L)
        handler.postDelayed({
            if (!finished) {
                val elapsed = System.currentTimeMillis() - lastResultTime
                val t = transcript.toString().trim()
                Log.d(TAG, "Watchdog triggered, elapsed since last result: ${elapsed}ms, transcript length: ${t.length}")
                if (t.isBlank()) {
                    val partial = partialBuffer.toString().trim()
                    finish(if (partial.isNotBlank()) Result.success(partial)
                           else Result.failure(IllegalStateException("Transcription incomplete - no speech detected")))
                } else {
                    finish(Result.success(t))
                }
            }
        }, durationMs + 20_000L)

        // Progress checker - restart recognizer if no results for a while
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (finished) return
                val elapsed = System.currentTimeMillis() - lastResultTime
                if (elapsed > 15_000 && isStillPlaying(mediaPlayer)) {
                    Log.d(TAG, "No results for ${elapsed}ms, restarting recognizer")
                    restartRecognizer()
                }
                if (!finished) handler.postDelayed(this, 5_000)
            }
        }, 10_000)

        cont.invokeOnCancellation {
            runCatching { recognizer?.destroy() }
            runCatching { mediaPlayer?.stop(); mediaPlayer?.release() }
        }
    }

    private fun createRecognizerForPlayback(): SpeechRecognizer {
        // Prefer system default which uses cloud recognition
        return SpeechRecognizer.createSpeechRecognizer(context)
    }

    /**
     * Run a single SpeechRecognizer session against a raw-PCM file.
     * Caller must invoke from Main thread.
     */
    private suspend fun runRecognizer(pcmFile: File, languageTag: String): String =
        suspendCancellableCoroutine { cont ->
            var pfd: ParcelFileDescriptor? = null
            var recognizer: SpeechRecognizer? = null
            // Watchdog so a stuck recogniser can never hang forever.
            val handler = Handler(Looper.getMainLooper())
            val partials = StringBuilder()
            val finals = StringBuilder()
            var finished = false

            fun finishOk(text: String) {
                if (finished) return
                finished = true
                handler.removeCallbacksAndMessages(null)
                try { recognizer?.destroy() } catch (_: Exception) {}
                try { pfd?.close() } catch (_: Exception) {}
                if (cont.isActive) cont.resume(text)
            }
            fun finishErr(msg: String) {
                if (finished) return
                finished = true
                handler.removeCallbacksAndMessages(null)
                try { recognizer?.destroy() } catch (_: Exception) {}
                try { pfd?.close() } catch (_: Exception) {}
                Log.w(TAG, "Recognizer ended with: $msg")
                if (cont.isActive) cont.resumeWithException(IllegalStateException(msg))
            }

            try {
                pfd = ParcelFileDescriptor.open(
                    pcmFile, ParcelFileDescriptor.MODE_READ_ONLY
                )
                // Prefer the on-device engine (API 31+); fall back to the
                // generic one if creation throws or returns null.
                recognizer = createRecognizer()
                if (recognizer == null) {
                    finishErr("Could not create SpeechRecognizer")
                    return@suspendCancellableCoroutine
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}

                    override fun onPartialResults(partialResults: Bundle?) {
                        val txt = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull().orEmpty()
                        if (txt.isNotBlank()) {
                            partials.setLength(0)
                            partials.append(txt)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val txt = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull().orEmpty().trim()
                        if (txt.isNotBlank()) {
                            if (finals.isNotEmpty()) finals.append(' ')
                            finals.append(txt)
                        }
                        // For files, onResults fires once at the end of the stream.
                        val combined = listOf(finals.toString(), partials.toString())
                            .filter { it.isNotBlank() }.joinToString(" ").trim()
                        finishOk(combined)
                    }

                    override fun onError(error: Int) {
                        val msg = describeError(error)
                        // Even on error we may have collected useful partials.
                        val tail = (finals.toString() + " " + partials.toString()).trim()
                        if (tail.isNotBlank()) finishOk(tail)
                        else finishErr(msg)
                    }
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // File-source extras (added in API 33).
                        putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, pfd)
                        putExtra(
                            RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING,
                            AudioFormat.ENCODING_PCM_16BIT
                        )
                        putExtra(
                            RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE,
                            PcmDecoder.TARGET_SAMPLE_RATE
                        )
                        putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                    }
                }

                cont.invokeOnCancellation { finishErr("cancelled") }

                // Watchdog: 5 s slack on top of audio length.
                val pcmDurationMs = (pcmFile.length() * 1000) /
                    (PcmDecoder.TARGET_SAMPLE_RATE * 2 /* 16-bit mono */).toLong()
                val timeoutMs = pcmDurationMs.coerceAtLeast(15_000L) + 30_000L
                handler.postDelayed({
                    val tail = (finals.toString() + " " + partials.toString()).trim()
                    if (tail.isNotBlank()) finishOk(tail) else finishErr("watchdog timeout")
                }, timeoutMs)

                recognizer.startListening(intent)
            } catch (t: Throwable) {
                Log.e(TAG, "runRecognizer setup failed", t)
                finishErr(t.message ?: t.javaClass.simpleName)
            }
        }

    private fun createRecognizer(): SpeechRecognizer? {
        // Try Gboard first — it uses online recognition supporting 100+ languages without downloads.
        runCatching {
            val gboard = ComponentName(
                "com.google.android.inputmethod.latin",
                "com.google.android.inputmethod.latin.voice.VoiceRecognitionService"
            )
            val r = SpeechRecognizer.createSpeechRecognizer(context, gboard)
            Log.d(TAG, "Using Gboard recognizer for file transcription")
            return r
        }
        // Fallback: system default (works online)
        return try {
            SpeechRecognizer.createSpeechRecognizer(context)
        } catch (t: Throwable) {
            Log.e(TAG, "createRecognizer failed", t)
            null
        }
    }

    private fun describeError(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognised"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        12 -> "Language not supported on this device"
        13 -> "Language pack not downloaded"
        14 -> "Recognition cannot start — try again"
        15 -> "Server disconnected"
        else -> "Speech recognition error ($code)"
    }

    /**
     * Returns whether on-device transcription of saved files is feasible
     * on this device. Used by the UI to surface the right CTA.
     */
    fun isFileTranscriptionAvailable(): Boolean =
        Build.VERSION.SDK_INT >= MIN_API_FOR_FILE_SOURCE &&
        SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Returns whether ANY form of speech recognition is available.
     */
    fun isSpeechRecognitionAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Produce a basic extractive summary from transcript text without any AI model.
     */
    suspend fun summarizeText(text: String): Result<String> = withContext(Dispatchers.Default) {
        runCatching {
            val sentences = text.split(Regex("(?<=[.!?。！？])\\s+")).filter { it.length > 12 }
            if (sentences.isEmpty()) {
                // Fallback for short transcripts (often non-Latin scripts without sentence punctuation).
                return@runCatching "• ${text.trim()}"
            }
            val wordFreq = text.lowercase()
                .split(Regex("\\W+"))
                .filter { it.length > 3 }
                .groupingBy { it }
                .eachCount()

            val scored = sentences.map { sentence ->
                val score = sentence.lowercase().split(Regex("\\W+"))
                    .sumOf { wordFreq[it] ?: 0 }
                Pair(sentence.trim(), score)
            }
            val topSentences = scored
                .sortedByDescending { it.second }
                .take(3)
                .sortedBy { sentences.indexOf(it.first) }
                .map { "• ${it.first}" }
            topSentences.joinToString("\n").ifBlank { "• ${text.trim().take(200)}" }
        }
    }

    suspend fun generateWithPrompt(prompt: String): Result<String> =
        Result.failure(IllegalStateException(
            "AI generation requires an OpenAI API key. Add one in Settings to enable summaries, flashcards, and chat."
        ))
}
