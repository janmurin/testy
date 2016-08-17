package sk.jmurin.android.testy.entities;

/**
 * Created by jan.murin on 16-Aug-16.
 */
public class TestInformation {
    public String name;
    public int version;
    public int questionsSize;
    public int answersSize;
    public int id;

    public TestInformation(){

    }

    public TestInformation(Test test, int id) {
        this.answersSize = test.questions.get(0).answers.size();
        this.name = test.name;
        this.questionsSize = test.questions.size();
        this.version = test.version;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestInformation that = (TestInformation) o;

        if (version != that.version) return false;
        if (questionsSize != that.questionsSize) return false;
        if (answersSize != that.answersSize) return false;
        if (id != that.id) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version;
        result = 31 * result + questionsSize;
        result = 31 * result + answersSize;
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        return "TestInformation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version=" + version +
                '}';
    }
}
