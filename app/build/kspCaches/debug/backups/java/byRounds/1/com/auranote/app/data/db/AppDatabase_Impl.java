package com.auranote.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile RecordingDao _recordingDao;

  private volatile TranscriptDao _transcriptDao;

  private volatile SummaryDao _summaryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `recordings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `filePath` TEXT NOT NULL, `durationMs` INTEGER NOT NULL, `fileSizeBytes` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `transcriptionStatus` TEXT NOT NULL, `language` TEXT NOT NULL, `type` TEXT NOT NULL, `tags` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `speakerCount` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `transcript_segments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordingId` INTEGER NOT NULL, `speakerLabel` TEXT NOT NULL, `text` TEXT NOT NULL, `startTimeSeconds` REAL NOT NULL, `endTimeSeconds` REAL NOT NULL, `confidence` REAL NOT NULL, `segmentIndex` INTEGER NOT NULL, FOREIGN KEY(`recordingId`) REFERENCES `recordings`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transcript_segments_recordingId` ON `transcript_segments` (`recordingId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ai_summaries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordingId` INTEGER NOT NULL, `overview` TEXT NOT NULL, `keyPoints` TEXT NOT NULL, `actionItems` TEXT NOT NULL, `decisions` TEXT NOT NULL, `nextSteps` TEXT NOT NULL, `studyGuide` TEXT NOT NULL, `flashcardsJson` TEXT NOT NULL, `quizJson` TEXT NOT NULL, `generatedAt` INTEGER NOT NULL, FOREIGN KEY(`recordingId`) REFERENCES `recordings`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_summaries_recordingId` ON `ai_summaries` (`recordingId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1d23e5404be55981d48a2ff968e3be79')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `recordings`");
        db.execSQL("DROP TABLE IF EXISTS `transcript_segments`");
        db.execSQL("DROP TABLE IF EXISTS `ai_summaries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsRecordings = new HashMap<String, TableInfo.Column>(12);
        _columnsRecordings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("filePath", new TableInfo.Column("filePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("fileSizeBytes", new TableInfo.Column("fileSizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("transcriptionStatus", new TableInfo.Column("transcriptionStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("language", new TableInfo.Column("language", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("speakerCount", new TableInfo.Column("speakerCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecordings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecordings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRecordings = new TableInfo("recordings", _columnsRecordings, _foreignKeysRecordings, _indicesRecordings);
        final TableInfo _existingRecordings = TableInfo.read(db, "recordings");
        if (!_infoRecordings.equals(_existingRecordings)) {
          return new RoomOpenHelper.ValidationResult(false, "recordings(com.auranote.app.data.model.Recording).\n"
                  + " Expected:\n" + _infoRecordings + "\n"
                  + " Found:\n" + _existingRecordings);
        }
        final HashMap<String, TableInfo.Column> _columnsTranscriptSegments = new HashMap<String, TableInfo.Column>(8);
        _columnsTranscriptSegments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranscriptSegments.put("recordingId", new TableInfo.Column("recordingId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranscriptSegments.put("speakerLabel", new TableInfo.Column("speakerLabel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranscriptSegments.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranscriptSegments.put("startTimeSeconds", new TableInfo.Column("startTimeSeconds", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranscriptSegments.put("endTimeSeconds", new TableInfo.Column("endTimeSeconds", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranscriptSegments.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTranscriptSegments.put("segmentIndex", new TableInfo.Column("segmentIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTranscriptSegments = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTranscriptSegments.add(new TableInfo.ForeignKey("recordings", "CASCADE", "NO ACTION", Arrays.asList("recordingId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTranscriptSegments = new HashSet<TableInfo.Index>(1);
        _indicesTranscriptSegments.add(new TableInfo.Index("index_transcript_segments_recordingId", false, Arrays.asList("recordingId"), Arrays.asList("ASC")));
        final TableInfo _infoTranscriptSegments = new TableInfo("transcript_segments", _columnsTranscriptSegments, _foreignKeysTranscriptSegments, _indicesTranscriptSegments);
        final TableInfo _existingTranscriptSegments = TableInfo.read(db, "transcript_segments");
        if (!_infoTranscriptSegments.equals(_existingTranscriptSegments)) {
          return new RoomOpenHelper.ValidationResult(false, "transcript_segments(com.auranote.app.data.model.TranscriptSegment).\n"
                  + " Expected:\n" + _infoTranscriptSegments + "\n"
                  + " Found:\n" + _existingTranscriptSegments);
        }
        final HashMap<String, TableInfo.Column> _columnsAiSummaries = new HashMap<String, TableInfo.Column>(11);
        _columnsAiSummaries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("recordingId", new TableInfo.Column("recordingId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("overview", new TableInfo.Column("overview", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("keyPoints", new TableInfo.Column("keyPoints", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("actionItems", new TableInfo.Column("actionItems", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("decisions", new TableInfo.Column("decisions", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("nextSteps", new TableInfo.Column("nextSteps", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("studyGuide", new TableInfo.Column("studyGuide", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("flashcardsJson", new TableInfo.Column("flashcardsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("quizJson", new TableInfo.Column("quizJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSummaries.put("generatedAt", new TableInfo.Column("generatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAiSummaries = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysAiSummaries.add(new TableInfo.ForeignKey("recordings", "CASCADE", "NO ACTION", Arrays.asList("recordingId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesAiSummaries = new HashSet<TableInfo.Index>(1);
        _indicesAiSummaries.add(new TableInfo.Index("index_ai_summaries_recordingId", false, Arrays.asList("recordingId"), Arrays.asList("ASC")));
        final TableInfo _infoAiSummaries = new TableInfo("ai_summaries", _columnsAiSummaries, _foreignKeysAiSummaries, _indicesAiSummaries);
        final TableInfo _existingAiSummaries = TableInfo.read(db, "ai_summaries");
        if (!_infoAiSummaries.equals(_existingAiSummaries)) {
          return new RoomOpenHelper.ValidationResult(false, "ai_summaries(com.auranote.app.data.model.AISummary).\n"
                  + " Expected:\n" + _infoAiSummaries + "\n"
                  + " Found:\n" + _existingAiSummaries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "1d23e5404be55981d48a2ff968e3be79", "deb9a00382a69c2b17ec3445632a783f");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "recordings","transcript_segments","ai_summaries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `recordings`");
      _db.execSQL("DELETE FROM `transcript_segments`");
      _db.execSQL("DELETE FROM `ai_summaries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RecordingDao.class, RecordingDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TranscriptDao.class, TranscriptDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SummaryDao.class, SummaryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RecordingDao recordingDao() {
    if (_recordingDao != null) {
      return _recordingDao;
    } else {
      synchronized(this) {
        if(_recordingDao == null) {
          _recordingDao = new RecordingDao_Impl(this);
        }
        return _recordingDao;
      }
    }
  }

  @Override
  public TranscriptDao transcriptDao() {
    if (_transcriptDao != null) {
      return _transcriptDao;
    } else {
      synchronized(this) {
        if(_transcriptDao == null) {
          _transcriptDao = new TranscriptDao_Impl(this);
        }
        return _transcriptDao;
      }
    }
  }

  @Override
  public SummaryDao summaryDao() {
    if (_summaryDao != null) {
      return _summaryDao;
    } else {
      synchronized(this) {
        if(_summaryDao == null) {
          _summaryDao = new SummaryDao_Impl(this);
        }
        return _summaryDao;
      }
    }
  }
}
