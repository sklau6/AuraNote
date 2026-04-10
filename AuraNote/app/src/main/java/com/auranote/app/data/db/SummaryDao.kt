package com.auranote.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.auranote.app.data.model.AISummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    @Query("SELECT * FROM ai_summaries WHERE recordingId = :recordingId LIMIT 1")
    fun getSummaryByRecordingId(recordingId: Long): Flow<AISummary?>

    @Query("SELECT * FROM ai_summaries WHERE recordingId = :recordingId LIMIT 1")
    suspend fun getSummarySync(recordingId: Long): AISummary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: AISummary): Long

    @Update
    suspend fun updateSummary(summary: AISummary)

    @Query("DELETE FROM ai_summaries WHERE recordingId = :recordingId")
    suspend fun deleteSummaryByRecordingId(recordingId: Long)
}
