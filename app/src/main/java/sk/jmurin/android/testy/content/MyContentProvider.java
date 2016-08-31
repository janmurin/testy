package sk.jmurin.android.testy.content;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by zavadpe on 7/26/16.
 */
public class MyContentProvider extends ContentProvider {

    public static final String TAG = MyContentProvider.class.getSimpleName();

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.Tables.QUESTION_STATS, DataContract.Codes.QUESTION_STATS);
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.Tables.QUESTION_STAT_ID, DataContract.Codes.QUESTIONS_STATS_ID);
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.Tables.STATS_NOT_SENT, DataContract.Codes.STATS_NOT_SENT);
    }

    private SQLiteDatabase db;
    private DatabaseOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()");
        dbHelper = new DatabaseOpenHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case DataContract.Codes.QUESTION_STATS:
                return DataContract.QuestionStats.CONTENT_TYPE;
            case DataContract.Codes.QUESTIONS_STATS_ID:
                return DataContract.QuestionStats.CONTENT_TYPE_ITEM;
            case DataContract.Codes.STATS_NOT_SENT:
                return DataContract.StatsNotSent.CONTENT_TYPE;
            default:
                throw new RuntimeException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String groupBy = null;

        switch (uriMatcher.match(uri)) {
            case DataContract.Codes.QUESTION_STATS:
                queryBuilder.setTables(DataContract.Tables.QUESTION_STATS);
                break;
            case DataContract.Codes.QUESTIONS_STATS_ID:
                queryBuilder.setTables(DataContract.Tables.QUESTION_STATS);
                queryBuilder.appendWhere(DataContract.QuestionStats._ID + "=" + uri.getLastPathSegment());
                break;
            case DataContract.Codes.STATS_NOT_SENT:
                queryBuilder.setTables(DataContract.Tables.STATS_NOT_SENT);
                break;
            default:
                throw new RuntimeException("Unknown uri: " + uri);
        }

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = BaseColumns._ID;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, groupBy, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = 0;
        switch (uriMatcher.match(uri)) {
            case DataContract.Codes.QUESTION_STATS:
                id = db.insertWithOnConflict(DataContract.Tables.QUESTION_STATS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case DataContract.Codes.STATS_NOT_SENT:
                id = db.insertWithOnConflict(DataContract.Tables.STATS_NOT_SENT, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            default:
                throw new RuntimeException("Unknown uri: " + uri);
        }
        Uri newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numInserted = 0;
        String table;
        switch (uriMatcher.match(uri)) {
            case DataContract.Codes.QUESTION_STATS:
                table = DataContract.Tables.QUESTION_STATS;
                break;
            default:
                throw new RuntimeException("Unknown uri: " + uri);
        }
        db.beginTransaction();
        try {
            for (ContentValues cv : values) {
                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
            numInserted = values.length;
        } finally {
            db.endTransaction();
        }
        return numInserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delCount = 0;
        switch (uriMatcher.match(uri)) {
            case DataContract.Codes.QUESTION_STATS:
                delCount = db.delete(DataContract.Tables.QUESTION_STATS, selection, selectionArgs);
                break;
            case DataContract.Codes.STATS_NOT_SENT:
                delCount = db.delete(DataContract.Tables.STATS_NOT_SENT, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        switch (uriMatcher.match(uri)) {
            case DataContract.Codes.QUESTION_STATS:
                updateCount = db.update(DataContract.Tables.QUESTION_STATS, values, selection, selectionArgs);
                break;
            case DataContract.Codes.QUESTIONS_STATS_ID:
                int id = Integer.parseInt(uri.getLastPathSegment());
                updateCount = db.update(DataContract.Tables.QUESTION_STATS, values, DataContract.QuestionStats._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
