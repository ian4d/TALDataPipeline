package com.ianford.podcasts.model.jekyll;

public class Statement {

    private final String speakerName;
    private final String text;

    public Statement(String speakerName, String text) {
        this.speakerName = speakerName;
        this.text = text;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public String getText() {
        return text;
    }
}
