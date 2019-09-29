package com.ianford.tal.steps;

import com.ianford.podcasts.model.BasicEpisodeRecord;
import com.ianford.podcasts.tal.io.TALEpisodeParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BackfillDatabaseStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final DynamoDbTable<BasicEpisodeRecord> table;
    private final TALEpisodeParser episodeParser;

    /**
     * Constructor.
     *
     * @param table
     * @param episodeParser
     */
    public BackfillDatabaseStep(DynamoDbTable<BasicEpisodeRecord> table, TALEpisodeParser episodeParser) {
        this.table = table;
        this.episodeParser = episodeParser;
    }

    @Override
    public void run() throws IOException {
        // Converted all files into data
        List<Path> allFilePaths =
                Files.list(Path.of("data/downloads/raw")).collect(Collectors.toList());

        List<BasicEpisodeRecord> writtenContents = allFilePaths.stream()
                .peek(path -> logger.info("Reading file at {}", path.toString()))
                .filter(path -> !episodeIsAlreadyParsed(path))
                .map(path -> episodeParser.apply(path))
                .flatMap(List::stream)
                .peek(record -> logger.debug(record.toString()))
                .map(this::writeRecordToDB)// write each record to DB
                .collect(Collectors.toList());

        logger.debug("writtenContents: {}", writtenContents.size());

        // Read all locally stored files
        // Convert all fo them into records in our db
    }

    boolean episodeIsAlreadyParsed(Path path) {
        logger.info("Running matcher against path {}", path.toString());
        // Get episode number from filepath
        Matcher matcher = Pattern.compile("episode-(\\d+).html").matcher(path.getFileName().toString());
        if (matcher.matches()) {
            String epNum = matcher.toMatchResult().group(1);
            // Need to see if this ep is already present in the DB

            BasicEpisodeRecord record = table.getItem(Key.builder()
                    .partitionValue("TAL")
                    .sortValue(String.format("EP%s#NAME", epNum))
                    .build());
            return Objects.nonNull(record);

        }
        return false;
    }

    BasicEpisodeRecord writeRecordToDB(BasicEpisodeRecord record) {
        logger.debug("Writing record: {}", record.toString());
        table.putItem(record);
        return record;
    }

    String readFile(Path path) {
        try (Stream<String> lines = Files.readAllLines(path).stream()) {
            return lines.collect(Collectors.joining(""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
