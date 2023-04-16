package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.BasicPodcastRecord;
import com.ianford.podcasts.model.ParsedEpisode;
import com.ianford.podcasts.model.jekyll.Episode;
import com.ianford.tal.io.RawEpisodeParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.io.File;
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

    private final DynamoDbTable<BasicPodcastRecord> table;
    private final RawEpisodeParser episodeParser;
    private final Gson gson;

    /**
     * Constructor.
     *
     * @param table         Used to store records we are backfilling.
     * @param episodeParser Used to convert raw HTML files into episode data.
     * @param gson          Used to serialize and deserialize statements
     */
    @SuppressWarnings("unused")
    public BackfillEpisodeDataStep(DynamoDbTable<BasicPodcastRecord> table, RawEpisodeParser episodeParser, Gson gson) {
        this.table = table;
        this.episodeParser = episodeParser;
        this.gson = gson;
    }

    @SuppressWarnings("unused")
    @Override
    public void run() throws IOException {

        // Get a list of every single file in the raw download folder
        List<Path> allFilePaths =
                Files.list(Path.of("_data/downloads/raw"))
                        .collect(Collectors.toList());

        // Parse all raw episode files
        List<ParsedEpisode> parsedEpisodeList = buildParsedEpisodeList(allFilePaths);

        // Read all lines of current episode list and remove last entry

        List<String> episodeList = new ArrayList<>();
        File episodeListFile = Path.of("_data/blog/episodeList.json")
                .toFile();
        if (episodeListFile.exists()) {
            episodeList = Files.readAllLines(Path.of("_data/blog/episodeList.json"));
            episodeList.remove(episodeList.size() - 1);
        } else {
            episodeList.add("[");
        }


        // For each parsed episode write that episode to a standalone file
        for (ParsedEpisode parsedEp : parsedEpisodeList) {
            Map<Integer, Episode> episodeMap = parsedEp.getEpisodeMap();
            for (Integer epNum : episodeMap.keySet()) {
                Path outputPath = Path.of(String.format("_data/blog/episodes/%s.json",
                        epNum));

                Episode episode = episodeMap.get(epNum);

                String serializedEpisode = gson.toJson(episodeMap.get(epNum));
                Files.writeString(outputPath,
                        serializedEpisode);

                String summary = gson.toJson(episode.summarize());
                episodeList.add(summary);

            }
        }

        episodeList.add("]");
        logger.info("Final episode list: {}",
                episodeList.toString());
        Files.write(episodeListFile.toPath(),
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

            BasicPodcastRecord record = table.getItem(Key.builder()
                    .partitionValue("TAL")
                    .sortValue(String.format("EP%s#NAME",
                            epNum))
                    .build());
            return Objects.nonNull(record);

        }
        return false;
    }
}
