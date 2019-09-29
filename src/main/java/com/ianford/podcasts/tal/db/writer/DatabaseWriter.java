package com.ianford.podcasts.tal.db.writer;

import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Used to write records into the DB
 */
public class DatabaseWriter implements Consumer<EpisodeRecord> {

    private static final Logger logger = LogManager.getLogger();
    private final Collection<Consumer<EpisodeRecord>> consumers;

    /**
     * Constructor
     *
     * @param recordConsumers
     */
    public DatabaseWriter(Collection<Consumer<EpisodeRecord>> recordConsumers) {
        this.consumers = recordConsumers;
    }

    /**
     * Inserts the provided episodeRecord into the DB
     *
     * @param episodeRecord The record to insert
     */
    @Override
    public void accept(EpisodeRecord episodeRecord) {
        consumers.forEach(consumer -> consumer.accept(episodeRecord));
    }
}
