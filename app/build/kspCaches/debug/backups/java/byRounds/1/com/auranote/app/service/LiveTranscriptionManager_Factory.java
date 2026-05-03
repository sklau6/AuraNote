package com.auranote.app.service;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class LiveTranscriptionManager_Factory implements Factory<LiveTranscriptionManager> {
  private final Provider<Context> contextProvider;

  public LiveTranscriptionManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LiveTranscriptionManager get() {
    return newInstance(contextProvider.get());
  }

  public static LiveTranscriptionManager_Factory create(Provider<Context> contextProvider) {
    return new LiveTranscriptionManager_Factory(contextProvider);
  }

  public static LiveTranscriptionManager newInstance(Context context) {
    return new LiveTranscriptionManager(context);
  }
}
