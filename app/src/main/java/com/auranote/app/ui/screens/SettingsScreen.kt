package com.auranote.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auranote.app.ui.components.GlassCard
import com.auranote.app.ui.components.GradientButton
import com.auranote.app.ui.components.InfoBanner
import com.auranote.app.ui.theme.DeepNavy
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.GreenAccent
import com.auranote.app.ui.theme.NavyCard
import com.auranote.app.ui.theme.NavyElevated
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import com.auranote.app.ui.theme.TextTertiary
import com.auranote.app.ui.viewmodel.SettingsViewModel

private val supportedLanguages = listOf(
    "auto" to "Auto-detect",
    "en" to "English",
    "zh" to "Chinese",
    "es" to "Spanish",
    "fr" to "French",
    "de" to "German",
    "ja" to "Japanese",
    "ko" to "Korean",
    "pt" to "Portuguese",
    "ar" to "Arabic",
    "hi" to "Hindi",
    "it" to "Italian",
    "ru" to "Russian",
    "nl" to "Dutch",
    "tr" to "Turkish",
    "pl" to "Polish",
    "sv" to "Swedish",
    "id" to "Indonesian"
)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var apiKeyInput by remember(uiState.apiKey) { mutableStateOf(uiState.apiKey) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(24.dp))

            SettingsSectionTitle(title = "AI Configuration", icon = Icons.Default.AutoAwesome)
            Spacer(Modifier.height(8.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "OpenAI API Key (optional — Gemini AI is built-in)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Required for transcription, summaries, and AI chat features",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("sk-... (optional)", color = TextTertiary) },
                        visualTransformation = if (uiState.apiKeyVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleApiKeyVisibility() }) {
                                Icon(
                                    imageVector = if (uiState.apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = NavyElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PurplePrimary
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    GradientButton(
                        text = if (uiState.saveSuccess) "✓ Saved" else "Save API Key",
                        onClick = { viewModel.saveApiKey(apiKeyInput) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = if (uiState.saveSuccess) Icons.Default.Check else null
                    )
                    Spacer(Modifier.height(8.dp))
                    InfoBanner(
                        message = "Gemini AI (15 keys, 6 models) is built-in and requires no setup. Add an OpenAI key only for Whisper transcription or as fallback.",
                        icon = Icons.Default.Security,
                        tint = TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            SettingsSectionTitle(title = "Transcription", icon = Icons.Default.RecordVoiceOver)
            Spacer(Modifier.height(8.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Box {
                        SettingsRow(
                            title = "Language",
                            subtitle = supportedLanguages.find { it.first == uiState.transcriptionLanguage }?.second ?: "Auto-detect",
                            icon = Icons.Default.Language,
                            onClick = { showLanguageMenu = true }
                        )
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                            modifier = Modifier.background(NavyElevated)
                        ) {
                            supportedLanguages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            name,
                                            color = if (uiState.transcriptionLanguage == code) PurplePrimary else TextPrimary,
                                            fontWeight = if (uiState.transcriptionLanguage == code) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setLanguage(code)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Divider(color = NavyElevated.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsToggleRow(
                        title = "Speaker Detection",
                        subtitle = "Identify and label different speakers",
                        icon = Icons.Default.Person,
                        checked = uiState.speakerDetection,
                        onToggle = viewModel::setSpeakerDetection
                    )

                    Divider(color = NavyElevated.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsToggleRow(
                        title = "Auto-Transcribe",
                        subtitle = "Automatically transcribe after recording stops",
                        icon = Icons.Default.AutoAwesome,
                        checked = uiState.autoTranscribe,
                        onToggle = viewModel::setAutoTranscribe
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            SettingsSectionTitle(title = "Appearance", icon = Icons.Default.Brightness4)
            Spacer(Modifier.height(8.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("DARK",   "Dark",   Icons.Default.Brightness4),
                            Triple("LIGHT",  "Light",  Icons.Default.Brightness7),
                            Triple("SYSTEM", "System", Icons.Default.BrightnessAuto)
                        ).forEach { (code, label, icon) ->
                            val selected = uiState.appTheme == code
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) PurplePrimary.copy(alpha = 0.18f)
                                        else NavyElevated.copy(alpha = 0.4f)
                                    )
                                    .clickable { viewModel.setTheme(code) }
                                    .padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    icon, null,
                                    tint = if (selected) PurplePrimary else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) PurplePrimary else TextSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(PurplePrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SettingsSectionTitle(title = "Recording Quality", icon = Icons.Default.Tune)
            Spacer(Modifier.height(8.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    listOf(
                        Triple("HIGH", "High Quality", "128 kbps AAC, best for transcription"),
                        Triple("MEDIUM", "Medium Quality", "64 kbps AAC, balanced"),
                        Triple("LOW", "Low Quality", "32 kbps AAC, smaller files")
                    ).forEach { (code, name, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (uiState.recordingQuality == code)
                                        PurplePrimary.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            if (uiState.recordingQuality == code) {
                                Box(
                                    modifier = Modifier.size(22.dp).clip(CircleShape).background(PurplePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                                }
                            } else {
                                TextButton(onClick = { viewModel.setRecordingQuality(code) }) {
                                    Text("Select", color = PurplePrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("AuraNote", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "AI-powered meeting notes & transcription",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = PurplePrimary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = PurplePrimary,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = NavyElevated
            )
        )
    }
}
