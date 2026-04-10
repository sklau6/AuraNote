package com.auranote.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.RecordingType
import com.auranote.app.data.model.TranscriptionStatus
import com.auranote.app.ui.theme.AmberAccent
import com.auranote.app.ui.theme.CyanAccent
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.GreenAccent
import com.auranote.app.ui.theme.NavyCard
import com.auranote.app.ui.theme.NavyElevated
import com.auranote.app.ui.theme.PinkAccent
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.RedAccent
import com.auranote.app.ui.theme.StatusDone
import com.auranote.app.ui.theme.StatusError
import com.auranote.app.ui.theme.StatusPending
import com.auranote.app.ui.theme.StatusProcessing
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import com.auranote.app.ui.theme.TextTertiary
import com.auranote.app.util.TimeUtils
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordingCard(
    recording: Recording,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val accent = typeColor(recording.type)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NavyCard)
            .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
    ) {
        // Accent left border
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(80.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                .background(Brush.verticalGradient(listOf(accent, accent.copy(alpha = 0.3f))))
        )

        Column(modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 12.dp)) {
            // ── Header row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeBadge(type = recording.type)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = TimeUtils.formatRelativeTime(recording.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
                if (recording.isFavorite) {
                    Icon(Icons.Default.Favorite, null, tint = PinkAccent, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.MoreVert, "More", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(NavyElevated)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (recording.isFavorite) "Remove Favorite" else "Add to Favorites", color = TextPrimary)
                            },
                            leadingIcon = {
                                Icon(
                                    if (recording.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    null, tint = PinkAccent
                                )
                            },
                            onClick = { showMenu = false; onToggleFavorite() }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Share, null, tint = CyanAccent) },
                            onClick = { showMenu = false; onShare() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = RedAccent) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = RedAccent) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Waveform thumbnail ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                StaticWaveformThumbnail(
                    amplitudes = generateFakeAmplitudes(recording.id),
                    modifier = Modifier.fillMaxSize(),
                    activeColor = accent,
                    progress = 0f
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Metadata row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetaChip(text = TimeUtils.formatDuration(recording.durationMs), color = accent)
                    if (recording.fileSizeBytes > 0) {
                        Text("·", color = TextTertiary, fontSize = 10.sp)
                        Text(TimeUtils.formatFileSize(recording.fileSizeBytes), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                    if (recording.speakerCount > 0) {
                        Text("·", color = TextTertiary, fontSize = 10.sp)
                        Text("${recording.speakerCount} spk", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }
                TranscriptionStatusBadge(status = recording.transcriptionStatus)
            }
        }
    }
}

@Composable
private fun TypeBadge(type: RecordingType) {
    val color = typeColor(type)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0.08f))))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun MetaChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun TranscriptionStatusBadge(status: TranscriptionStatus) {
    val (color, label) = when (status) {
        TranscriptionStatus.PENDING -> Pair(StatusPending, "Pending")
        TranscriptionStatus.IN_PROGRESS -> Pair(StatusProcessing, "Processing…")
        TranscriptionStatus.COMPLETED -> Pair(StatusDone, "Transcribed")
        TranscriptionStatus.ERROR -> Pair(StatusError, "Error")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}

private fun typeColor(type: RecordingType): Color = when (type) {
    RecordingType.MEETING -> GradientStart
    RecordingType.LECTURE -> CyanAccent
    RecordingType.INTERVIEW -> AmberAccent
    RecordingType.CALL -> GreenAccent
    RecordingType.PERSONAL -> PinkAccent
    RecordingType.OTHER -> GradientEnd
}

private fun generateFakeAmplitudes(seed: Long): List<Float> {
    val rng = seed.toDouble()
    return (0 until 40).map { i ->
        ((sin(rng * 0.7 + i * 0.8) + sin(rng * 1.3 + i * 0.4)) / 2.0 + 1.0).toFloat() / 2f * 0.8f + 0.1f
    }
}
