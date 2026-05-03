package com.auranote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auranote.app.data.model.TranscriptSegment
import com.auranote.app.ui.theme.AmberAccent
import com.auranote.app.ui.theme.CyanAccent
import com.auranote.app.ui.theme.GreenAccent
import com.auranote.app.ui.theme.NavyElevated
import com.auranote.app.ui.theme.PinkAccent
import com.auranote.app.ui.theme.PurplePrimary
import com.auranote.app.ui.theme.Speaker1Color
import com.auranote.app.ui.theme.Speaker2Color
import com.auranote.app.ui.theme.Speaker3Color
import com.auranote.app.ui.theme.Speaker4Color
import com.auranote.app.ui.theme.Speaker5Color
import com.auranote.app.ui.theme.TextPrimary
import com.auranote.app.ui.theme.TextSecondary
import com.auranote.app.ui.theme.TextTertiary
import com.auranote.app.util.TimeUtils

@Composable
fun TranscriptSegmentItem(
    segment: TranscriptSegment,
    searchQuery: String = "",
    isActive: Boolean = false,
    onSeekTo: (Float) -> Unit = {},
    onEdit: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val speakerColor = speakerColor(segment.speakerLabel)
    var isEditing by remember { mutableStateOf(false) }
    var editedText by remember(segment.id) { mutableStateOf(TextFieldValue(segment.text)) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) speakerColor.copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onSeekTo(segment.startTimeSeconds) }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(speakerColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = speakerInitial(segment.speakerLabel),
                    color = speakerColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(speakerColor.copy(alpha = 0.2f))
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = segment.speakerLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = speakerColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = TimeUtils.formatDurationSeconds(segment.startTimeSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            Spacer(Modifier.height(4.dp))
            if (isEditing && onEdit != null) {
                BasicTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    isEditing = false
                                    if (editedText.text != segment.text) {
                                        onEdit(editedText.text)
                                    }
                                }
                            )
                        },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(PurplePrimary),
                    singleLine = false
                )
            } else {
                Text(
                    text = highlightText(segment.text, searchQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 22.sp,
                    modifier = if (onEdit != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { isEditing = true }
                            )
                        }
                    } else Modifier
                )
            }
        }
    }
}

private fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var lastIndex = 0
        var index = lowerText.indexOf(lowerQuery)
        while (index >= 0) {
            append(text.substring(lastIndex, index))
            withStyle(SpanStyle(background = AmberAccent.copy(alpha = 0.3f), color = AmberAccent)) {
                append(text.substring(index, index + query.length))
            }
            lastIndex = index + query.length
            index = lowerText.indexOf(lowerQuery, lastIndex)
        }
        append(text.substring(lastIndex))
    }
}

fun speakerColor(label: String): Color {
    val num = label.filter { it.isDigit() }.toIntOrNull() ?: 1
    return when ((num - 1) % 5) {
        0 -> Speaker1Color
        1 -> Speaker2Color
        2 -> Speaker3Color
        3 -> Speaker4Color
        else -> Speaker5Color
    }
}

private fun speakerInitial(label: String): String {
    val num = label.filter { it.isDigit() }
    return if (num.isNotEmpty()) num else label.firstOrNull()?.uppercase() ?: "S"
}
