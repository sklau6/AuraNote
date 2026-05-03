package com.auranote.app.data.repository;

import com.auranote.app.data.api.GeminiService;
import com.auranote.app.data.db.SummaryDao;
import com.google.gson.Gson;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GeminiRepository_Factory implements Factory<GeminiRepository> {
  private final Provider<GeminiService> geminiServiceProvider;

  private final Provider<SummaryDao> summaryDaoProvider;

  private final Provider<Gson> gsonProvider;

  public GeminiRepository_Factory(Provider<GeminiService> geminiServiceProvider,
      Provider<SummaryDao> summaryDaoProvider, Provider<Gson> gsonProvider) {
    this.geminiServiceProvider = geminiServiceProvider;
    this.summaryDaoProvider = summaryDaoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public GeminiRepository get() {
    return newInstance(geminiServiceProvider.get(), summaryDaoProvider.get(), gsonProvider.get());
  }

  public static GeminiRepository_Factory create(Provider<GeminiService> geminiServiceProvider,
      Provider<SummaryDao> summaryDaoProvider, Provider<Gson> gsonProvider) {
    return new GeminiRepository_Factory(geminiServiceProvider, summaryDaoProvider, gsonProvider);
  }

  public static GeminiRepository newInstance(GeminiService geminiService, SummaryDao summaryDao,
      Gson gson) {
    return new GeminiRepository(geminiService, summaryDao, gson);
  }
}
