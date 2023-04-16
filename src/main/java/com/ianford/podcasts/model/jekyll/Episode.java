package com.ianford.podcasts.model.jekyll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Episode {

    private final int episodeNumber;
    private String episodeTitle;

    private Map<String, Contributor> contributorMap;

    private Map<Integer, Act> actMap;

    private final List<Statement> statementList;

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

    public Summary summarize() {
        return new Summary(String.valueOf(this.episodeNumber),
                this.episodeTitle,
                contributorMap.values()
                        .stream()
                        .map(Contributor::getName)
                        .collect(Collectors.toList()));

    }

    private static class Summary {

        private final String number;
        private final String title;
        private final List<String> contributors;

        private Summary(String number, String title, List<String> contributors) {
            this.number = number;
            this.title = title;
            this.contributors = contributors;
        }

    }
}
