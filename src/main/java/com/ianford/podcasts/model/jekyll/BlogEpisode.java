package com.ianford.podcasts.model.jekyll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlogEpisode {

    private final int episodeNumber;
    private String episodeTitle;

    private Map<String, BlogEpisodeContributor> contributorMap;

    private Map<Integer, BlogEpisodeAct> actMap;

    private final List<BlogEpisodeStatement> blogEpisodeStatementList;

    public BlogEpisode(int episodeNumber) {
        this.episodeNumber = episodeNumber;
        this.contributorMap = new HashMap<>();
        this.actMap = new HashMap<>();
        this.blogEpisodeStatementList = new ArrayList<>();
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public Map<String, BlogEpisodeContributor> getContributorMap() {
        return contributorMap;
    }

    public Map<Integer, BlogEpisodeAct> getActMap() {
        return actMap;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public void setContributorMap(Map<String, BlogEpisodeContributor> contributorMap) {
        this.contributorMap = contributorMap;
    }

    public void setActMap(Map<Integer, BlogEpisodeAct> actMap) {
        this.actMap = actMap;
    }

    public List<BlogEpisodeStatement> getStatementList() {
        return blogEpisodeStatementList;
    }

    public Summary summarize() {
        return new Summary(String.valueOf(this.episodeNumber),
                this.episodeTitle,
                contributorMap.values()
                        .stream()
                        .map(BlogEpisodeContributor::getName)
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
