package com.ianford.podcasts.model;


import org.apache.commons.csv.CSVRecord;

public class BasicEpisodeRecord extends AbstractEpisodeRecord {

    public BasicEpisodeRecord() {
    }

    public BasicEpisodeRecord(CSVRecord csvRecord) {
        this.episodeNumber = Integer.parseInt(csvRecord.get(0));
        this.episodeTitle = csvRecord.get(1);
        this.actNumber = Integer.parseInt(csvRecord.get(2));
        this.actName = csvRecord.get(3);
        this.speakerRole = csvRecord.get(4);
        this.speakerName = csvRecord.get(5);
        this.startTime = csvRecord.get(6);
        this.text = csvRecord.get(7);
    }
}
