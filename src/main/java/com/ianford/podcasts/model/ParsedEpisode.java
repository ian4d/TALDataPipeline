package com.ianford.podcasts.model;

import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.podcasts.model.jekyll.BlogEpisode;

import java.util.List;
import java.util.Map;

public class ParsedEpisode {
    final List<PodcastDBDBRecord> databaseRecords;
    final Map<Integer, BlogEpisode> episodeMap;

    public ParsedEpisode(List<PodcastDBDBRecord> databaseRecords, Map<Integer, BlogEpisode> episodeMap) {
        this.databaseRecords = databaseRecords;
        this.episodeMap = episodeMap;
    }

    public List<PodcastDBDBRecord> getDatabaseRecords() {
        return databaseRecords;
    }

    public Map<Integer, BlogEpisode> getEpisodeMap() {
        return episodeMap;
    }
}
