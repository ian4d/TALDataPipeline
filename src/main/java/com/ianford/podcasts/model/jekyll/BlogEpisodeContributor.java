package com.ianford.podcasts.model.jekyll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlogEpisodeContributor {

    private final String name;
    private Map<Integer, String> episodes;
    private List<String> statements;
    private Set<String> spokenWords;

    public BlogEpisodeContributor(String name) {
        this.name = name;
        this.episodes = new HashMap<>();
        this.statements = new ArrayList<>();
        this.spokenWords = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Map<Integer, String> getEpisodes() {
        return episodes;
    }

    public List<String> getStatements() {
        return statements;
    }

    public Set<String> getSpokenWords() {
        return spokenWords;
    }

    public void setEpisodes(Map<Integer, String> episodes) {
        this.episodes = episodes;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }

    public void setSpokenWords(Set<String> spokenWords) {
        this.spokenWords = spokenWords;
    }
}
