package com.auranote.app.service;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class RecordingService_MembersInjector implements MembersInjector<RecordingService> {
  private final Provider<AudioRecorderManager> recorderManagerProvider;

  private final Provider<AudioFocusManager> audioFocusManagerProvider;

  public RecordingService_MembersInjector(Provider<AudioRecorderManager> recorderManagerProvider,
      Provider<AudioFocusManager> audioFocusManagerProvider) {
    this.recorderManagerProvider = recorderManagerProvider;
    this.audioFocusManagerProvider = audioFocusManagerProvider;
  }

  public static MembersInjector<RecordingService> create(
      Provider<AudioRecorderManager> recorderManagerProvider,
      Provider<AudioFocusManager> audioFocusManagerProvider) {
    return new RecordingService_MembersInjector(recorderManagerProvider, audioFocusManagerProvider);
  }

  @Override
  public void injectMembers(RecordingService instance) {
    injectRecorderManager(instance, recorderManagerProvider.get());
    injectAudioFocusManager(instance, audioFocusManagerProvider.get());
  }

  @InjectedFieldSignature("com.auranote.app.service.RecordingService.recorderManager")
  public static void injectRecorderManager(RecordingService instance,
      AudioRecorderManager recorderManager) {
    instance.recorderManager = recorderManager;
  }

  @InjectedFieldSignature("com.auranote.app.service.RecordingService.audioFocusManager")
  public static void injectAudioFocusManager(RecordingService instance,
      AudioFocusManager audioFocusManager) {
    instance.audioFocusManager = audioFocusManager;
  }
}
