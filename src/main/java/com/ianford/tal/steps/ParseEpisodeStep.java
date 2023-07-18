package com.ianford.tal.steps;

import com.ianford.podcasts.model.ParsedEpisode;
import com.ianford.tal.io.RawEpisodeParser;
import com.ianford.tal.model.PipelineConfig;
import com.ianford.tal.util.DBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class ParseEpisodeStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final DBUtil dbUtil;
    private final RawEpisodeParser episodeParser;

    /**
     * Constructor.
     *
     * @param dbUtil         Used to store records we are backfilling.
     * @param episodeParser Used to convert raw HTML files into episode data.
     */
    public ParseEpisodeStep(DBUtil dbUtil, RawEpisodeParser episodeParser) {
        this.dbUtil = dbUtil;
        this.episodeParser = episodeParser;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {
        logger.info("Running ParseEpisodeStep");

        // Get a list of every single file in the raw download folder
        Path rawDownloadDir = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getLocalDownloadDirectory());

        // Collect all the paths to a List
        List<Path> allFilePaths = Files.list(rawDownloadDir)
                .collect(Collectors.toList());

        // Build a list of every file at every path created above and then run each file through the episode parser
        pipelineConfig.setParsedEpisodes(buildParsedEpisodeList(allFilePaths));
        pipelineConfig.getParsedEpisodes()
                .stream()
                .map(ParsedEpisode::getDatabaseRecords)
                .flatMap(List::stream)
                .forEach(dbUtil::saveRecord);
    }


    /**
     * Builds a List of ParsedEpisode objects based on raw files available in the local file system.
     *
     * @param allFilePaths List of Paths of every locally downloaded file
     * @return List
     */
    private List<ParsedEpisode> buildParsedEpisodeList(List<Path> allFilePaths) {
        List<ParsedEpisode> parsedEpisodeList = allFilePaths.stream()
                .peek(path -> logger.info("About to parse episode at {}",
                        path.toString()))
                .map(episodeParser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return parsedEpisodeList;
    }
}
