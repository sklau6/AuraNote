package com.auranote.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.auranote.app.data.model.TranscriptSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {

    @Query("SELECT * FROM transcript_segments WHERE recordingId = :recordingId ORDER BY segmentIndex ASC")
    fun getSegmentsByRecordingId(recordingId: Long): Flow<List<TranscriptSegment>>

    @Query("SELECT * FROM transcript_segments WHERE recordingId = :recordingId ORDER BY segmentIndex ASC")
    suspend fun getSegmentsSync(recordingId: Long): List<TranscriptSegment>

    @Query("SELECT * FROM transcript_segments WHERE recordingId = :recordingId AND text LIKE '%' || :query || '%' ORDER BY segmentIndex ASC")
    suspend fun searchInTranscript(recordingId: Long, query: String): List<TranscriptSegment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TranscriptSegment>)

    @Query("DELETE FROM transcript_segments WHERE recordingId = :recordingId")
    suspend fun deleteSegmentsByRecordingId(recordingId: Long)

    @Query("SELECT GROUP_CONCAT(text, ' ') FROM transcript_segments WHERE recordingId = :recordingId ORDER BY segmentIndex ASC")
    suspend fun getFullTranscriptText(recordingId: Long): String?
}
