package com.auranote.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.auranote.app.data.model.AISummary
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.TranscriptSegment

@Database(
    entities = [Recording::class, TranscriptSegment::class, AISummary::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun summaryDao(): SummaryDao

    companion object {
        const val DATABASE_NAME = "auranote_db"
    }
}
