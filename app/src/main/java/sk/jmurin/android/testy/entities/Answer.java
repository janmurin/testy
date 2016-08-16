package sk.jmurin.android.testy.entities;

/**
 * Created by jan.murin on 16-Aug-16.
 */
public class Answer {
    public String text;
    public boolean isCorrect;

    @Override
    public String toString() {
        return "Answer{" +
                "text='" + text + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
