package com.ianford.tal.steps;

import com.ianford.podcasts.model.BasicPodcastRecord;
import com.ianford.podcasts.model.DBPartitionKey;
import com.ianford.podcasts.model.DBSortKey;
import com.ianford.tal.util.EpisodeDownloader;
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

    private final DynamoDbTable<BasicPodcastRecord> table;

    /**
     * Constructor.
     *
     * @param episodeDownloader Used to download specific episodes.
     * @param table             Used to identify the last episode we parsed.
     */
    @SuppressWarnings("unused")
    public DownloadEpisodeStep(EpisodeDownloader episodeDownloader,
            DynamoDbTable<BasicPodcastRecord> table) {
        this.episodeDownloader = episodeDownloader;
        this.table = table;
    }


    @SuppressWarnings("unused")
    @Override
    public void run() {
        logger.info("Downloading missing episodes");

        BasicPodcastRecord record = table.getItem(Key.builder()
                .partitionValue(DBPartitionKey.PODCAST_NAME.getValue())
                .sortValue(DBSortKey.LATEST_EPISODE.getValue())
                .build());

        int latestEpNumber = Optional.ofNullable(record)
                .map(BasicPodcastRecord::getValue)
                .map(Integer::parseInt)
                .map(latest -> latest + 1)
                .orElseGet(() -> 1);

        logger.info("Latest episode number: ",
                latestEpNumber);

        table.updateItem(new BasicPodcastRecord(DBPartitionKey.PODCAST_NAME.getValue(),
                DBSortKey.LATEST_EPISODE.getValue(),
                String.valueOf(latestEpNumber)));

        logger.info("Now Downloading Episode {}",
                latestEpNumber);
        episodeDownloader.apply(latestEpNumber);
    }
}
