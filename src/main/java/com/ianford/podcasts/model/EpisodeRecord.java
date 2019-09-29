package com.ianford.podcasts.model;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Represents a single episode of some particular show
 */
public interface EpisodeRecord extends Supplier<Collection<String>> {

    int getEpisodeNumber();

    void setEpisodeNumber(int episodeNumber);

    String getEpisodeTitle();

    void setEpisodeTitle(String episodeTitle);

    String getAirDate();

    void setAirDate(String airDate);

    int getActNumber();

    void setActNumber(int actNumber);

    String getActName();

    void setActName(String actName);

    String getSpeakerRole();

    void setSpeakerRole(String speakerRole);

    String getSpeakerName();

    void setSpeakerName(String speakerName);

    String getStartTime();

    void setStartTime(String startTime);

    String getText();

    void setText(String text);
}
