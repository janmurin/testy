package sk.jmurin.android.testy.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import sk.jmurin.android.testy.utils.DbUtils;


/**
 * Created by zavadpe on 7/26/16.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseOpenHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "testy_db";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL(createQuestionsStatsTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        db.execSQL(DbUtils.dropTable(DataContract.Tables.QUESTION_STATS));
        onCreate(db);
    }

    private String createQuestionsStatsTable() {
        final String query = DbUtils.TableBuilder.table(DataContract.Tables.QUESTION_STATS)
                .primaryKey(DataContract.QuestionStats._ID)
                .columnText(DataContract.QuestionStats.TEST_NAME)
                .columnInt(DataContract.QuestionStats.TEST_VERSION)
                .columnInt(DataContract.QuestionStats.STAT)
                .columnInt(DataContract.QuestionStats.QUESTION_TEST_ID)
                .build();
        Log.d(TAG, "query: "+query);
        return query;
    }
}
