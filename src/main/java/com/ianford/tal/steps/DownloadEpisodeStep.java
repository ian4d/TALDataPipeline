package com.ianford.tal.steps;

import com.ianford.podcasts.model.db.DBPartitionKey;
import com.ianford.podcasts.model.db.DBSortKey;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.tal.model.PipelineConfig;
import com.ianford.tal.util.EpisodeDownloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Used to download missing episodes.
 */
public class DownloadEpisodeStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();
    private final EpisodeDownloader episodeDownloader;
    private final DynamoDbTable<PodcastDBDBRecord> table;

    /**
     * Constructor.
     *
     * @param episodeDownloader Used to download specific episodes.
     * @param table             Used to identify the last episode we parsed.
     */
    public DownloadEpisodeStep(EpisodeDownloader episodeDownloader, DynamoDbTable<PodcastDBDBRecord> table) {
        this.episodeDownloader = episodeDownloader;
        this.table = table;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) {
        logger.info("Downloading missing episodes");

        // Find most recent episode stored in the table
        PodcastDBDBRecord record = table.getItem(Key.builder()
                .partitionValue(DBPartitionKey.PODCAST_NAME.getValue())
                .sortValue(DBSortKey.LATEST_EPISODE.getValue())
                .build());

        // Extract episode number from most recent episode and increment by 1 for download
        int latestEpNumber = Optional.ofNullable(record)
                .map(PodcastDBDBRecord::getValue)
                .map(Integer::parseInt)
                .map(latest -> latest + 1)
                .orElseGet(() -> 1);

        logger.info("Latest episode number: ",
                latestEpNumber);

        // Write a new latest episode record to our table
        table.updateItem(new PodcastDBDBRecord(DBPartitionKey.PODCAST_NAME.getValue(),
                DBSortKey.LATEST_EPISODE.getValue(),
                String.valueOf(latestEpNumber)));

        logger.info("Now Downloading Episode {}",
                latestEpNumber);

        // Prepare path to downloads by resolving local paths against working directory
        Path downloadPath = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getLocalDownloadDirectory());
        // Download the actual episode
        episodeDownloader.apply(latestEpNumber,
                downloadPath);
    }
}
