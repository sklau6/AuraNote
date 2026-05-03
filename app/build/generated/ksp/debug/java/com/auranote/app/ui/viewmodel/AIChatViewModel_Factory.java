package com.auranote.app.ui.viewmodel;

import com.auranote.app.data.preferences.AppPreferences;
import com.auranote.app.data.repository.AIRepository;
import com.auranote.app.data.repository.GeminiRepository;
import com.auranote.app.data.repository.OnDeviceAIRepository;
import com.auranote.app.data.repository.RecordingRepository;
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
public final class AIChatViewModel_Factory implements Factory<AIChatViewModel> {
  private final Provider<AIRepository> aiRepositoryProvider;

  private final Provider<GeminiRepository> geminiRepositoryProvider;

  private final Provider<OnDeviceAIRepository> onDeviceAIProvider;

  private final Provider<RecordingRepository> recordingRepositoryProvider;

  private final Provider<AppPreferences> preferencesProvider;

  public AIChatViewModel_Factory(Provider<AIRepository> aiRepositoryProvider,
      Provider<GeminiRepository> geminiRepositoryProvider,
      Provider<OnDeviceAIRepository> onDeviceAIProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<AppPreferences> preferencesProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
    this.geminiRepositoryProvider = geminiRepositoryProvider;
    this.onDeviceAIProvider = onDeviceAIProvider;
    this.recordingRepositoryProvider = recordingRepositoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public AIChatViewModel get() {
    return newInstance(aiRepositoryProvider.get(), geminiRepositoryProvider.get(), onDeviceAIProvider.get(), recordingRepositoryProvider.get(), preferencesProvider.get());
  }

  public static AIChatViewModel_Factory create(Provider<AIRepository> aiRepositoryProvider,
      Provider<GeminiRepository> geminiRepositoryProvider,
      Provider<OnDeviceAIRepository> onDeviceAIProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<AppPreferences> preferencesProvider) {
    return new AIChatViewModel_Factory(aiRepositoryProvider, geminiRepositoryProvider, onDeviceAIProvider, recordingRepositoryProvider, preferencesProvider);
  }

  public static AIChatViewModel newInstance(AIRepository aiRepository,
      GeminiRepository geminiRepository, OnDeviceAIRepository onDeviceAI,
      RecordingRepository recordingRepository, AppPreferences preferences) {
    return new AIChatViewModel(aiRepository, geminiRepository, onDeviceAI, recordingRepository, preferences);
  }
}
