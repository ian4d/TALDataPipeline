package com.ianford.podcasts.model;

import com.ianford.podcasts.model.jekyll.Episode;

import java.util.List;
import java.util.Map;

public class ParsedEpisode {
    final List<BasicPodcastRecord> databaseRecords;
    final Map<Integer, Episode> episodeMap;


    public ParsedEpisode(List<BasicPodcastRecord> databaseRecords, Map<Integer, Episode> episodeMap) {
        this.databaseRecords = databaseRecords;
        this.episodeMap = episodeMap;
    }

    public List<BasicPodcastRecord> getDatabaseRecords() {
        return databaseRecords;
    }

    public Map<Integer, Episode> getEpisodeMap() {
        return episodeMap;
    }
}
