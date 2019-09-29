package com.ianford.podcasts.model;

import java.util.Arrays;
import java.util.Collection;

public abstract class AbstractEpisodeRecord implements EpisodeRecord {

    int episodeNumber;
    String episodeTitle;
    int actNumber;
    String actName;
    String speakerRole;
    String speakerName;
    String startTime;
    String text;
    private String airDate;

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public String getAirDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }

    public int getActNumber() {
        return actNumber;
    }

    public void setActNumber(int actNumber) {
        this.actNumber = actNumber;
    }

    public String getActName() {
        return actName;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public String getSpeakerRole() {
        return speakerRole;
    }

    public void setSpeakerRole(String speakerRole) {
        this.speakerRole = speakerRole;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public Collection<String> get() {
        return Arrays.asList(
                String.valueOf(episodeNumber),
                episodeTitle,
                String.valueOf(actNumber),
                actName,
                speakerRole,
                speakerName,
                startTime,
                text
        );
    }
}
