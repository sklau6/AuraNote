package com.auranote.app.di;

import com.auranote.app.data.db.RecordingDao;
import com.auranote.app.data.db.TranscriptDao;
import com.auranote.app.data.repository.RecordingRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideRecordingRepositoryFactory implements Factory<RecordingRepository> {
  private final Provider<RecordingDao> recordingDaoProvider;

  private final Provider<TranscriptDao> transcriptDaoProvider;

  public AppModule_ProvideRecordingRepositoryFactory(Provider<RecordingDao> recordingDaoProvider,
      Provider<TranscriptDao> transcriptDaoProvider) {
    this.recordingDaoProvider = recordingDaoProvider;
    this.transcriptDaoProvider = transcriptDaoProvider;
  }

  @Override
  public RecordingRepository get() {
    return provideRecordingRepository(recordingDaoProvider.get(), transcriptDaoProvider.get());
  }

  public static AppModule_ProvideRecordingRepositoryFactory create(
      Provider<RecordingDao> recordingDaoProvider, Provider<TranscriptDao> transcriptDaoProvider) {
    return new AppModule_ProvideRecordingRepositoryFactory(recordingDaoProvider, transcriptDaoProvider);
  }

  public static RecordingRepository provideRecordingRepository(RecordingDao recordingDao,
      TranscriptDao transcriptDao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRecordingRepository(recordingDao, transcriptDao));
  }
}
