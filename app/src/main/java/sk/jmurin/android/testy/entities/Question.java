package sk.jmurin.android.testy.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jan.murin on 16-Aug-16.
 */
public class Question implements Serializable{
    public String question;
    public List<Answer> answers=new ArrayList<>();

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                ", answersSize=" + answers +
                '}';
    }
}
