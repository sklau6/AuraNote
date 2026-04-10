package com.auranote.app.di;

import com.auranote.app.data.db.AppDatabase;
import com.auranote.app.data.db.TranscriptDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvideTranscriptDaoFactory implements Factory<TranscriptDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideTranscriptDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TranscriptDao get() {
    return provideTranscriptDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideTranscriptDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideTranscriptDaoFactory(dbProvider);
  }

  public static TranscriptDao provideTranscriptDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTranscriptDao(db));
  }
}
