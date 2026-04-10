package com.auranote.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.auranote.app.data.model.TranscriptSegment;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TranscriptDao_Impl implements TranscriptDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TranscriptSegment> __insertionAdapterOfTranscriptSegment;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSegmentsByRecordingId;

  public TranscriptDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTranscriptSegment = new EntityInsertionAdapter<TranscriptSegment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `transcript_segments` (`id`,`recordingId`,`speakerLabel`,`text`,`startTimeSeconds`,`endTimeSeconds`,`confidence`,`segmentIndex`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TranscriptSegment entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRecordingId());
        statement.bindString(3, entity.getSpeakerLabel());
        statement.bindString(4, entity.getText());
        statement.bindDouble(5, entity.getStartTimeSeconds());
        statement.bindDouble(6, entity.getEndTimeSeconds());
        statement.bindDouble(7, entity.getConfidence());
        statement.bindLong(8, entity.getSegmentIndex());
      }
    };
    this.__preparedStmtOfDeleteSegmentsByRecordingId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM transcript_segments WHERE recordingId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSegments(final List<TranscriptSegment> segments,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTranscriptSegment.insert(segments);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSegmentsByRecordingId(final long recordingId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSegmentsByRecordingId.acquire();
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
          __preparedStmtOfDeleteSegmentsByRecordingId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TranscriptSegment>> getSegmentsByRecordingId(final long recordingId) {
    final String _sql = "SELECT * FROM transcript_segments WHERE recordingId = ? ORDER BY segmentIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transcript_segments"}, new Callable<List<TranscriptSegment>>() {
      @Override
      @NonNull
      public List<TranscriptSegment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfSpeakerLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "speakerLabel");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfStartTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeSeconds");
          final int _cursorIndexOfEndTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeSeconds");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfSegmentIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "segmentIndex");
          final List<TranscriptSegment> _result = new ArrayList<TranscriptSegment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TranscriptSegment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordingId;
            _tmpRecordingId = _cursor.getLong(_cursorIndexOfRecordingId);
            final String _tmpSpeakerLabel;
            _tmpSpeakerLabel = _cursor.getString(_cursorIndexOfSpeakerLabel);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final float _tmpStartTimeSeconds;
            _tmpStartTimeSeconds = _cursor.getFloat(_cursorIndexOfStartTimeSeconds);
            final float _tmpEndTimeSeconds;
            _tmpEndTimeSeconds = _cursor.getFloat(_cursorIndexOfEndTimeSeconds);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final int _tmpSegmentIndex;
            _tmpSegmentIndex = _cursor.getInt(_cursorIndexOfSegmentIndex);
            _item = new TranscriptSegment(_tmpId,_tmpRecordingId,_tmpSpeakerLabel,_tmpText,_tmpStartTimeSeconds,_tmpEndTimeSeconds,_tmpConfidence,_tmpSegmentIndex);
            _result.add(_item);
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
  public Object getSegmentsSync(final long recordingId,
      final Continuation<? super List<TranscriptSegment>> $completion) {
    final String _sql = "SELECT * FROM transcript_segments WHERE recordingId = ? ORDER BY segmentIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TranscriptSegment>>() {
      @Override
      @NonNull
      public List<TranscriptSegment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfSpeakerLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "speakerLabel");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfStartTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeSeconds");
          final int _cursorIndexOfEndTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeSeconds");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfSegmentIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "segmentIndex");
          final List<TranscriptSegment> _result = new ArrayList<TranscriptSegment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TranscriptSegment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordingId;
            _tmpRecordingId = _cursor.getLong(_cursorIndexOfRecordingId);
            final String _tmpSpeakerLabel;
            _tmpSpeakerLabel = _cursor.getString(_cursorIndexOfSpeakerLabel);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final float _tmpStartTimeSeconds;
            _tmpStartTimeSeconds = _cursor.getFloat(_cursorIndexOfStartTimeSeconds);
            final float _tmpEndTimeSeconds;
            _tmpEndTimeSeconds = _cursor.getFloat(_cursorIndexOfEndTimeSeconds);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final int _tmpSegmentIndex;
            _tmpSegmentIndex = _cursor.getInt(_cursorIndexOfSegmentIndex);
            _item = new TranscriptSegment(_tmpId,_tmpRecordingId,_tmpSpeakerLabel,_tmpText,_tmpStartTimeSeconds,_tmpEndTimeSeconds,_tmpConfidence,_tmpSegmentIndex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object searchInTranscript(final long recordingId, final String query,
      final Continuation<? super List<TranscriptSegment>> $completion) {
    final String _sql = "SELECT * FROM transcript_segments WHERE recordingId = ? AND text LIKE '%' || ? || '%' ORDER BY segmentIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TranscriptSegment>>() {
      @Override
      @NonNull
      public List<TranscriptSegment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfSpeakerLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "speakerLabel");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfStartTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeSeconds");
          final int _cursorIndexOfEndTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeSeconds");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfSegmentIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "segmentIndex");
          final List<TranscriptSegment> _result = new ArrayList<TranscriptSegment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TranscriptSegment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordingId;
            _tmpRecordingId = _cursor.getLong(_cursorIndexOfRecordingId);
            final String _tmpSpeakerLabel;
            _tmpSpeakerLabel = _cursor.getString(_cursorIndexOfSpeakerLabel);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final float _tmpStartTimeSeconds;
            _tmpStartTimeSeconds = _cursor.getFloat(_cursorIndexOfStartTimeSeconds);
            final float _tmpEndTimeSeconds;
            _tmpEndTimeSeconds = _cursor.getFloat(_cursorIndexOfEndTimeSeconds);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final int _tmpSegmentIndex;
            _tmpSegmentIndex = _cursor.getInt(_cursorIndexOfSegmentIndex);
            _item = new TranscriptSegment(_tmpId,_tmpRecordingId,_tmpSpeakerLabel,_tmpText,_tmpStartTimeSeconds,_tmpEndTimeSeconds,_tmpConfidence,_tmpSegmentIndex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getFullTranscriptText(final long recordingId,
      final Continuation<? super String> $completion) {
    final String _sql = "SELECT GROUP_CONCAT(text, ' ') FROM transcript_segments WHERE recordingId = ? ORDER BY segmentIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<String>() {
      @Override
      @Nullable
      public String call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final String _result;
          if (_cursor.moveToFirst()) {
            final String _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(0);
            }
            _result = _tmp;
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
