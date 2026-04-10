package com.auranote.app.data.repository;

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
public final class OnDeviceAIRepository_Factory implements Factory<OnDeviceAIRepository> {
  private final Provider<Context> contextProvider;

  public OnDeviceAIRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OnDeviceAIRepository get() {
    return newInstance(contextProvider.get());
  }

  public static OnDeviceAIRepository_Factory create(Provider<Context> contextProvider) {
    return new OnDeviceAIRepository_Factory(contextProvider);
  }

  public static OnDeviceAIRepository newInstance(Context context) {
    return new OnDeviceAIRepository(context);
  }
}
