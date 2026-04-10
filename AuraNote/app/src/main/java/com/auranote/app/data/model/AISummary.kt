package com.auranote.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "ai_summaries",
    foreignKeys = [
        ForeignKey(
            entity = Recording::class,
            parentColumns = ["id"],
            childColumns = ["recordingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordingId")]
)
data class AISummary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordingId: Long,
    val overview: String = "",
    val keyPoints: String = "",
    val actionItems: String = "",
    val decisions: String = "",
    val nextSteps: String = "",
    val studyGuide: String = "",
    val flashcardsJson: String = "",
    val quizJson: String = "",
    val generatedAt: Long = System.currentTimeMillis()
)

data class Flashcard(
    val question: String,
    val answer: String
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    @SerializedName("correct_index") val correctAnswerIndex: Int = 0,
    val explanation: String = ""
)
