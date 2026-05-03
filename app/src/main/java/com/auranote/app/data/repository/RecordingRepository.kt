package com.auranote.app.data.repository

import com.auranote.app.data.db.RecordingDao
import com.auranote.app.data.db.TranscriptDao
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.RecordingType
import com.auranote.app.data.model.TranscriptSegment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val recordingDao: RecordingDao,
    private val transcriptDao: TranscriptDao
) {

    fun getAllRecordings(): Flow<List<Recording>> = recordingDao.getAllRecordings()

    fun getRecordingsByType(type: RecordingType): Flow<List<Recording>> =
        recordingDao.getRecordingsByType(type)

    fun getFavoriteRecordings(): Flow<List<Recording>> =
        recordingDao.getFavoriteRecordings()

    fun searchRecordings(query: String): Flow<List<Recording>> =
        recordingDao.searchRecordings(query)

    suspend fun getRecordingById(id: Long): Recording? =
        recordingDao.getRecordingById(id)

    suspend fun insertRecording(recording: Recording): Long =
        recordingDao.insertRecording(recording)

    suspend fun updateRecording(recording: Recording) =
        recordingDao.updateRecording(recording)

    suspend fun deleteRecording(recording: Recording) =
        recordingDao.deleteRecording(recording)

    suspend fun deleteRecordingById(id: Long) =
        recordingDao.deleteRecordingById(id)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) =
        recordingDao.setFavorite(id, isFavorite)

    suspend fun updateTitle(id: Long, title: String) =
        recordingDao.updateTitle(id, title)

    suspend fun updateTranscriptionStatus(id: Long, status: String) =
        recordingDao.updateTranscriptionStatus(id, status)

    fun getTranscriptSegments(recordingId: Long): Flow<List<TranscriptSegment>> =
        transcriptDao.getSegmentsByRecordingId(recordingId)

    suspend fun getTranscriptSegmentsSync(recordingId: Long): List<TranscriptSegment> =
        transcriptDao.getSegmentsSync(recordingId)

    suspend fun insertTranscriptSegments(segments: List<TranscriptSegment>) =
        transcriptDao.insertSegments(segments)

    suspend fun updateTranscriptSegment(segment: TranscriptSegment) =
        transcriptDao.insertSegments(listOf(segment))

    suspend fun deleteTranscriptSegments(recordingId: Long) =
        transcriptDao.deleteSegmentsByRecordingId(recordingId)

    suspend fun getFullTranscriptText(recordingId: Long): String? =
        transcriptDao.getFullTranscriptText(recordingId)

    suspend fun searchInTranscript(recordingId: Long, query: String): List<TranscriptSegment> =
        transcriptDao.searchInTranscript(recordingId, query)
}
