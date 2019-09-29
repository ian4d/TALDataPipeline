package com.ianford.podcasts.model.jekyll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogEpisodeAct {

    private final int actNumber;
    private String actName;
    private Map<String, BlogEpisodeContributor> contributorMap;
    private List<BlogEpisodeStatement> blogEpisodeStatementList;

    /**
     * Represents a single act in the episode for the blog.
     *
     * @param actNumber The number of the act.
     */
    public BlogEpisodeAct(int actNumber) {
        this.actNumber = actNumber;
        this.contributorMap = new HashMap<>();
        this.blogEpisodeStatementList = new ArrayList<>();
    }

    public int getActNumber() {
        return actNumber;
    }

    public String getActName() {
        return actName;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public Map<String, BlogEpisodeContributor> getContributorMap() {
        return contributorMap;
    }

    public void setContributorMap(Map<String, BlogEpisodeContributor> contributorMap) {
        this.contributorMap = contributorMap;
    }

    public List<BlogEpisodeStatement> getStatementList() {
        return blogEpisodeStatementList;
    }

    public void setStatementList(List<BlogEpisodeStatement> blogEpisodeStatementList) {
        this.blogEpisodeStatementList = blogEpisodeStatementList;
    }
}
