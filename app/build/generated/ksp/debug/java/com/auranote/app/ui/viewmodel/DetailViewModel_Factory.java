package com.auranote.app.ui.viewmodel;

import android.content.Context;
import com.auranote.app.data.preferences.AppPreferences;
import com.auranote.app.data.repository.AIRepository;
import com.auranote.app.data.repository.GeminiRepository;
import com.auranote.app.data.repository.OnDeviceAIRepository;
import com.auranote.app.data.repository.RecordingRepository;
import com.google.gson.Gson;
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
public final class DetailViewModel_Factory implements Factory<DetailViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<RecordingRepository> recordingRepositoryProvider;

  private final Provider<AIRepository> aiRepositoryProvider;

  private final Provider<GeminiRepository> geminiRepositoryProvider;

  private final Provider<OnDeviceAIRepository> onDeviceAIProvider;

  private final Provider<AppPreferences> preferencesProvider;

  private final Provider<Gson> gsonProvider;

  public DetailViewModel_Factory(Provider<Context> contextProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<AIRepository> aiRepositoryProvider,
      Provider<GeminiRepository> geminiRepositoryProvider,
      Provider<OnDeviceAIRepository> onDeviceAIProvider,
      Provider<AppPreferences> preferencesProvider, Provider<Gson> gsonProvider) {
    this.contextProvider = contextProvider;
    this.recordingRepositoryProvider = recordingRepositoryProvider;
    this.aiRepositoryProvider = aiRepositoryProvider;
    this.geminiRepositoryProvider = geminiRepositoryProvider;
    this.onDeviceAIProvider = onDeviceAIProvider;
    this.preferencesProvider = preferencesProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public DetailViewModel get() {
    return newInstance(contextProvider.get(), recordingRepositoryProvider.get(), aiRepositoryProvider.get(), geminiRepositoryProvider.get(), onDeviceAIProvider.get(), preferencesProvider.get(), gsonProvider.get());
  }

  public static DetailViewModel_Factory create(Provider<Context> contextProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<AIRepository> aiRepositoryProvider,
      Provider<GeminiRepository> geminiRepositoryProvider,
      Provider<OnDeviceAIRepository> onDeviceAIProvider,
      Provider<AppPreferences> preferencesProvider, Provider<Gson> gsonProvider) {
    return new DetailViewModel_Factory(contextProvider, recordingRepositoryProvider, aiRepositoryProvider, geminiRepositoryProvider, onDeviceAIProvider, preferencesProvider, gsonProvider);
  }

  public static DetailViewModel newInstance(Context context,
      RecordingRepository recordingRepository, AIRepository aiRepository,
      GeminiRepository geminiRepository, OnDeviceAIRepository onDeviceAI,
      AppPreferences preferences, Gson gson) {
    return new DetailViewModel(context, recordingRepository, aiRepository, geminiRepository, onDeviceAI, preferences, gson);
  }
}
