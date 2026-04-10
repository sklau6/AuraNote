package com.auranote.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auranote.app.ui.components.GradientButton
import com.auranote.app.ui.viewmodel.SettingsViewModel
import com.auranote.app.ui.theme.CyanAccent
import com.auranote.app.ui.theme.DeepNavy
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientMid
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.GreenAccent
import com.auranote.app.ui.theme.NavyCard
import com.auranote.app.ui.theme.PinkAccent
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val gradientColors: List<Color>
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Mic,
        iconColor = GradientStart,
        title = "Record & Transcribe",
        subtitle = "Capture every word with AI-powered real-time transcription in 100+ languages. Never miss important details again.",
        gradientColors = listOf(GradientStart.copy(alpha = 0.3f), GradientEnd.copy(alpha = 0.1f))
    ),
    OnboardingPage(
        icon = Icons.Default.Summarize,
        iconColor = CyanAccent,
        title = "AI Meeting Notes",
        subtitle = "Instantly generate summaries, key points, action items, and decisions from any recording with GPT-4 AI.",
        gradientColors = listOf(CyanAccent.copy(alpha = 0.3f), GradientMid.copy(alpha = 0.1f))
    ),
    OnboardingPage(
        icon = Icons.Default.Chat,
        iconColor = PinkAccent,
        title = "Ask Your Notes",
        subtitle = "Chat with your recordings. Ask \"What stats did Sarah mention?\" or \"What were the next steps?\" and get instant answers.",
        gradientColors = listOf(PinkAccent.copy(alpha = 0.3f), GradientStart.copy(alpha = 0.1f))
    ),
    OnboardingPage(
        icon = Icons.Default.School,
        iconColor = GreenAccent,
        title = "Study Smarter",
        subtitle = "Auto-generate flashcards, quizzes, and study guides from lecture recordings. Perfect for students.",
        gradientColors = listOf(GreenAccent.copy(alpha = 0.3f), CyanAccent.copy(alpha = 0.1f))
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        val page = pages.getOrNull(pagerState.currentPage) ?: pages.first()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = page.gradientColors,
                        radius = 800f
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "AuraNote",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.weight(1f))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    val p = pages[pageIndex]
                    PageContent(page = p)
                }

                Spacer(Modifier.height(40.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateFloatAsState(
                            targetValue = if (isSelected) 28f else 8f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                    else Brush.linearGradient(listOf(
                                        PurplePrimary.copy(alpha = 0.25f),
                                        PurplePrimary.copy(alpha = 0.25f)
                                    ))
                                )
                                .size(width = width.dp, height = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                GradientButton(
                    text = if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Next",
                    onClick = {
                        if (pagerState.currentPage == pages.lastIndex) {
                            viewModel.completeOnboarding()
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                if (pagerState.currentPage < pages.lastIndex) {
                    androidx.compose.material3.TextButton(onClick = {
                        viewModel.completeOnboarding()
                        onFinish()
                    }) {
                        Text(
                            text = "Skip",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    val inf = rememberInfiniteTransition(label = "iconGlow")
    val glowScale by inf.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowScale"
    )
    val glowAlpha by inf.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Icon with animated glow
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size((160 * glowScale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                page.iconColor.copy(alpha = glowAlpha * 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Inner icon container
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                page.iconColor.copy(alpha = 0.28f),
                                page.iconColor.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.iconColor,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}
