package sk.jmurin.android.testy.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jan.murin on 15-Aug-16.
 */
public class Test implements Serializable {
    public String name;
    public int version;
    public boolean isSingleAnswer = true;
    public List<Question> questions = new ArrayList<>();


    @Override
    public String toString() {
        return "Test{" +
                "name='" + name + '\'' +
                ", version=" + version +
                ", isSingleAnswer=" + isSingleAnswer +
                ", questions=" + questions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Test test = (Test) o;

        if (version != test.version) return false;
        return name.equals(test.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version;
        return result;
    }
}
