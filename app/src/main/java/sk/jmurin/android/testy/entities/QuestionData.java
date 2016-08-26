package sk.jmurin.android.testy.entities;

/**
 * Created by jan.murin on 26-Aug-16.
 */
public class QuestionData {

    public int stat;
    public int db_id;
    public int test_question_index;
    public int test_id;

    public QuestionData(int stat, int db_id, int test_question_index, int test_id) {
        this.stat = stat;
        this.db_id = db_id;
        this.test_question_index = test_question_index;
        this.test_id = test_id;
    }
}
