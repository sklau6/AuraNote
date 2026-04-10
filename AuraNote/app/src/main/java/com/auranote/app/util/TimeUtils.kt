package com.auranote.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    fun formatDuration(durationMs: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDurationSeconds(seconds: Float): String {
        return formatDuration((seconds * 1000).toLong())
    }

    fun formatDate(timestampMs: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    fun formatDateTime(timestampMs: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    fun formatRelativeTime(timestampMs: Long): String {
        val nowMs = System.currentTimeMillis()
        val diffMs = nowMs - timestampMs
        return when {
            diffMs < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diffMs < TimeUnit.HOURS.toMillis(1) -> {
                val m = TimeUnit.MILLISECONDS.toMinutes(diffMs)
                "$m min ago"
            }
            diffMs < TimeUnit.DAYS.toMillis(1) -> {
                val h = TimeUnit.MILLISECONDS.toHours(diffMs)
                "$h hr ago"
            }
            diffMs < TimeUnit.DAYS.toMillis(7) -> {
                val d = TimeUnit.MILLISECONDS.toDays(diffMs)
                if (d == 1L) "Yesterday" else "$d days ago"
            }
            else -> formatDate(timestampMs)
        }
    }

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024f)
        else -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
    }
}
