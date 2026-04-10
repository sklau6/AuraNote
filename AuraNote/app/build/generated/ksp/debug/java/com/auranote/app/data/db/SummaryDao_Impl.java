package com.auranote.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.auranote.app.data.model.AISummary;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SummaryDao_Impl implements SummaryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AISummary> __insertionAdapterOfAISummary;

  private final EntityDeletionOrUpdateAdapter<AISummary> __updateAdapterOfAISummary;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSummaryByRecordingId;

  public SummaryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAISummary = new EntityInsertionAdapter<AISummary>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ai_summaries` (`id`,`recordingId`,`overview`,`keyPoints`,`actionItems`,`decisions`,`nextSteps`,`studyGuide`,`flashcardsJson`,`quizJson`,`generatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AISummary entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRecordingId());
        statement.bindString(3, entity.getOverview());
        statement.bindString(4, entity.getKeyPoints());
        statement.bindString(5, entity.getActionItems());
        statement.bindString(6, entity.getDecisions());
        statement.bindString(7, entity.getNextSteps());
        statement.bindString(8, entity.getStudyGuide());
        statement.bindString(9, entity.getFlashcardsJson());
        statement.bindString(10, entity.getQuizJson());
        statement.bindLong(11, entity.getGeneratedAt());
      }
    };
    this.__updateAdapterOfAISummary = new EntityDeletionOrUpdateAdapter<AISummary>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `ai_summaries` SET `id` = ?,`recordingId` = ?,`overview` = ?,`keyPoints` = ?,`actionItems` = ?,`decisions` = ?,`nextSteps` = ?,`studyGuide` = ?,`flashcardsJson` = ?,`quizJson` = ?,`generatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AISummary entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRecordingId());
        statement.bindString(3, entity.getOverview());
        statement.bindString(4, entity.getKeyPoints());
        statement.bindString(5, entity.getActionItems());
        statement.bindString(6, entity.getDecisions());
        statement.bindString(7, entity.getNextSteps());
        statement.bindString(8, entity.getStudyGuide());
        statement.bindString(9, entity.getFlashcardsJson());
        statement.bindString(10, entity.getQuizJson());
        statement.bindLong(11, entity.getGeneratedAt());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSummaryByRecordingId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ai_summaries WHERE recordingId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSummary(final AISummary summary,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAISummary.insertAndReturnId(summary);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSummary(final AISummary summary,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAISummary.handle(summary);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSummaryByRecordingId(final long recordingId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSummaryByRecordingId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, recordingId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSummaryByRecordingId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<AISummary> getSummaryByRecordingId(final long recordingId) {
    final String _sql = "SELECT * FROM ai_summaries WHERE recordingId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ai_summaries"}, new Callable<AISummary>() {
      @Override
      @Nullable
      public AISummary call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfOverview = CursorUtil.getColumnIndexOrThrow(_cursor, "overview");
          final int _cursorIndexOfKeyPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "keyPoints");
          final int _cursorIndexOfActionItems = CursorUtil.getColumnIndexOrThrow(_cursor, "actionItems");
          final int _cursorIndexOfDecisions = CursorUtil.getColumnIndexOrThrow(_cursor, "decisions");
          final int _cursorIndexOfNextSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "nextSteps");
          final int _cursorIndexOfStudyGuide = CursorUtil.getColumnIndexOrThrow(_cursor, "studyGuide");
          final int _cursorIndexOfFlashcardsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "flashcardsJson");
          final int _cursorIndexOfQuizJson = CursorUtil.getColumnIndexOrThrow(_cursor, "quizJson");
          final int _cursorIndexOfGeneratedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "generatedAt");
          final AISummary _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordingId;
            _tmpRecordingId = _cursor.getLong(_cursorIndexOfRecordingId);
            final String _tmpOverview;
            _tmpOverview = _cursor.getString(_cursorIndexOfOverview);
            final String _tmpKeyPoints;
            _tmpKeyPoints = _cursor.getString(_cursorIndexOfKeyPoints);
            final String _tmpActionItems;
            _tmpActionItems = _cursor.getString(_cursorIndexOfActionItems);
            final String _tmpDecisions;
            _tmpDecisions = _cursor.getString(_cursorIndexOfDecisions);
            final String _tmpNextSteps;
            _tmpNextSteps = _cursor.getString(_cursorIndexOfNextSteps);
            final String _tmpStudyGuide;
            _tmpStudyGuide = _cursor.getString(_cursorIndexOfStudyGuide);
            final String _tmpFlashcardsJson;
            _tmpFlashcardsJson = _cursor.getString(_cursorIndexOfFlashcardsJson);
            final String _tmpQuizJson;
            _tmpQuizJson = _cursor.getString(_cursorIndexOfQuizJson);
            final long _tmpGeneratedAt;
            _tmpGeneratedAt = _cursor.getLong(_cursorIndexOfGeneratedAt);
            _result = new AISummary(_tmpId,_tmpRecordingId,_tmpOverview,_tmpKeyPoints,_tmpActionItems,_tmpDecisions,_tmpNextSteps,_tmpStudyGuide,_tmpFlashcardsJson,_tmpQuizJson,_tmpGeneratedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSummarySync(final long recordingId,
      final Continuation<? super AISummary> $completion) {
    final String _sql = "SELECT * FROM ai_summaries WHERE recordingId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AISummary>() {
      @Override
      @Nullable
      public AISummary call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfOverview = CursorUtil.getColumnIndexOrThrow(_cursor, "overview");
          final int _cursorIndexOfKeyPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "keyPoints");
          final int _cursorIndexOfActionItems = CursorUtil.getColumnIndexOrThrow(_cursor, "actionItems");
          final int _cursorIndexOfDecisions = CursorUtil.getColumnIndexOrThrow(_cursor, "decisions");
          final int _cursorIndexOfNextSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "nextSteps");
          final int _cursorIndexOfStudyGuide = CursorUtil.getColumnIndexOrThrow(_cursor, "studyGuide");
          final int _cursorIndexOfFlashcardsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "flashcardsJson");
          final int _cursorIndexOfQuizJson = CursorUtil.getColumnIndexOrThrow(_cursor, "quizJson");
          final int _cursorIndexOfGeneratedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "generatedAt");
          final AISummary _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordingId;
            _tmpRecordingId = _cursor.getLong(_cursorIndexOfRecordingId);
            final String _tmpOverview;
            _tmpOverview = _cursor.getString(_cursorIndexOfOverview);
            final String _tmpKeyPoints;
            _tmpKeyPoints = _cursor.getString(_cursorIndexOfKeyPoints);
            final String _tmpActionItems;
            _tmpActionItems = _cursor.getString(_cursorIndexOfActionItems);
            final String _tmpDecisions;
            _tmpDecisions = _cursor.getString(_cursorIndexOfDecisions);
            final String _tmpNextSteps;
            _tmpNextSteps = _cursor.getString(_cursorIndexOfNextSteps);
            final String _tmpStudyGuide;
            _tmpStudyGuide = _cursor.getString(_cursorIndexOfStudyGuide);
            final String _tmpFlashcardsJson;
            _tmpFlashcardsJson = _cursor.getString(_cursorIndexOfFlashcardsJson);
            final String _tmpQuizJson;
            _tmpQuizJson = _cursor.getString(_cursorIndexOfQuizJson);
            final long _tmpGeneratedAt;
            _tmpGeneratedAt = _cursor.getLong(_cursorIndexOfGeneratedAt);
            _result = new AISummary(_tmpId,_tmpRecordingId,_tmpOverview,_tmpKeyPoints,_tmpActionItems,_tmpDecisions,_tmpNextSteps,_tmpStudyGuide,_tmpFlashcardsJson,_tmpQuizJson,_tmpGeneratedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
