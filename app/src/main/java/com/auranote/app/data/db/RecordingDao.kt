package com.auranote.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.RecordingType
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<Recording>>

    @Query("SELECT * FROM recordings WHERE type = :type ORDER BY createdAt DESC")
    fun getRecordingsByType(type: RecordingType): Flow<List<Recording>>

    @Query("SELECT * FROM recordings WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRecordings(): Flow<List<Recording>>

    @Query("SELECT * FROM recordings WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRecordings(query: String): Flow<List<Recording>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): Recording?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording): Long

    @Update
    suspend fun updateRecording(recording: Recording)

    @Delete
    suspend fun deleteRecording(recording: Recording)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Long)

    @Query("UPDATE recordings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE recordings SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query("UPDATE recordings SET transcriptionStatus = :status WHERE id = :id")
    suspend fun updateTranscriptionStatus(id: Long, status: String)

    @Query("UPDATE recordings SET speakerCount = :count WHERE id = :id")
    suspend fun updateSpeakerCount(id: Long, count: Int)

    @Query("SELECT COUNT(*) FROM recordings")
    suspend fun getRecordingCount(): Int
}
