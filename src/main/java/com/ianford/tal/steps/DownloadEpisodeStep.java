package com.ianford.tal.steps;

import com.ianford.podcasts.model.BasicEpisodeRecord;
import com.ianford.podcasts.tal.util.EpisodeDownloader;
import com.ianford.podcasts.tal.util.MissingEpisodeFinder;
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
    private final MissingEpisodeFinder missingEpisodeFinder;

    private final DynamoDbTable<BasicEpisodeRecord> table;

    /**
     * Constructor
     *
     * @param episodeDownloader    Used to download specific episodes
     * @param missingEpisodeFinder Used to identify episodes that are currently missing from the data set
     * @param table
     */
    public DownloadEpisodeStep(EpisodeDownloader episodeDownloader, MissingEpisodeFinder missingEpisodeFinder,
                               DynamoDbTable<BasicEpisodeRecord> table) {
        this.episodeDownloader = episodeDownloader;
        this.missingEpisodeFinder = missingEpisodeFinder;
        this.table = table;
    }


    @Override
    public void run() {
        logger.info("Downloading missing episodes");

        BasicEpisodeRecord record = table.getItem(Key.builder()
                .partitionValue("TAL")
                .sortValue("EP#LATEST")
                .build());

        int latestEpNumber = Optional.ofNullable(record)
                .map(BasicEpisodeRecord::getValue)
                .map(Integer::parseInt)
                .map(latest -> latest + 1)
                .orElseGet(() -> 1);

        table.updateItem(new BasicEpisodeRecord("TAL", "EP#LATEST", String.valueOf(latestEpNumber)));

        logger.info("Now Downloading Episode {}", latestEpNumber);
        episodeDownloader.apply(latestEpNumber);
        logger.info("All missing episodes downloaded");
    }
}
