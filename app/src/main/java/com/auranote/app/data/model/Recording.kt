package com.auranote.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class Recording(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val transcriptionStatus: TranscriptionStatus = TranscriptionStatus.PENDING,
    val language: String = "auto",
    val type: RecordingType = RecordingType.MEETING,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val speakerCount: Int = 0
)

enum class TranscriptionStatus {
    PENDING, IN_PROGRESS, COMPLETED, ERROR
}

enum class RecordingType(val label: String) {
    MEETING("Meeting"),
    LECTURE("Lecture"),
    INTERVIEW("Interview"),
    CALL("Call"),
    PERSONAL("Personal"),
    OTHER("Other")
}
