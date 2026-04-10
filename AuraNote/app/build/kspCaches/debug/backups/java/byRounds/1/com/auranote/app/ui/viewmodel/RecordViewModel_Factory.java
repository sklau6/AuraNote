package com.auranote.app.ui.viewmodel;

import com.auranote.app.data.preferences.AppPreferences;
import com.auranote.app.data.repository.RecordingRepository;
import com.auranote.app.service.AudioRecorderManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class RecordViewModel_Factory implements Factory<RecordViewModel> {
  private final Provider<AudioRecorderManager> recorderManagerProvider;

  private final Provider<RecordingRepository> repositoryProvider;

  private final Provider<AppPreferences> preferencesProvider;

  public RecordViewModel_Factory(Provider<AudioRecorderManager> recorderManagerProvider,
      Provider<RecordingRepository> repositoryProvider,
      Provider<AppPreferences> preferencesProvider) {
    this.recorderManagerProvider = recorderManagerProvider;
    this.repositoryProvider = repositoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public RecordViewModel get() {
    return newInstance(recorderManagerProvider.get(), repositoryProvider.get(), preferencesProvider.get());
  }

  public static RecordViewModel_Factory create(
      Provider<AudioRecorderManager> recorderManagerProvider,
      Provider<RecordingRepository> repositoryProvider,
      Provider<AppPreferences> preferencesProvider) {
    return new RecordViewModel_Factory(recorderManagerProvider, repositoryProvider, preferencesProvider);
  }

  public static RecordViewModel newInstance(AudioRecorderManager recorderManager,
      RecordingRepository repository, AppPreferences preferences) {
    return new RecordViewModel(recorderManager, repository, preferences);
  }
}
