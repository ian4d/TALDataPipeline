package com.ianford.tal.steps;

import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.podcasts.tal.db.writer.DatabaseWriter;
import com.ianford.podcasts.tal.file.EpisodeRecordListBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Used to persist our model to the DB
 */
public class PersistModelStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();


    private final EpisodeRecordListBuilder episodeRecordListBuilder;
    private final DatabaseWriter dbWriter;

    /**
     * Constructor
     *
     * @param episodeRecordListBuilder Builds a List of episode records
     * @param dbWriter                 Writes records to our DB
     */
    public PersistModelStep(EpisodeRecordListBuilder episodeRecordListBuilder,
                            DatabaseWriter dbWriter) {
        this.episodeRecordListBuilder = episodeRecordListBuilder;
        this.dbWriter = dbWriter;
    }


    @Override
    public void run() {
        logger.info("Building episode model and writing to database");
        List<List<EpisodeRecord>> episodeRecordList = episodeRecordListBuilder.get();
        episodeRecordList.stream()
                .flatMap(List::stream)
                .forEach(dbWriter);
    }
}
