package sk.jmurin.android.testy.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jan.murin on 16-Aug-16.
 */
public class Question implements Serializable {
    private String question;
    private List<Answer> answers;
    private int testQuestionIndex;
    private int stat;
    private int dbID;

    public String getQuestion() {
        return question;
    }

    void setQuestion(String question) {
        this.question = question;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public int getTestQuestionIndex() {
        return testQuestionIndex;
    }

    void setTestQuestionIndex(int testQuestionIndex) {
        this.testQuestionIndex = testQuestionIndex;
    }

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                ", answers=" + answers +
                ", id=" + testQuestionIndex +
                '}';
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public int getStat() {
        return stat;
    }

    void setDbID(int dbID) {
        this.dbID = dbID;
    }

    public int getDbID() {
        return dbID;
    }
}
