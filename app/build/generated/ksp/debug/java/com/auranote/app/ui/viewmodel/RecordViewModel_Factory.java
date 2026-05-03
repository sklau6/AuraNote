package com.auranote.app.ui.viewmodel;

import android.content.Context;
import com.auranote.app.data.preferences.AppPreferences;
import com.auranote.app.data.repository.RecordingRepository;
import com.auranote.app.service.AudioRecorderManager;
import com.auranote.app.service.LiveTranscriptionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
  private final Provider<Context> appContextProvider;

  private final Provider<AudioRecorderManager> recorderManagerProvider;

  private final Provider<LiveTranscriptionManager> liveTranscriptionProvider;

  private final Provider<RecordingRepository> repositoryProvider;

  private final Provider<AppPreferences> preferencesProvider;

  public RecordViewModel_Factory(Provider<Context> appContextProvider,
      Provider<AudioRecorderManager> recorderManagerProvider,
      Provider<LiveTranscriptionManager> liveTranscriptionProvider,
      Provider<RecordingRepository> repositoryProvider,
      Provider<AppPreferences> preferencesProvider) {
    this.appContextProvider = appContextProvider;
    this.recorderManagerProvider = recorderManagerProvider;
    this.liveTranscriptionProvider = liveTranscriptionProvider;
    this.repositoryProvider = repositoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public RecordViewModel get() {
    return newInstance(appContextProvider.get(), recorderManagerProvider.get(), liveTranscriptionProvider.get(), repositoryProvider.get(), preferencesProvider.get());
  }

  public static RecordViewModel_Factory create(Provider<Context> appContextProvider,
      Provider<AudioRecorderManager> recorderManagerProvider,
      Provider<LiveTranscriptionManager> liveTranscriptionProvider,
      Provider<RecordingRepository> repositoryProvider,
      Provider<AppPreferences> preferencesProvider) {
    return new RecordViewModel_Factory(appContextProvider, recorderManagerProvider, liveTranscriptionProvider, repositoryProvider, preferencesProvider);
  }

  public static RecordViewModel newInstance(Context appContext,
      AudioRecorderManager recorderManager, LiveTranscriptionManager liveTranscription,
      RecordingRepository repository, AppPreferences preferences) {
    return new RecordViewModel(appContext, recorderManager, liveTranscription, repository, preferences);
  }
}
