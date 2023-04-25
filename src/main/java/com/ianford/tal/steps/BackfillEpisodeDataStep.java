package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.ParsedEpisode;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.podcasts.model.jekyll.BlogEpisode;
import com.ianford.tal.io.RawEpisodeParser;
import com.ianford.tal.model.PipelineConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Takes all existing raw data, parses it, and writes it to DDB and to our serialized representation used for Jekyll.
 */
public class BackfillEpisodeDataStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final DynamoDbTable<PodcastDBDBRecord> table;
    private final RawEpisodeParser episodeParser;
    private final Gson gson;

    /**
     * Constructor.
     *
     * @param table         Used to store records we are backfilling.
     * @param episodeParser Used to convert raw HTML files into episode data.
     * @param gson          Used to serialize and deserialize statements
     */
    public BackfillEpisodeDataStep(DynamoDbTable<PodcastDBDBRecord> table, RawEpisodeParser episodeParser, Gson gson) {
        this.table = table;
        this.episodeParser = episodeParser;
        this.gson = gson;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {

        // Get a list of every single file in the raw download folder
        Path rawDownloadDir = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getLocalDownloadDirectory());

        List<Path> allFilePaths = Files.list(rawDownloadDir)
                .collect(Collectors.toList());

        // Parse all raw episode files
        List<ParsedEpisode> parsedEpisodeList = buildParsedEpisodeList(allFilePaths);

        // Read all lines of current episode list and remove last entry
        List<String> episodeList = new ArrayList<>();
        Path episodeListPath = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getEpisodeListFilepath());
        if (episodeListPath.toFile()
                .exists()) {
            episodeList = Files.readAllLines(episodeListPath);
            episodeList.remove(episodeList.size() - 1);
        } else {
            episodeList.add("[");
        }


        // For each parsed episode write that episode to a standalone file
        for (ParsedEpisode parsedEp : parsedEpisodeList) {
            Map<Integer, BlogEpisode> episodeMap = parsedEp.getEpisodeMap();
            for (Integer epNum : episodeMap.keySet()) {
                Path outputPath = pipelineConfig.getWorkingDirectory()
                        .resolve(pipelineConfig.getLocalParsedEpisodeDirectory())
                        .resolve(String.format("episode-%s.json",
                                epNum));

                BlogEpisode blogEpisode = episodeMap.get(epNum);
                String serializedEpisode = gson.toJson(episodeMap.get(epNum));
                Files.writeString(outputPath,
                        serializedEpisode);

                String summary = gson.toJson(blogEpisode.summarize());
                episodeList.add(summary);
            }

            pipelineConfig.getParsedEpisodes()
                    .add(parsedEp);
        }

        episodeList.add("]");
        Files.write(episodeListPath,
                episodeList);

    }

    /**
     * Builds a List of ParsedEpisode objects based on raw files available in the local file system.
     *
     * @param allFilePaths List of Paths of every locally downloaded file
     * @return List
     */
    private List<ParsedEpisode> buildParsedEpisodeList(List<Path> allFilePaths) {
        List<ParsedEpisode> parsedEpisodeList = allFilePaths.stream()
                .filter(path -> !episodeIsAlreadyParsed(path))
                .map(episodeParser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return parsedEpisodeList;
    }


    /**
     * Determines if a parsed representation of this episode is already present on disk.
     *
     * @param path Path to check
     * @return true if a parsed representation is already available locally
     */
    boolean episodeIsAlreadyParsed(Path path) {
        logger.info("Running matcher against path {}",
                path.toString());
        // Get episode number from filepath
        Matcher matcher = Pattern.compile("episode-(\\d+).html")
                .matcher(path.getFileName()
                        .toString());
        if (matcher.matches()) {
            String epNum = matcher.toMatchResult()
                    .group(1);
            // Need to see if this ep is already present in the DB

            PodcastDBDBRecord record = table.getItem(Key.builder()
                    .partitionValue("TAL")
                    .sortValue(String.format("EP%s#NAME",
                            epNum))
                    .build());
            return Objects.nonNull(record);

        }
        return false;
    }
}
