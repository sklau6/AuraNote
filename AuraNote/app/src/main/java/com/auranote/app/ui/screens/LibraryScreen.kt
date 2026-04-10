package com.auranote.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auranote.app.ui.components.EmptyStateView
import com.auranote.app.ui.components.RecordingCard
import com.auranote.app.ui.theme.CyanAccent
import com.auranote.app.ui.theme.DeepNavy
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.NavyCard
import com.auranote.app.ui.theme.NavyElevated
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import com.auranote.app.ui.theme.TextTertiary
import com.auranote.app.ui.viewmodel.HomeViewModel
import com.auranote.app.util.FileUtils
import com.auranote.app.util.TimeUtils

private enum class SortOrder(val label: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    DURATION("Longest first"),
    NAME("Alphabetical")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val sortedRecordings = remember(uiState.recordings, sortOrder) {
        when (sortOrder) {
            SortOrder.DATE_DESC -> uiState.recordings.sortedByDescending { it.createdAt }
            SortOrder.DATE_ASC -> uiState.recordings.sortedBy { it.createdAt }
            SortOrder.DURATION -> uiState.recordings.sortedByDescending { it.durationMs }
            SortOrder.NAME -> uiState.recordings.sortedBy { it.title.lowercase() }
        }
    }

    val totalMs = uiState.recordings.sumOf { it.durationMs }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {

            // ── Header ────────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(GradientEnd.copy(alpha = 0.14f), DeepNavy))
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Library",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${uiState.recordings.size} recordings  ·  ${TimeUtils.formatDuration(totalMs)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NavyCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Sort, "Sort", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(NavyElevated)
                            ) {
                                SortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                order.label,
                                                color = if (sortOrder == order) GradientStart else TextPrimary,
                                                fontWeight = if (sortOrder == order) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            if (sortOrder == order) {
                                                Icon(Icons.Default.Check, null, tint = GradientStart, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        onClick = { sortOrder = order; showSortMenu = false }
                                    )
                                }
                            }
                        }
                    }

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

                    // Stats chips when there are recordings
                    if (uiState.recordings.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LibStatChip(
                                value = "${uiState.recordings.count { it.isFavorite }}",
                                label = "Starred",
                                color = CyanAccent
                            )
                            LibStatChip(
                                value = "${uiState.recordings.count { it.speakerCount > 1 }}",
                                label = "Multi-speaker",
                                color = GradientStart
                            )
                        }
                    }
                }
            }

            // ── Empty state ──────────────────────────────────────────────────
            if (sortedRecordings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.FolderOpen,
                            title = if (uiState.searchQuery.isNotBlank()) "No results found" else "Library is empty",
                            subtitle = if (uiState.searchQuery.isNotBlank())
                                "Try a different search term"
                            else
                                "Your recordings will appear here once you start capturing"
                        )
                    }
                }
            } else {
                // Section label
                item {
                    Text(
                        text = "${sortedRecordings.size} recording${if (sortedRecordings.size != 1) "s" else ""}  ·  ${sortOrder.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                items(sortedRecordings, key = { it.id }) { recording ->
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

@Composable
private fun LibStatChip(value: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
        Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.8f))
    }
}
