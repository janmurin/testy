package sk.jmurin.android.testy.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import sk.jmurin.android.testy.App;
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
        //Log.d(TAG, "onCreate(SQLiteDatabase db)");
        App.zaloguj(App.DEBUG,TAG,"onCreate(SQLiteDatabase db)");
        db.execSQL(createQuestionsStatsTable());
        db.execSQL(createStatsNotSentTable());
    }

    private String createStatsNotSentTable() {
        final String query = DbUtils.TableBuilder.table(DataContract.Tables.STATS_NOT_SENT)
                .primaryKey(DataContract.StatsNotSent._ID)
                .columnText(DataContract.StatsNotSent.DEVICE_ID)
                .columnText(DataContract.StatsNotSent.SKORE)
                .columnText(DataContract.StatsNotSent.STATS)
                .columnText(DataContract.StatsNotSent.TEST_ID)
                .columnText(DataContract.StatsNotSent.TEST_VERSION)
                .columnText(DataContract.StatsNotSent.TIME_CREATED)
                .columnText(DataContract.StatsNotSent.USERNAME)
                .build();
        //Log.d(TAG, "query: "+query);
        App.zaloguj(App.DEBUG,TAG,"query: "+query);
        return query;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.d(TAG, "onUpgrade");
        App.zaloguj(App.DEBUG,TAG,"onUpgrade");
        db.execSQL(DbUtils.dropTable(DataContract.Tables.QUESTION_STATS));
        onCreate(db);
    }

    private String createQuestionsStatsTable() {
        final String query = DbUtils.TableBuilder.table(DataContract.Tables.QUESTION_STATS)
                .primaryKey(DataContract.QuestionStats._ID)
                .columnInt(DataContract.QuestionStats.TEST_ID)
                .columnInt(DataContract.QuestionStats.STAT)
                .columnInt(DataContract.QuestionStats.TEST_QUESTION_INDEX)
                .build();
        //Log.d(TAG, "query: "+query);
        App.zaloguj(App.DEBUG,TAG,"query: "+query);
        return query;
    }
}
