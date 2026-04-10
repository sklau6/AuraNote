package com.auranote.app.di;

import com.auranote.app.data.db.AppDatabase;
import com.auranote.app.data.db.SummaryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideSummaryDaoFactory implements Factory<SummaryDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSummaryDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SummaryDao get() {
    return provideSummaryDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSummaryDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSummaryDaoFactory(dbProvider);
  }

  public static SummaryDao provideSummaryDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSummaryDao(db));
  }
}
