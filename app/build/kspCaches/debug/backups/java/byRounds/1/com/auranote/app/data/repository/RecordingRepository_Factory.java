package com.auranote.app.data.repository;

import com.auranote.app.data.db.RecordingDao;
import com.auranote.app.data.db.TranscriptDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class RecordingRepository_Factory implements Factory<RecordingRepository> {
  private final Provider<RecordingDao> recordingDaoProvider;

  private final Provider<TranscriptDao> transcriptDaoProvider;

  public RecordingRepository_Factory(Provider<RecordingDao> recordingDaoProvider,
      Provider<TranscriptDao> transcriptDaoProvider) {
    this.recordingDaoProvider = recordingDaoProvider;
    this.transcriptDaoProvider = transcriptDaoProvider;
  }

  @Override
  public RecordingRepository get() {
    return newInstance(recordingDaoProvider.get(), transcriptDaoProvider.get());
  }

  public static RecordingRepository_Factory create(Provider<RecordingDao> recordingDaoProvider,
      Provider<TranscriptDao> transcriptDaoProvider) {
    return new RecordingRepository_Factory(recordingDaoProvider, transcriptDaoProvider);
  }

  public static RecordingRepository newInstance(RecordingDao recordingDao,
      TranscriptDao transcriptDao) {
    return new RecordingRepository(recordingDao, transcriptDao);
  }
}
