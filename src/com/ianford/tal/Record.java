package com.ianford.tal;

import java.time.LocalDate;

/**
 * Describes individual statements from episodes
 */
public class Record {
    private int episodeNumber;
    private String episodeTitle;
    private LocalDate airDate;
    private int actNumber;
    private String actName;
    private String speakerRole;
    private String speakerName;
    private String startTime;
    private String text;


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

    public LocalDate getAirDate() {
        return airDate;
    }

    public void setAirDate(LocalDate airDate) {
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
    public String toString() {
        return "Record{" +
                "episodeNumber=" + episodeNumber +
                ", episodeTitle='" + episodeTitle + '\'' +
                ", airDate=" + airDate +
                ", actNumber=" + actNumber +
                ", actName='" + actName + '\'' +
                ", speakerRole='" + speakerRole + '\'' +
                ", speakerName='" + speakerName + '\'' +
                ", startTime=" + startTime +
                ", text='" + text + '\'' +
                '}';
    }

    public String toCSV() {
    }
}
