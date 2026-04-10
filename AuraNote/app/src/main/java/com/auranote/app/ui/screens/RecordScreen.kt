package com.auranote.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auranote.app.data.model.RecordingType
import com.auranote.app.service.RecorderState
import com.auranote.app.ui.components.WaveformVisualizer
import com.auranote.app.ui.theme.AmberAccent
import com.auranote.app.ui.theme.DeepNavy
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.NavyCard
import com.auranote.app.ui.theme.NavyElevated
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import com.auranote.app.ui.theme.TextTertiary
import com.auranote.app.ui.viewmodel.RecordViewModel
import com.auranote.app.util.TimeUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordScreen(
    onBack: () -> Unit,
    onRecordingComplete: (Long) -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarState = remember { SnackbarHostState() }
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importAudioFile(context, it) }
    }

    LaunchedEffect(uiState.savedRecordingId) {
        uiState.savedRecordingId?.let { id -> onRecordingComplete(id) }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarState.showSnackbar(it); viewModel.clearError() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "recAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f, targetValue = 0.30f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "glow"
    )

    val isRecording = uiState.recorderState == RecorderState.RECORDING
    val isPaused = uiState.recorderState == RecorderState.PAUSED
    val isIdle = uiState.recorderState == RecorderState.IDLE
    val isActive = isRecording || isPaused

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        // Dynamic background glow
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(
                        when {
                            isRecording -> Color(0xFFEF4444).copy(alpha = glowAlpha)
                            isPaused -> AmberAccent.copy(alpha = glowAlpha * 0.7f)
                            else -> GradientStart.copy(alpha = 0.12f)
                        },
                        Color.Transparent
                    ),
                    radius = 750f
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { viewModel.cancelRecording(); onBack() }) {
                    Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Medium)
                }

                AnimatedContent(targetState = uiState.recorderState, label = "stateLabel") { state ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when (state) {
                                    RecorderState.RECORDING -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                    RecorderState.PAUSED -> AmberAccent.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        if (state == RecorderState.RECORDING) {
                            RecordingPulseDot()
                        }
                        Text(
                            text = when (state) {
                                RecorderState.IDLE -> "New Recording"
                                RecorderState.RECORDING -> "Recording"
                                RecorderState.PAUSED -> "Paused"
                                RecorderState.STOPPED -> "Saving…"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = when (state) {
                                RecorderState.RECORDING -> Color(0xFFEF4444)
                                RecorderState.PAUSED -> AmberAccent
                                else -> TextPrimary
                            }
                        )
                    }
                }

                TextButton(onClick = { viewModel.stopRecording() }, enabled = isActive) {
                    Text(
                        "Done",
                        color = if (isActive) GradientStart else TextTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Setup fields (idle only) ─────────────────────────────────────
            AnimatedVisibility(
                visible = isIdle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::setTitle,
                        placeholder = { Text("Recording title (optional)", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = NavyElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PurplePrimary,
                            focusedContainerColor = NavyCard,
                            unfocusedContainerColor = NavyCard
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    // ── Import Audio card ────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(GradientStart.copy(alpha = 0.15f), GradientEnd.copy(alpha = 0.08f))
                                )
                            )
                            .clickable { importLauncher.launch("audio/*") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GradientStart.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FileUpload, null, tint = GradientStart, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Import Audio File", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("m4a, mp3, wav, aac, ogg…", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Icon(Icons.Default.FileUpload, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(RecordingType.values()) { type ->
                            FilterChip(
                                selected = uiState.recordingType == type,
                                onClick = { viewModel.setRecordingType(type) },
                                label = { Text(type.label, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = NavyCard,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Timer ────────────────────────────────────────────────────────
            Text(
                text = TimeUtils.formatDuration(uiState.durationMs),
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                color = if (isRecording) TextPrimary else TextSecondary,
                textAlign = TextAlign.Center,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    isRecording -> "Recording • tap to pause"
                    isPaused -> "Paused • tap to resume"
                    else -> "Tap the mic to begin"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // ── Waveform ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(NavyCard)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                WaveformVisualizer(
                    amplitudes = uiState.amplitudes,
                    isRecording = isRecording,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Controls ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Import (left)
                Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedVisibility(visible = isIdle, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                        IconButton(
                            onClick = { importLauncher.launch("audio/*") },
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(NavyCard)
                        ) {
                            Icon(Icons.Default.FileUpload, "Import audio", tint = TextSecondary, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                // Center record / pause button
                Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.22f))
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            when (uiState.recorderState) {
                                RecorderState.IDLE -> {
                                    if (micPermission.status.isGranted) viewModel.startRecording()
                                    else micPermission.launchPermissionRequest()
                                }
                                RecorderState.RECORDING -> viewModel.pauseRecording()
                                RecorderState.PAUSED -> viewModel.resumeRecording()
                                else -> {}
                            }
                        },
                        modifier = Modifier.size(78.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = when {
                                isRecording -> Color(0xFFEF4444)
                                isPaused -> AmberAccent
                                else -> GradientStart
                            }
                        )
                    ) {
                        Icon(
                            imageVector = when {
                                isRecording -> Icons.Default.Pause
                                isPaused -> Icons.Default.PlayArrow
                                else -> Icons.Default.Mic
                            },
                            contentDescription = "Record",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Stop (right)
                Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedVisibility(visible = isActive, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                        FilledIconButton(
                            onClick = { viewModel.stopRecording() },
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = NavyCard)
                        ) {
                            Icon(Icons.Default.Stop, "Stop", tint = TextSecondary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarState, modifier = Modifier.align(Alignment.BottomCenter)) { data ->
            Snackbar(snackbarData = data, containerColor = NavyCard, contentColor = TextPrimary)
        }
    }
}

@Composable
private fun RecordingPulseDot() {
    val inf = rememberInfiniteTransition(label = "dot")
    val alpha by inf.animateFloat(
        initialValue = 1f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "dotAlpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFFEF4444).copy(alpha = alpha))
    )
}
