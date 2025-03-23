package com.azlan.mindcare;

public class GuidedExercise {
    private final String title;
    private final String link;

    public GuidedExercise(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }
}
