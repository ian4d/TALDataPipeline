package com.ianford.podcasts.model.jekyll;

public class BlogEpisodeStatement {

    private final String speakerName;
    private final String text;

    public BlogEpisodeStatement(String speakerName, String text) {
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
