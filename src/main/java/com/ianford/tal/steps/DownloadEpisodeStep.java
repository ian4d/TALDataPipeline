package com.ianford.tal.steps;

import com.ianford.podcasts.tal.util.EpisodeDownloader;
import com.ianford.podcasts.tal.util.MissingEpisodeFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Used to download missing episodes
 */
public class DownloadEpisodeStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();
    private final EpisodeDownloader episodeDownloader;
    private final MissingEpisodeFinder missingEpisodeFinder;

    /**
     * Constructor
     *
     * @param episodeDownloader    Used to download specific episodes
     * @param missingEpisodeFinder Used to identify episodes that are currently missing from the data set
     */
    public DownloadEpisodeStep(EpisodeDownloader episodeDownloader, MissingEpisodeFinder missingEpisodeFinder) {
        this.episodeDownloader = episodeDownloader;
        this.missingEpisodeFinder = missingEpisodeFinder;
    }


    @Override
    public void run() {
        logger.info("Downloading missing episodes");
        List<Integer> missingEpNumList = missingEpisodeFinder.findMissingEpisodes();
        for (Integer epNum : missingEpNumList) {
            logger.info("Now Downloading Episode {}", epNum);
            episodeDownloader.apply(epNum);
        }
        logger.info("All missing episodes downloaded");
    }
}
