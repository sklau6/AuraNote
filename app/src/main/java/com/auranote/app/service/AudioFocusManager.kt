package com.auranote.app.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralised AudioFocus handling so the recorder pauses when interrupted by
 * a phone call, voice assistant, or another media app.
 */
@Singleton
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object { private const val TAG = "AudioFocusManager" }

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var listener: ((Int) -> Unit)? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        Log.d(TAG, "Audio focus changed: $change")
        listener?.invoke(change)
    }

    fun request(onFocusChange: (Int) -> Unit): Boolean {
        listener = onFocusChange
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioManager.requestAudioFocus(focusRequest!!) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        if (!granted) Log.w(TAG, "Audio focus NOT granted")
        return granted
    }

    fun abandon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
        listener = null
    }
}
