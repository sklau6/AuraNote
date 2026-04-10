package com.auranote.app.data.repository;

import com.auranote.app.data.api.OpenAIService;
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
public final class AIRepository_Factory implements Factory<AIRepository> {
  private final Provider<OpenAIService> openAIServiceProvider;

  private final Provider<SummaryDao> summaryDaoProvider;

  private final Provider<Gson> gsonProvider;

  public AIRepository_Factory(Provider<OpenAIService> openAIServiceProvider,
      Provider<SummaryDao> summaryDaoProvider, Provider<Gson> gsonProvider) {
    this.openAIServiceProvider = openAIServiceProvider;
    this.summaryDaoProvider = summaryDaoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public AIRepository get() {
    return newInstance(openAIServiceProvider.get(), summaryDaoProvider.get(), gsonProvider.get());
  }

  public static AIRepository_Factory create(Provider<OpenAIService> openAIServiceProvider,
      Provider<SummaryDao> summaryDaoProvider, Provider<Gson> gsonProvider) {
    return new AIRepository_Factory(openAIServiceProvider, summaryDaoProvider, gsonProvider);
  }

  public static AIRepository newInstance(OpenAIService openAIService, SummaryDao summaryDao,
      Gson gson) {
    return new AIRepository(openAIService, summaryDao, gson);
  }
}
