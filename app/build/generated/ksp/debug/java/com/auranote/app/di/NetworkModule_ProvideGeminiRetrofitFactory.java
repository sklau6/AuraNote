package com.auranote.app.di;

import com.google.gson.Gson;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.auranote.app.di.GeminiRetrofit")
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
public final class NetworkModule_ProvideGeminiRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<Gson> gsonProvider;

  public NetworkModule_ProvideGeminiRetrofitFactory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<Gson> gsonProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public Retrofit get() {
    return provideGeminiRetrofit(okHttpClientProvider.get(), gsonProvider.get());
  }

  public static NetworkModule_ProvideGeminiRetrofitFactory create(
      Provider<OkHttpClient> okHttpClientProvider, Provider<Gson> gsonProvider) {
    return new NetworkModule_ProvideGeminiRetrofitFactory(okHttpClientProvider, gsonProvider);
  }

  public static Retrofit provideGeminiRetrofit(OkHttpClient okHttpClient, Gson gson) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideGeminiRetrofit(okHttpClient, gson));
  }
}
