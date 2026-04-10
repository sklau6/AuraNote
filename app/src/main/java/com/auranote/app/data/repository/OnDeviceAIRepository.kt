package com.auranote.app.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OnDeviceAIRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "OnDeviceAIRepository"
    }

    /**
     * Transcribe audio using Android's built-in on-device SpeechRecognizer.
     * Works offline on Android 13+ (API 33) via EXTRA_PREFER_OFFLINE.
     * On older devices it may use network; caller should check availability first.
     */
    suspend fun transcribeAudioFile(audioFile: File): Result<String> =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    cont.resumeWithException(
                        IllegalStateException("Speech recognition not available on this device")
                    )
                    return@suspendCancellableCoroutine
                }

                val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                    ?: SpeechRecognizer.createSpeechRecognizer(context)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, audioFile.absolutePath)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}

                    override fun onResults(results: Bundle?) {
                        recognizer.destroy()
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty().trim()
                        if (text.isBlank()) {
                            cont.resumeWithException(IllegalStateException("No speech detected in audio file"))
                        } else {
                            cont.resume(Result.success(text))
                        }
                    }

                    override fun onError(error: Int) {
                        recognizer.destroy()
                        cont.resumeWithException(
                            IllegalStateException("Speech recognition error code: $error")
                        )
                    }
                })

                cont.invokeOnCancellation { recognizer.destroy() }
                recognizer.startListening(intent)
            }
        }

    /**
     * Returns whether Android on-device speech recognition is available.
     */
    fun isSpeechRecognitionAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Produce a basic extractive summary from transcript text without any AI model.
     * Splits into sentences, scores by word frequency, returns top sentences as bullets.
     */
    suspend fun summarizeText(text: String): Result<String> = withContext(Dispatchers.Default) {
        runCatching {
            val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.length > 20 }
            if (sentences.isEmpty()) throw IllegalStateException("No content to summarize")

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

            topSentences.joinToString("\n").ifBlank {
                throw IllegalStateException("Could not generate summary")
            }
        }
    }

    /**
     * Stub for prompt-based generation without an AI model.
     * Returns a message directing user to add an OpenAI key for this feature.
     */
    suspend fun generateWithPrompt(prompt: String): Result<String> =
        Result.failure(IllegalStateException(
            "AI generation requires an OpenAI API key. Add one in Settings to enable summaries, flashcards, and chat."
        ))
}
