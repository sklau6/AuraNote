package com.auranote.app.di

import com.auranote.app.data.repository.AIRepository
import com.auranote.app.data.repository.RecordingRepository
import com.auranote.app.data.db.RecordingDao
import com.auranote.app.data.db.SummaryDao
import com.auranote.app.data.db.TranscriptDao
import com.auranote.app.data.api.OpenAIService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRecordingRepository(
        recordingDao: RecordingDao,
        transcriptDao: TranscriptDao
    ): RecordingRepository = RecordingRepository(recordingDao, transcriptDao)

    @Provides
    @Singleton
    fun provideAIRepository(
        openAIService: OpenAIService,
        summaryDao: SummaryDao,
        gson: Gson
    ): AIRepository = AIRepository(openAIService, summaryDao, gson)
}
