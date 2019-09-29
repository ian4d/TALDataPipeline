package com.ianford.tal.steps;

import com.ianford.podcasts.model.db.DBPartitionKey;
import com.ianford.podcasts.model.db.DBSortKey;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.tal.model.PipelineConfig;
import com.ianford.tal.util.DBUtil;
import com.ianford.tal.util.EpisodeDownloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.nio.file.Path;

/**
 * Used to download missing episodes.
 */
public class DownloadEpisodeStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();
    private final EpisodeDownloader episodeDownloader;
    private final DBUtil dbUtil;

    /**
     * Constructor.
     *
     * @param episodeDownloader Used to download specific episodes.
     * @param dbUtil            Stores db records.
     */
    public DownloadEpisodeStep(EpisodeDownloader episodeDownloader, DBUtil dbUtil) {
        this.episodeDownloader = episodeDownloader;

        this.dbUtil = dbUtil;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) {
        logger.info("Downloading missing episodes");

        // Extract episode number from most recent episode and increment by 1 for download
        int latestEpNumber = dbUtil.getMostRecentlyParsedEpisode()
                .map(latest -> latest + 1)
                .orElseGet(() -> 1);

        logger.info("Latest episode number: ",
                latestEpNumber);

        // Write a new latest episode record to our table
//        table.updateItem(new PodcastDBDBRecord(DBPartitionKey.PODCAST_NAME.getValue(),
//                DBSortKey.LATEST_EPISODE.getValue(),
//                String.valueOf(latestEpNumber)));
        dbUtil.saveRecord(new PodcastDBDBRecord(DBPartitionKey.PODCAST_NAME.getValue(),
                DBSortKey.LATEST_EPISODE.getValue(),
                String.valueOf(latestEpNumber)));

        logger.info("Now Downloading Episode {}",
                latestEpNumber);

        // Prepare path to downloads by resolving local paths against working directory
        Path downloadPath = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getLocalDownloadDirectory());

        // Download the actual episode
        pipelineConfig.getDownloadedEpisodes()
                .add(episodeDownloader.apply(latestEpNumber,
                        downloadPath));
    }
}
