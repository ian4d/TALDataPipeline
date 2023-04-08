package com.ianford.podcasts.model.jekyll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Episode {

    private final int episodeNumber;
    private String episodeTitle;

    private Map<String, Contributor> contributorMap;

    private Map<Integer, Act> actMap;

    private List<Statement> statementList;

    public Episode(int episodeNumber) {
        this.episodeNumber = episodeNumber;
        this.contributorMap = new HashMap<>();
        this.actMap = new HashMap<>();
        this.statementList = new ArrayList<>();
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public Map<String, Contributor> getContributorMap() {
        return contributorMap;
    }

    public Map<Integer, Act> getActMap() {
        return actMap;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public void setContributorMap(Map<String, Contributor> contributorMap) {
        this.contributorMap = contributorMap;
    }

    public void setActMap(Map<Integer, Act> actMap) {
        this.actMap = actMap;
    }

    public List<Statement> getStatementList() {
        return statementList;
    }
}
