package com.ianford.tal.steps;

import com.ianford.podcasts.model.BasicEpisodeRecord;
import com.ianford.podcasts.model.DBKey;
import com.ianford.podcasts.tal.util.EpisodeDownloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

/**
 * Used to download missing episodes
 */
public class DownloadEpisodeStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();
    private final EpisodeDownloader episodeDownloader;

    private final DynamoDbTable<BasicEpisodeRecord> table;

    /**
     * Constructor.
     *
     * @param episodeDownloader Used to download specific episodes.
     * @param table             Used to identify the last episode we parsed.
     */
    @SuppressWarnings("unused")
    public DownloadEpisodeStep(EpisodeDownloader episodeDownloader, DynamoDbTable<BasicEpisodeRecord> table) {
        this.episodeDownloader = episodeDownloader;
        this.table = table;
    }


    @SuppressWarnings("unused")
    @Override
    public void run() {
        logger.info("Downloading missing episodes");

        BasicEpisodeRecord record = table.getItem(Key.builder()
                .partitionValue(DBKey.PARTITION.getValue())
                .sortValue(DBKey.LATEST_EPISODE.getValue())
                .build());

        int latestEpNumber = Optional.ofNullable(record)
                .map(BasicEpisodeRecord::getValue)
                .map(Integer::parseInt)
                .map(latest -> latest + 1)
                .orElseGet(() -> 1);

        table.updateItem(new BasicEpisodeRecord(DBKey.PARTITION.getValue(), DBKey.LATEST_EPISODE.getValue(),
                String.valueOf(latestEpNumber)));

        logger.info("Now Downloading Episode {}", latestEpNumber);
        episodeDownloader.apply(latestEpNumber);
        logger.info("All missing episodes downloaded");
    }
}
