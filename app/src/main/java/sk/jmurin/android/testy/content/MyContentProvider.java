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
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.Tables.QUESTION_STATS, DataContract.Codes.QUESTIONS);
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.Tables.QUESTION_STAT_ID, DataContract.Codes.QUESTIONS_ID);
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
            case DataContract.Codes.QUESTIONS:
                return DataContract.QuestionStats.CONTENT_TYPE;
            case DataContract.Codes.QUESTIONS_ID:
                return DataContract.QuestionStats.CONTENT_TYPE_ITEM;
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
            case DataContract.Codes.QUESTIONS:
                queryBuilder.setTables(DataContract.Tables.QUESTION_STATS);
                break;
            case DataContract.Codes.QUESTIONS_ID:
                queryBuilder.setTables(DataContract.Tables.QUESTION_STATS);
                queryBuilder.appendWhere(DataContract.QuestionStats._ID + "=" + uri.getLastPathSegment());
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
            case DataContract.Codes.QUESTIONS:
                id = db.insertWithOnConflict(DataContract.Tables.QUESTION_STATS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
            case DataContract.Codes.QUESTIONS:
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
            case DataContract.Codes.QUESTIONS:
                delCount = db.delete(DataContract.Tables.QUESTION_STATS, selection, selectionArgs);
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
            case DataContract.Codes.QUESTIONS:
                updateCount = db.update(DataContract.Tables.QUESTION_STATS, values, selection, selectionArgs);
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
