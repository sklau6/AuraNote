package com.auranote.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.RecordingType
import com.auranote.app.data.model.TranscriptionStatus
import com.auranote.app.ui.components.EmptyStateView
import com.auranote.app.ui.components.RecordingCard
import com.auranote.app.ui.theme.CyanAccent
import com.auranote.app.ui.theme.DeepNavy
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientMid
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.GreenAccent
import com.auranote.app.ui.theme.NavyCard
import com.auranote.app.ui.theme.NavyElevated
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import com.auranote.app.ui.theme.TextTertiary
import com.auranote.app.ui.theme.VioletAccent
import com.auranote.app.ui.viewmodel.HomeViewModel
import com.auranote.app.util.FileUtils
import com.auranote.app.util.TimeUtils
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToRecord: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val totalMs = uiState.recordings.sumOf { it.durationMs }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {

            // ── Header ──────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(GradientStart.copy(alpha = 0.18f), DeepNavy)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "AuraNote",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            IconButton(onClick = { viewModel.toggleSearch() }) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NavyCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.showSearch) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Stats strip
                        if (uiState.recordings.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatPill(
                                    icon = Icons.Default.Mic,
                                    value = "${uiState.recordings.size}",
                                    label = "Recordings",
                                    color = GradientStart
                                )
                                StatPill(
                                    icon = Icons.Default.Schedule,
                                    value = TimeUtils.formatDuration(totalMs),
                                    label = "Recorded",
                                    color = CyanAccent
                                )
                                StatPill(
                                    icon = Icons.Default.AutoAwesome,
                                    value = "${uiState.recordings.count { it.transcriptionStatus == TranscriptionStatus.COMPLETED }}",
                                    label = "AI Notes",
                                    color = GreenAccent
                                )
                            }
                        }

                        // Search bar
                        AnimatedVisibility(
                            visible = uiState.showSearch,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                TextField(
                                    value = uiState.searchQuery,
                                    onValueChange = viewModel::setSearchQuery,
                                    placeholder = { Text("Search recordings…", color = TextTertiary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = NavyCard,
                                        unfocusedContainerColor = NavyCard,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = PurplePrimary
                                    ),
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        if (uiState.searchQuery.isNotBlank()) {
                                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                Icon(Icons.Default.Close, "Clear", tint = TextTertiary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // ── Quick record CTA (when list is empty) ──────────────────────
            if (uiState.recordings.isEmpty() && !uiState.isLoading) {
                item {
                    QuickRecordBanner(onClick = onNavigateToRecord)
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.Mic,
                            title = "No recordings yet",
                            subtitle = "Tap the mic button to capture your first meeting, lecture, or voice note",
                            action = "Start Recording",
                            onAction = onNavigateToRecord
                        )
                    }
                }
            } else {
                // ── Filter chips ────────────────────────────────────────────
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedFilter == null,
                                onClick = { viewModel.setFilter(null) },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = NavyCard,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                        items(RecordingType.values()) { type ->
                            FilterChip(
                                selected = uiState.selectedFilter == type,
                                onClick = { viewModel.setFilter(if (uiState.selectedFilter == type) null else type) },
                                label = { Text(type.label) },
                                leadingIcon = {
                                    Icon(typeIcon(type), null, modifier = Modifier.size(14.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = NavyCard,
                                    labelColor = TextSecondary,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }

                // ── Date-grouped recordings ─────────────────────────────────
                val grouped = groupByDate(uiState.recordings)
                grouped.forEach { (section, recs) ->
                    item(key = "header_$section") {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextTertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
                        )
                    }
                    items(recs, key = { it.id }) { recording ->
                        RecordingCard(
                            recording = recording,
                            onClick = { onNavigateToDetail(recording.id) },
                            onDelete = { viewModel.deleteRecording(recording) },
                            onToggleFavorite = { viewModel.toggleFavorite(recording) },
                            onShare = { FileUtils.shareAudioFile(context, recording.filePath) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(label, color = color.copy(alpha = 0.75f), fontSize = 11.sp)
    }
}

@Composable
private fun QuickRecordBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(GradientStart, GradientMid, GradientEnd)))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Start Recording",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tap to capture a meeting, lecture, or note",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun groupByDate(recordings: List<Recording>): LinkedHashMap<String, List<Recording>> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val todayStart = cal.timeInMillis
    val yesterdayStart = todayStart - TimeUnit.DAYS.toMillis(1)
    val weekStart = todayStart - TimeUnit.DAYS.toMillis(7)
    val monthStart = todayStart - TimeUnit.DAYS.toMillis(30)

    val result = LinkedHashMap<String, MutableList<Recording>>()
    recordings.sortedByDescending { it.createdAt }.forEach { rec ->
        val key = when {
            rec.createdAt >= todayStart -> "Today"
            rec.createdAt >= yesterdayStart -> "Yesterday"
            rec.createdAt >= weekStart -> "This Week"
            rec.createdAt >= monthStart -> "This Month"
            else -> "Older"
        }
        result.getOrPut(key) { mutableListOf() }.add(rec)
    }
    @Suppress("UNCHECKED_CAST")
    return result as LinkedHashMap<String, List<Recording>>
}

private fun typeIcon(type: RecordingType): ImageVector = when (type) {
    RecordingType.MEETING -> Icons.Default.Work
    RecordingType.LECTURE -> Icons.Default.School
    RecordingType.INTERVIEW -> Icons.Default.Person
    RecordingType.CALL -> Icons.Default.Call
    RecordingType.PERSONAL -> Icons.Default.Star
    RecordingType.OTHER -> Icons.Default.Mic
}
