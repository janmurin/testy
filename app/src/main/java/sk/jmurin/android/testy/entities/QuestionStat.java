package sk.jmurin.android.testy.entities;

import java.io.Serializable;

/**
 * Created by jan.murin on 17-Aug-16.
 */
public class QuestionStat implements Serializable{
    public final int db_id;
    public final int test_id;
    public int stat;

    /**
     *
     * @param db_id
     * @param test_id
     * @param stat
     */
    public QuestionStat(int db_id, int test_id, int stat) {
        this.db_id = db_id;
        this.test_id = test_id;
        this.stat = stat;
    }

    @Override
    public String toString() {
        return "QuestionStat{" +
                "db_id=" + db_id +
                ", test_id=" + test_id +
                ", stat=" + stat +
                '}';
    }
}
