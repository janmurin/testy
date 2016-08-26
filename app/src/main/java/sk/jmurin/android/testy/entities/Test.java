package sk.jmurin.android.testy.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jan.murin on 15-Aug-16.
 */
public class Test implements Serializable {
    private   String name;
    private   int version;
    private   int id;
    private   List<Question> questions;

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    void setVersion(int version) {
        this.version = version;
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Test test = (Test) o;

        return id == test.id;

    }

    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", version=" + version +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int getSkorePercento() {
        int percento = 0;
        for (int j = 0; j < questions.size(); j++) {
            int stat = questions.get(j).getStat();
            if (stat >= 0) {
                percento += stat;
            }
        }
        percento = (int) (percento / (double) (questions.size() * 3) * 100);
        return percento;
    }
}
