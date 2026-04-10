package com.auranote.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auranote.app.data.model.AISummary
import com.auranote.app.data.model.Flashcard
import com.auranote.app.data.model.QuizQuestion
import com.auranote.app.data.model.TranscriptSegment
import com.auranote.app.data.model.TranscriptionStatus
import com.auranote.app.data.model.suggestedQuestions
import com.auranote.app.ui.components.AudioPlayerBar
import com.auranote.app.ui.components.GlassCard
import com.auranote.app.ui.components.GradientButton
import com.auranote.app.ui.components.GradientDivider
import com.auranote.app.ui.components.InfoBanner
import com.auranote.app.ui.components.LoadingCard
import com.auranote.app.ui.components.SectionHeader
import com.auranote.app.ui.components.TagChip
import com.auranote.app.ui.components.TranscriptSegmentItem
import com.auranote.app.ui.theme.AmberAccent
import com.auranote.app.ui.theme.CyanAccent
import com.auranote.app.ui.theme.DeepNavy
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.GreenAccent
import com.auranote.app.ui.theme.NavyCard
import com.auranote.app.ui.theme.NavyElevated
import com.auranote.app.ui.theme.NavySurface
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.RedAccent
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import com.auranote.app.ui.theme.TextTertiary
import com.auranote.app.ui.viewmodel.AIChatViewModel
import com.auranote.app.ui.viewmodel.DetailViewModel
import com.auranote.app.util.TimeUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    recordingId: Long,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var editTitleText by remember { mutableStateOf("") }

    LaunchedEffect(recordingId) { viewModel.loadRecording(recordingId) }

    LaunchedEffect(uiState.recording?.title) {
        editTitleText = uiState.recording?.title ?: ""
    }

    val recording = uiState.recording

    if (uiState.apiKeyMissing) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("API Key Required", color = TextPrimary) },
            text = {
                Text(
                    "Please add your OpenAI API key in Settings to use AI features.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK", color = PurplePrimary)
                }
            },
            containerColor = NavyCard
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                if (isEditingTitle) {
                    OutlinedTextField(
                        value = editTitleText,
                        onValueChange = { editTitleText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = NavyCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(onClick = {
                        viewModel.updateTitle(editTitleText)
                        isEditingTitle = false
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = GreenAccent)
                    }
                    IconButton(onClick = { isEditingTitle = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
                    }
                } else {
                    Text(
                        text = recording?.title ?: "Loading...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { isEditingTitle = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit title", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { viewModel.shareTranscript(context) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary)
                    }
                }
            }

            recording?.let { rec ->
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TagChip(text = rec.type.label, color = PurplePrimary)
                    Text(TimeUtils.formatDateTime(rec.createdAt), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text("·", color = TextTertiary)
                    Text(TimeUtils.formatDuration(rec.durationMs), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }

                Spacer(Modifier.height(12.dp))

                AudioPlayerBar(
                    filePath = rec.filePath,
                    totalDurationMs = rec.durationMs,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(12.dp))

                if (rec.transcriptionStatus == TranscriptionStatus.PENDING || rec.transcriptionStatus == TranscriptionStatus.ERROR) {
                    GradientButton(
                        text = if (rec.transcriptionStatus == TranscriptionStatus.ERROR) "Retry Transcription" else "Transcribe with AI",
                        onClick = { viewModel.transcribeRecording() },
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (uiState.isTranscribing) {
                    LoadingCard(
                        message = if (uiState.isAssigningSpeakers) "Identifying speakers..." else "Transcribing audio...",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            val tabs = listOf(
                Pair("Transcript", Icons.Default.Mic),
                Pair("Summary", Icons.Default.Summarize),
                Pair("AI Chat", Icons.Default.Chat),
                Pair("Study", Icons.Default.School)
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavySurface,
                contentColor = PurplePrimary,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                        )
                    }
                },
                divider = { GradientDivider() }
            ) {
                tabs.forEachIndexed { index, (label, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal) },
                        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        selectedContentColor = PurplePrimary,
                        unselectedContentColor = TextTertiary
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> TranscriptTab(viewModel = viewModel, uiState = uiState)
                    1 -> SummaryTab(viewModel = viewModel, uiState = uiState)
                    2 -> AIChatTab(recordingId = recordingId)
                    3 -> StudyTab(viewModel = viewModel, uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun TranscriptTab(
    viewModel: DetailViewModel,
    uiState: com.auranote.app.ui.viewmodel.DetailUiState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.transcriptSearchQuery,
            onValueChange = viewModel::setTranscriptSearchQuery,
            placeholder = { Text("Search in transcript...", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = NavyCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary) },
            trailingIcon = {
                if (uiState.transcriptSearchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setTranscriptSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextTertiary)
                    }
                }
            },
            singleLine = true
        )

        val filteredSegments = if (uiState.transcriptSearchQuery.isBlank()) {
            uiState.segments
        } else {
            uiState.segments.filter {
                it.text.contains(uiState.transcriptSearchQuery, ignoreCase = true)
            }
        }

        if (filteredSegments.isEmpty() && uiState.segments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No transcript yet", color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                    Text("Tap 'Transcribe with AI' to generate a transcript", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredSegments, key = { it.id }) { seg ->
                    TranscriptSegmentItem(
                        segment = seg,
                        searchQuery = uiState.transcriptSearchQuery
                    )
                }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

@Composable
private fun SummaryTab(
    viewModel: DetailViewModel,
    uiState: com.auranote.app.ui.viewmodel.DetailUiState
) {
    val gson = remember { Gson() }
    val listType = remember { object : TypeToken<List<String>>() {}.type }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.isGeneratingSummary) {
            item { LoadingCard("Generating AI summary...") }
        }

        if (uiState.summary == null && !uiState.isGeneratingSummary) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text("Generate AI Summary", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Get key points, action items, and decisions extracted automatically", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        GradientButton("Generate Summary", onClick = { viewModel.generateSummary() }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        uiState.summary?.let { summary ->
            if (summary.overview.isNotBlank()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader(title = "Overview")
                            Spacer(Modifier.height(8.dp))
                            Text(summary.overview, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 22.sp)
                        }
                    }
                }
            }

            val keyPoints = parseSummaryList(gson, listType, summary.keyPoints)
            if (keyPoints.isNotEmpty()) {
                item { BulletListCard(title = "Key Points", items = keyPoints, color = CyanAccent) }
            }

            val actionItems = parseSummaryList(gson, listType, summary.actionItems)
            if (actionItems.isNotEmpty()) {
                item { BulletListCard(title = "Action Items", items = actionItems, color = GreenAccent, isChecklist = true) }
            }

            val decisions = parseSummaryList(gson, listType, summary.decisions)
            if (decisions.isNotEmpty()) {
                item { BulletListCard(title = "Decisions Made", items = decisions, color = AmberAccent) }
            }

            val nextSteps = parseSummaryList(gson, listType, summary.nextSteps)
            if (nextSteps.isNotEmpty()) {
                item { BulletListCard(title = "Next Steps", items = nextSteps, color = PurplePrimary) }
            }

            item {
                TextButton(
                    onClick = { viewModel.generateSummary() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Regenerate Summary", color = PurplePrimary)
                }
            }
        }

        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun BulletListCard(
    title: String,
    items: List<String>,
    color: Color,
    isChecklist: Boolean = false,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(Modifier.height(10.dp))
            items.forEachIndexed { i, item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (isChecklist) {
                        Box(
                            modifier = Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(6.dp).clip(CircleShape).background(color.copy(alpha = 0.6f)).align(Alignment.CenterVertically)
                        )
                    }
                    Text(item, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                }
                if (i < items.lastIndex) Divider(color = NavyElevated, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun AIChatTab(recordingId: Long, chatViewModel: AIChatViewModel = hiltViewModel()) {
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recordingId) { chatViewModel.initWithRecording(recordingId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = false
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Suggested questions:", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                        suggestedQuestions.forEach { q ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().clickable { chatViewModel.sendSuggestedQuestion(q) }
                            ) {
                                Text(
                                    text = q,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
            items(uiState.messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }
            if (uiState.isLoading) {
                item { TypingIndicator() }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = chatViewModel::setInputText,
                placeholder = { Text("Ask about your recording...", color = TextTertiary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = NavyCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 4
            )
            FilledIconButton(
                onClick = { chatViewModel.sendMessage() },
                enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = PurplePrimary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(message: com.auranote.app.data.model.ChatMessage) {
    val isUser = message.role == com.auranote.app.data.model.MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(PurplePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 20.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 20.dp,
                        bottomStart = 20.dp, bottomEnd = 20.dp
                    )
                )
                .background(
                    if (isUser) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    else Brush.horizontalGradient(listOf(NavyCard, NavyCard))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .then(if (isUser) Modifier else Modifier.fillMaxWidth(0.88f))
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(PurplePrimary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
        }
        GlassCard {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PurplePrimary, strokeWidth = 2.dp)
                Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
    }
}

@Composable
private fun StudyTab(
    viewModel: DetailViewModel,
    uiState: com.auranote.app.ui.viewmodel.DetailUiState
) {
    var showQuiz by remember { mutableStateOf(false) }
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.isGeneratingStudy) {
            item { LoadingCard("Generating study materials...") }
        }

        if (uiState.flashcards.isEmpty() && uiState.quizQuestions.isEmpty() && !uiState.isGeneratingStudy) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(32.dp))
                        Text("Generate Study Materials", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Create flashcards and quizzes from your lecture or meeting content", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        GradientButton("Generate Flashcards & Quiz", onClick = { viewModel.generateStudyContent() }, modifier = Modifier.fillMaxWidth(), icon = Icons.Default.AutoAwesome)
                    }
                }
            }
        }

        if (uiState.flashcards.isNotEmpty()) {
            item { SectionHeader(title = "Flashcards (${uiState.flashcards.size})") }
            item {
                val card = uiState.flashcards.getOrNull(currentCardIndex)
                if (card != null) {
                    FlashcardView(
                        card = card,
                        cardIndex = currentCardIndex,
                        totalCards = uiState.flashcards.size,
                        isFlipped = isFlipped,
                        onFlip = { isFlipped = !isFlipped },
                        onPrev = { if (currentCardIndex > 0) { currentCardIndex--; isFlipped = false } },
                        onNext = { if (currentCardIndex < uiState.flashcards.lastIndex) { currentCardIndex++; isFlipped = false } }
                    )
                }
            }
        }

        if (uiState.quizQuestions.isNotEmpty()) {
            item { SectionHeader(title = "Quiz (${uiState.quizQuestions.size} questions)") }
            itemsIndexed(uiState.quizQuestions) { i, question ->
                QuizQuestionCard(question = question, questionNumber = i + 1)
            }
        }

        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun FlashcardView(
    card: Flashcard,
    cardIndex: Int,
    totalCards: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "cardFlip"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${cardIndex + 1} / $totalCards", style = MaterialTheme.typography.labelSmall, color = TextTertiary)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer { rotationY = rotation; cameraDistance = 12 * density }
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (rotation <= 90f) Brush.linearGradient(listOf(NavyCard, NavyElevated))
                    else Brush.linearGradient(listOf(PurplePrimary.copy(alpha = 0.3f), NavyCard))
                )
                .clickable { onFlip() },
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(20.dp)) {
                    TagChip("Question", color = CyanAccent)
                    Text(card.question, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(20.dp).graphicsLayer { rotationY = 180f }
                ) {
                    TagChip("Answer", color = GreenAccent)
                    Text(card.answer, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        Text("Tap to flip", style = MaterialTheme.typography.labelSmall, color = TextTertiary)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onPrev, enabled = cardIndex > 0) {
                Text("← Prev", color = if (cardIndex > 0) PurplePrimary else TextTertiary)
            }
            TextButton(onClick = onNext, enabled = cardIndex < totalCards - 1) {
                Text("Next →", color = if (cardIndex < totalCards - 1) PurplePrimary else TextTertiary)
            }
        }
    }
}

@Composable
private fun QuizQuestionCard(question: QuizQuestion, questionNumber: Int) {
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    val isAnswered = selectedAnswer >= 0

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Q$questionNumber: ${question.question}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
            question.options.forEachIndexed { i, option ->
                val isCorrect = i == question.correctAnswerIndex
                val isSelected = i == selectedAnswer
                val bgColor = when {
                    isAnswered && isCorrect -> GreenAccent.copy(alpha = 0.2f)
                    isAnswered && isSelected && !isCorrect -> RedAccent.copy(alpha = 0.2f)
                    isSelected -> PurplePrimary.copy(alpha = 0.15f)
                    else -> NavyElevated
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable(enabled = !isAnswered) { selectedAnswer = i }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(22.dp).clip(CircleShape).background(
                            if (isAnswered && isCorrect) GreenAccent else if (isAnswered && isSelected) RedAccent else NavyCard
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(('A' + i).toString(), style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Text(option, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                    if (isAnswered && isCorrect) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(16.dp))
                }
            }
            if (isAnswered && question.explanation.isNotBlank()) {
                Divider(color = NavyElevated)
                InfoBanner(message = question.explanation, tint = CyanAccent)
            }
        }
    }
}

private fun parseSummaryList(gson: Gson, type: java.lang.reflect.Type, json: String): List<String> {
    if (json.isBlank() || json == "null") return emptyList()
    return try {
        gson.fromJson(json, type) ?: emptyList()
    } catch (e: Exception) {
        if (json.startsWith("[")) emptyList() else listOf(json)
    }
}
