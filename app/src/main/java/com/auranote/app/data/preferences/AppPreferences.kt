package com.auranote.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auranote_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_API_KEY = stringPreferencesKey("openai_api_key")
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val KEY_LANGUAGE = stringPreferencesKey("transcription_language")
        val KEY_SPEAKER_DETECTION = booleanPreferencesKey("speaker_detection")
        val KEY_AUTO_TRANSCRIBE = booleanPreferencesKey("auto_transcribe")
        val KEY_RECORDING_QUALITY = stringPreferencesKey("recording_quality")
        val KEY_THEME = stringPreferencesKey("app_theme")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[KEY_API_KEY] ?: "" }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING] ?: false }
    val transcriptionLanguage: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "auto" }
    val speakerDetection: Flow<Boolean> = context.dataStore.data.map { it[KEY_SPEAKER_DETECTION] ?: true }
    val autoTranscribe: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_TRANSCRIBE] ?: true }
    val recordingQuality: Flow<String> = context.dataStore.data.map { it[KEY_RECORDING_QUALITY] ?: "HIGH" }

    /** Persisted theme mode: "DARK", "LIGHT", or "SYSTEM". */
    val appTheme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "DARK" }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[KEY_API_KEY] = key }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING] = done }
    }

    suspend fun setTranscriptionLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setSpeakerDetection(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SPEAKER_DETECTION] = enabled }
    }

    suspend fun setAutoTranscribe(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_TRANSCRIBE] = enabled }
    }

    suspend fun setRecordingQuality(quality: String) {
        context.dataStore.edit { it[KEY_RECORDING_QUALITY] = quality }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }
}
