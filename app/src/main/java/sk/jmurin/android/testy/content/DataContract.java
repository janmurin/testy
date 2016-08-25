package sk.jmurin.android.testy.content;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by zavadpe on 7/26/16.
 */
public class DataContract {

    public static final String AUTHORITY = "sk.jmurin.android.testy";

    /* The content:// style URL for the top-level authority */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    private static String buildContentTypeDir(String name) {
        return "vnd.android.cursor.dir/" + AUTHORITY + "." + name;
    }

    private static String buildContentTypeItem(String name) {
        return "vnd.android.cursor.item/" + AUTHORITY + "." + name;
    }

    public interface Tables {
        String QUESTION_STATS = "question_stats";
        String QUESTION_STAT_ID = "question_stats/#";
    }

    public interface Codes {
        int QUESTIONS = 100;
        int QUESTIONS_ID = 101;
    }

    public interface QuestionColumns extends BaseColumns{
//        String TEST_NAME = "test_name";
//        String TEST_VERSION = "test_version";

        String TEST_ID = "test_id";
        String STAT = "stat";
        String TEST_QUESTION_INDEX = "test_question_index";
    }

    public static final class QuestionStats implements QuestionColumns {
        public static final String CONTENT_TYPE = buildContentTypeDir(Tables.QUESTION_STATS);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.QUESTION_STATS);
        public static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.QUESTION_STAT_ID);
    }
}
