package sk.jmurin.android.testy.entities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jan.murin on 17-Aug-16.
 */
public class TestStats implements Serializable {
    public final String test_name;
    public final int test_version;
    public final ArrayList<QuestionStat> stats = new ArrayList<>();

    /**
     * prida do arraylistu na danu poziciu questionstat
     * poz
     *
     * @param test_id
     * @param stat
     * @param db_id
     * @return
     */
    public boolean addQuestionStat(int test_id, int stat, int db_id) {
        if (test_id >= stats.size()) {
            stats.ensureCapacity(test_id + 1);// pozor nato lebo idcko v liste a velkost listu je velky rozdiel
        }
        stats.add(test_id, new QuestionStat(db_id, test_id, stat));
        return true;
    }

    /**
     * @param test_name
     * @param test_version
     */
    public TestStats(String test_name, int test_version) {
        this.test_name = test_name;
        this.test_version = test_version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestStats testStats = (TestStats) o;

        if (test_version != testStats.test_version) return false;
        return test_name.equals(testStats.test_name);

    }

    @Override
    public int hashCode() {
        int result = test_name.hashCode();
        result = 31 * result + test_version;
        return result;
    }

    public int getSkorePercento() {
        int percento = 0;
        for (int j = 0; j < stats.size(); j++) {
            int stat = stats.get(j).stat;
            if (stat >= 0) {
                percento += stat;
            }
        }
        percento = (int) (percento / (double) (stats.size() * 3) * 100);
        return percento;
    }
}
