package sk.jmurin.android.testy.entities;

import java.io.Serializable;

/**
 * Created by jan.murin on 16-Aug-16.
 */
public class Answer implements Serializable{
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
