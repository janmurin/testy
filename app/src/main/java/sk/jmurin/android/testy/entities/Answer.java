package sk.jmurin.android.testy.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by jan.murin on 16-Aug-16.
 */

public class Answer implements Serializable{
    private  String text;
    private  boolean isCorrect;

    public String getText() {
        return text;
    }

     void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }


     void setIsCorrect(boolean correct) {
        isCorrect = correct;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "text='" + text + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
