package com.auranote.app.di;

import com.auranote.app.data.api.OpenAIService;
import com.auranote.app.data.db.SummaryDao;
import com.auranote.app.data.repository.AIRepository;
import com.google.gson.Gson;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideAIRepositoryFactory implements Factory<AIRepository> {
  private final Provider<OpenAIService> openAIServiceProvider;

  private final Provider<SummaryDao> summaryDaoProvider;

  private final Provider<Gson> gsonProvider;

  public AppModule_ProvideAIRepositoryFactory(Provider<OpenAIService> openAIServiceProvider,
      Provider<SummaryDao> summaryDaoProvider, Provider<Gson> gsonProvider) {
    this.openAIServiceProvider = openAIServiceProvider;
    this.summaryDaoProvider = summaryDaoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public AIRepository get() {
    return provideAIRepository(openAIServiceProvider.get(), summaryDaoProvider.get(), gsonProvider.get());
  }

  public static AppModule_ProvideAIRepositoryFactory create(
      Provider<OpenAIService> openAIServiceProvider, Provider<SummaryDao> summaryDaoProvider,
      Provider<Gson> gsonProvider) {
    return new AppModule_ProvideAIRepositoryFactory(openAIServiceProvider, summaryDaoProvider, gsonProvider);
  }

  public static AIRepository provideAIRepository(OpenAIService openAIService, SummaryDao summaryDao,
      Gson gson) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAIRepository(openAIService, summaryDao, gson));
  }
}
