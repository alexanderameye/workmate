package me.example.paul.Model;

import java.io.Serializable;

public class Question implements Serializable {

    private String question_id;
    private String title;
    private String type;
    private int reward;

    public String getQuestion_id() {
        return question_id;
    }

    public String getQuestionTitle() {
        return title;
    }

    public String getQuestionType() {
        return type;
    }

    public int getReward() {
        return reward;
    }
}
