package com.auranote.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.auranote.app.ui.theme.GradientEnd
import com.auranote.app.ui.theme.GradientStart
import com.auranote.app.ui.theme.WaveformInactive
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = GradientStart,
    inactiveColor: Color = WaveformInactive,
    barWidthDp: Dp = 3.dp,
    barSpacingDp: Dp = 2.dp,
    isRecording: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        val barWidth = barWidthDp.toPx()
        val barSpacing = barSpacingDp.toPx()
        val totalBarWidth = barWidth + barSpacing
        val numBars = (canvasWidth / totalBarWidth).toInt().coerceAtLeast(1)

        for (i in 0 until numBars) {
            val x = i * totalBarWidth
            val ampIndex = if (amplitudes.isNotEmpty()) {
                (i * amplitudes.size / numBars).coerceIn(0, amplitudes.size - 1)
            } else -1

            val amplitude = when {
                amplitudes.isNotEmpty() && ampIndex >= 0 -> amplitudes[ampIndex]
                isRecording -> {
                    val wave1 = sin((animOffset + i * 0.3f).toDouble()).toFloat()
                    val wave2 = sin((animOffset * 1.5f + i * 0.5f).toDouble()).toFloat()
                    ((wave1 + wave2) / 2f + 1f) / 2f * 0.6f + 0.1f
                }
                else -> 0.08f
            }

            val barHeight = (amplitude * canvasHeight * 0.9f).coerceIn(4f, canvasHeight * 0.9f)
            val top = centerY - barHeight / 2f
            val color = if (amplitude > 0.1f) barColor else inactiveColor

            drawRoundRect(
                color = color,
                topLeft = Offset(x = x, y = top),
                size = Size(width = barWidth, height = barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun StaticWaveformThumbnail(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    activeColor: Color = GradientStart,
    inactiveColor: Color = WaveformInactive,
    progress: Float = 0f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        val barWidth = 2.dp.toPx()
        val barSpacing = 1.5.dp.toPx()
        val totalBarWidth = barWidth + barSpacing
        val numBars = (canvasWidth / totalBarWidth).toInt().coerceAtLeast(1)

        for (i in 0 until numBars) {
            val x = i * totalBarWidth
            val ampIndex = if (amplitudes.isNotEmpty()) {
                (i * amplitudes.size / numBars).coerceIn(0, amplitudes.size - 1)
            } else -1

            val amplitude = if (amplitudes.isNotEmpty() && ampIndex >= 0) {
                amplitudes[ampIndex]
            } else {
                (sin(i * 0.4).toFloat() * 0.3f + 0.4f)
            }

            val barHeight = (amplitude * canvasHeight * 0.85f).coerceIn(3f, canvasHeight * 0.85f)
            val top = centerY - barHeight / 2f
            val isPlayed = (i.toFloat() / numBars) <= progress
            val color = if (isPlayed) activeColor else inactiveColor

            drawRoundRect(
                color = color,
                topLeft = Offset(x = x, y = top),
                size = Size(width = barWidth, height = barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
