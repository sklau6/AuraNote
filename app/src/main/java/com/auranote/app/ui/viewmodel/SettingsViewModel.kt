package com.auranote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auranote.app.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val apiKeyVisible: Boolean = false,
    val transcriptionLanguage: String = "auto",
    val speakerDetection: Boolean = true,
    val autoTranscribe: Boolean = true,
    val recordingQuality: String = "HIGH",
    val saveSuccess: Boolean = false,
    val appTheme: String = "DARK"
)

private data class PrefsSnapshot(
    val apiKey: String,
    val language: String,
    val speakerDetection: Boolean,
    val autoTranscribe: Boolean,
    val quality: String,
    val theme: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences
) : ViewModel() {

    private val _apiKeyVisible = MutableStateFlow(false)
    private val _saveSuccess = MutableStateFlow(false)

    private val prefsFlow = combine(
        preferences.apiKey,
        preferences.transcriptionLanguage,
        preferences.speakerDetection,
        preferences.autoTranscribe,
        preferences.recordingQuality,
        preferences.appTheme
    ) { values ->
        PrefsSnapshot(
            apiKey = values[0] as String,
            language = values[1] as String,
            speakerDetection = values[2] as Boolean,
            autoTranscribe = values[3] as Boolean,
            quality = values[4] as String,
            theme = values[5] as String
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        prefsFlow,
        _apiKeyVisible,
        _saveSuccess
    ) { prefs, visible, success ->
        SettingsUiState(
            apiKey = prefs.apiKey,
            apiKeyVisible = visible,
            transcriptionLanguage = prefs.language,
            speakerDetection = prefs.speakerDetection,
            autoTranscribe = prefs.autoTranscribe,
            recordingQuality = prefs.quality,
            saveSuccess = success,
            appTheme = prefs.theme
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            preferences.setApiKey(key.trim())
            _saveSuccess.value = true
            delay(2000)
            _saveSuccess.value = false
        }
    }

    fun toggleApiKeyVisibility() {
        _apiKeyVisible.update { !it }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { preferences.setTranscriptionLanguage(lang) }
    }

    fun setSpeakerDetection(enabled: Boolean) {
        viewModelScope.launch { preferences.setSpeakerDetection(enabled) }
    }

    fun setAutoTranscribe(enabled: Boolean) {
        viewModelScope.launch { preferences.setAutoTranscribe(enabled) }
    }

    fun setRecordingQuality(quality: String) {
        viewModelScope.launch { preferences.setRecordingQuality(quality) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { preferences.setAppTheme(theme) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { preferences.setOnboardingCompleted(true) }
    }
}
